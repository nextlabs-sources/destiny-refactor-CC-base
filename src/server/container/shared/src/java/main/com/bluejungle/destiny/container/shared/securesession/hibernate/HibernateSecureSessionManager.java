/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.securesession.ISecureSession;
import com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager;
import com.bluejungle.destiny.container.shared.securesession.SecureSessionPersistenceException;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * Implementation of ISecureSessionManager which utilizes Hibernate to persist
 * secure sessions. Note that some of the session data is maintained on the
 * client side within the session key. This allows for clustered session
 * retrieval without the need of hitting the database on every visit to the
 * server to search for session created on other machines or to update
 * expiration/end of life times
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/security/securesession/hibernate/HibernateSecureSessionManager.java#3 $
 */

public class HibernateSecureSessionManager implements ISecureSessionManager,
        IPersistedSecureSessionLoader, ILogEnabled, IConfigurable, IInitializable {

    /**
     * Configuration property for session expiration time
     */
    public static final PropertyKey<Long> SESSION_TIMEOUT_CONFIG_PROPERTY_NAME = 
            new PropertyKey<Long>("SessionTimeoutConfigPropertyName");

    /**
     * Default session time out - 30 minutes
     */
    public static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000;
    
    
    /**
     * Configuration property for session cleanup interval, the period at which
     * a process is run to delete expired sessions from the database
     */
    public static final PropertyKey<Long> SESSION_CLEANUP_TIME_INTERVAL_PROPERTY_NAME = 
            new PropertyKey<Long>("SessionCleanupTimeInterval");
    
    /**
     * Default session cleanup interval - 1 day
     */
    public static final long DEFAULT_SESSION_CLEANUP_TIME_INTERVAL = 1 * 24 * 60 * 60 * 1000;

    private static final Timer SESSION_CLEANUP_TIMER = new Timer("HibernateSessionSweeper", true);

    
    /**
     * End of life propety name of SecureSessionDO
     */
    private static final String END_OF_LIFE_PROPERTY_NAME ="endOfLife";

    /**
     * End of life update time buffer (See use below) - 5 minutes
     */
    private static final long END_OF_LIFE_UPDATE_TIME_BUFFER = 5 * 60 * 1000;

    /**
     * ID propety name of SecureSessionDO
     */
    private static final String SECURE_SESSION_ID_PROPERTY_NAME = "id";

    /**
     * <code>SESSION_PROPERTY_CACHE</code> maintains a cache of session
     * properties to prevent database access on each session retrieval.
     * Properties can be cached, because they're read only. Other session
     * characteristics, such as session timeout and end of life cannot be
     * cached, becuse they may change on a trip to another server in the cluster
     */
    private static final Map<Long,Properties> SESSION_PROPERTY_CACHE = new HashMap<Long,Properties>();

    private Log log;
    private IConfiguration configuration;
    private long sessionTimeoutInMillis;
    private long sessionCleanupTimeInterval;

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(null)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration myConfig = getConfiguration();
        if (myConfig != null) {
            this.sessionTimeoutInMillis = myConfig.get(
                    SESSION_TIMEOUT_CONFIG_PROPERTY_NAME, 
                    DEFAULT_SESSION_TIMEOUT);
            this.sessionCleanupTimeInterval = myConfig.get(
                    SESSION_CLEANUP_TIME_INTERVAL_PROPERTY_NAME, 
                    DEFAULT_SESSION_CLEANUP_TIME_INTERVAL);
        } else {
            this.sessionTimeoutInMillis = DEFAULT_SESSION_TIMEOUT;
            this.sessionCleanupTimeInterval = DEFAULT_SESSION_CLEANUP_TIME_INTERVAL;
        }

        TimerTask sessionSweeperTask = new SecureSessionSweeper();
        SESSION_CLEANUP_TIMER.schedule(sessionSweeperTask, sessionCleanupTimeInterval,
                sessionCleanupTimeInterval);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager#createSession()
     */
    public ISecureSession createSession(Properties sessionProperties) throws SecureSessionPersistenceException {
        long currentTime = System.currentTimeMillis();
        long sessionExpirationTime = generateExpirationTime(currentTime);
        long endOfLife = generateEndOfLifeTime(currentTime);

        IPersistedSecureSession persistentSession = new SecureSessionDO(endOfLife, sessionProperties);

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.save(persistentSession);
            transaction.commit();

            SESSION_PROPERTY_CACHE.put(persistentSession.getId(), sessionProperties);
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, getLog());

            throw new SecureSessionPersistenceException(exception);
        } finally {
            HibernateUtils.closeSession(hSession, getLog());
        }

        return new SecureSessionImpl(persistentSession, sessionExpirationTime);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager#getSessionByKey(java.lang.String)
     */
    public ISecureSession getSessionByKey(String sessionKeyString) throws SecureSessionPersistenceException {
        ISecureSession sessionToReturn = null;

        SecureSessionKey sessionKey = SecureSessionKeyKeeper.extractKey(sessionKeyString);
        long currentTime = System.currentTimeMillis();
        if (sessionKey.getExpirationTime() > currentTime) {
            long newExpirationTime = generateExpirationTime();

            /*
             * Determine if we need to update the end of life. Based on the
             * following calculation, we update the end of life when it's within
             * 1 times the session expiration time + some buffering factor. The
             * buffering factor is used to handle race conditions (end of life
             * update race with cleanup process).
             *  
             */
            long sessionEndOfLife = sessionKey.getEndOfLifeTime();
            if ((sessionEndOfLife - currentTime) < (this.sessionTimeoutInMillis + END_OF_LIFE_UPDATE_TIME_BUFFER)) {
                long endOfLife = generateEndOfLifeTime(currentTime);
                Session hSession = null;
                Transaction transaction = null;
                try {
                    hSession = getSession();
                    transaction = hSession.beginTransaction();
                    IPersistedSecureSession persistedSession = getPersistentSession(
                            sessionKey.getId(), hSession);
                    persistedSession.setEndOfLife(endOfLife);
                    transaction.commit();

                    sessionToReturn = new SecureSessionImpl(persistedSession, newExpirationTime);
                } catch (HibernateException exception) {
                    HibernateUtils.rollbackTransation(transaction, getLog());

                    throw new SecureSessionPersistenceException(exception);
                } catch (SecureSessionNotFoundException exception) {
                    StringBuffer errorMessage = new StringBuffer("Valid session with id, ");
                    errorMessage.append(sessionKey.getId());
                    errorMessage.append(", could not found in database. Considering it invalid.");
                    getLog().error(errorMessage.toString(), exception);
                } finally {
                    HibernateUtils.closeSession(hSession, getLog());
                }
            } else {
                /*
                 * If we don't need to update the end of life time, we return a
                 * session without going to the DB for performance reasons.
                 */
                Properties sessionProperties = SESSION_PROPERTY_CACHE.get(sessionKey.getId());
                if (sessionProperties != null) {
                    SecureSessionDO persistedSession = new SecureSessionDO(
                            sessionKey.getEndOfLifeTime(), sessionProperties);
                    persistedSession.setId(sessionKey.getId());
                    sessionToReturn = new SecureSessionImpl(persistedSession, newExpirationTime);
                } else {
                    /*
                     * In this case, the hope is that the invoked Service will
                     * not require the session properties and a DB hit won't be
                     * required
                     */
                    sessionToReturn = new SecureSessionImpl(
                            this, 
                            sessionKey.getId(), 
                            newExpirationTime, 
                            sessionKey.getEndOfLifeTime());
                }
            }
        }

        return sessionToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager#getDefaultTimeout()
     */
    public long getDefaultTimeout() {
        return sessionTimeoutInMillis;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.hibernate.IPersistedSecureSessionLoader#getPersistedSession(long)
     */
    public IPersistedSecureSession getPersistedSession(Long sessionId)
            throws SecureSessionPersistenceException, SecureSessionNotFoundException {
        if (sessionId == null) {
            throw new NullPointerException("sessionId cannot be null.");
        }

        IPersistedSecureSession sessionToReturn = null;

        Session hSession = null;

        try {
            hSession = getSession();
            sessionToReturn = getPersistentSession(sessionId, hSession);
            SESSION_PROPERTY_CACHE.put(sessionId, sessionToReturn.getProperties());
        } catch (HibernateException e) {
            throw new SecureSessionPersistenceException(e);
        } finally {
            HibernateUtils.closeSession(hSession, getLog());
        }

        return sessionToReturn;
    }

    /**
     * Retrieve persistent session
     * 
     * @param sessionId
     *            the id of the sessiont or retrieve
     * @param hSession
     *            the Hibernate sesson to utilize
     * @return the persistent session with the provided id
     * @throws SecureSessionNotFoundException
     * @throws SecureSessionPersistenceException
     */
    private IPersistedSecureSession getPersistentSession(Long sessionId, Session hSession)
            throws SecureSessionNotFoundException, SecureSessionPersistenceException {
        if (sessionId == null) {
            throw new NullPointerException("sessionId cannot be null.");
        }
        
        SecureSessionDO sessionToReturn = null;
        try {
            Criteria queryCriteria = hSession.createCriteria(SecureSessionDO.class);
            queryCriteria.add(Expression.eq(SECURE_SESSION_ID_PROPERTY_NAME, sessionId));
            sessionToReturn = (SecureSessionDO) queryCriteria.uniqueResult();
            if (sessionToReturn == null) {
                throw new SecureSessionNotFoundException(sessionId.toString());
            }
        } catch (HibernateException exception) {
            throw new SecureSessionPersistenceException(exception);
        }

        return sessionToReturn;
    }

    /**
     * Generate a new session expiration time
     * 
     * @return a new session expiration time
     */
    private long generateExpirationTime() {
        return generateExpirationTime(System.currentTimeMillis());
    }

    /**
     * Generate a new expiration time based on the specified current time
     * 
     * @param currentTime
     *            the current time
     * @return a new expiration time based on the specified current time
     */
    private long generateExpirationTime(long currentTime) {
        return currentTime + sessionTimeoutInMillis;
    }

    /**
     * Generate a new end of life time based on the specified current time
     * 
     * @param currentTime
     *            the current time
     * @return a new expiration time based on the specified current time
     */
    private long generateEndOfLifeTime(long currentTime) {
        return currentTime + sessionCleanupTimeInterval;
    }

    /**
     * Retrieve a Hibernate Session
     * 
     * @return a Hibernate Session
     */
    private Session getSession() throws HibernateException {
        /*
         * Theoretically, secure sessions are read only and we could use a
         * single Hibernate Session. Everything I've read, however, recommends
         * not doing this. Therefore, to be same, I'm not doing it here as well.
         * The second level cache must then be used to cache secure session
         */
        // FIX ME - Make sure second level cache is utilized
        IHibernateRepository dataSource = this.getDataSource();
        return dataSource.getSession();
    }

    /**
     * Returns a data source object that can be used to create Hibernate
     * sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(
                DestinyRepository.ACTIVITY_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for ProfileManager.");
        }

        return dataSource;
    }

    /**
     * A timed task which performs cleanup of expired sessions from the DB
     * 
     * @author sgoldstein
     */
    private class SecureSessionSweeper extends TimerTask {

        /**
         * @see java.util.TimerTask#run()
         */
        public void run() {
            Session hSession = null;
            Transaction transaction = null;
            try {
                hSession = getSession();
                Iterator<SecureSessionDO> secureSessionsToDelete = retrieveSessionsToDelete();
                transaction = hSession.beginTransaction();
                while (secureSessionsToDelete.hasNext()) {
                    hSession.delete(secureSessionsToDelete.next());
                }
                transaction.commit();
            } catch (HibernateException exception) {
                getLog().warn("Failed to clean up expired secure sessions.", exception);

                HibernateUtils.rollbackTransation(transaction, HibernateSecureSessionManager.this.getLog());
            } finally {
                HibernateUtils.closeSession(hSession, HibernateSecureSessionManager.this.getLog());
            }
        }

        /**
         * Retrieve sessions which are to be deleted (based on end of life time)
         * 
         * @return An iterator of SecureSessionDO instances to be deleted
         * @throws HibernateException
         *             if a failure occurs while accessing the persistent store
         */
        private Iterator<SecureSessionDO> retrieveSessionsToDelete() throws HibernateException {
            Iterator<SecureSessionDO> valuesToReturn = null;

            Session hSession = getSession();
            try {
                long currentTime = System.currentTimeMillis();
                Criterion endOfLifeCriteria = Expression.lt(END_OF_LIFE_PROPERTY_NAME, new Long(currentTime));

                Criteria queryCriteria = hSession.createCriteria(SecureSessionDO.class);
                queryCriteria.add(endOfLifeCriteria);
                List<SecureSessionDO> retrievedItems = queryCriteria.list();

                // We need to clone the list in order to allow deletion of items
                valuesToReturn = new LinkedList<SecureSessionDO>(retrievedItems).iterator();
            } finally {
                HibernateUtils.closeSession(hSession, HibernateSecureSessionManager.this.getLog());
            }

            return valuesToReturn;
        }
    }
}
