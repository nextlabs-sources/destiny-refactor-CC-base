/*
 * Created on Aug 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.Reference;

import net.sf.hibernate.Databinder;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.impl.SessionFactoryImpl;
import net.sf.hibernate.metadata.ClassMetadata;
import net.sf.hibernate.metadata.CollectionMetadata;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.configuration.DestinyRepository;

/**
 * This abstract class serves as a HibernateSessionFactory. An object of this
 * class can be usable concurrently by multiple threads since it uses the
 * ThreadLocal class for thread-specific storage.
 * 
 * BE CAREFUL - Do not use the same counted session on two seperate threads
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/BaseHibernateRepositoryImpl.java#2 $
 */

public abstract class BaseHibernateRepositoryImpl implements IHibernateRepository {

    private static final long serialVersionUID = -6047581226849055340L;

    /*
     * Parameter to hold the DestinyRepositoryEnum type
     */
    public static final PropertyKey<DestinyRepository> REPOSITORY_ENUM_PARAM = 
        new PropertyKey<DestinyRepository>(BaseHibernateRepositoryImpl.class, "RepositoryEnum");

    /*
     * Member variables:
     */
    protected ThreadLocal<SessionAndCounter> currentThreadLocalSession;
    protected SessionFactory sessionFactory;
    private IComponentManager manager;
    private IConfiguration configuration;
    private Log log;

    private class SessionAndCounter {

        private Session session;
        private int counter = 0;

        /**
         * Don't remove this constructor! (Ianis and Sergey think it's a Java
         * bug).
         */
        public SessionAndCounter() {
        }

        public synchronized Session obtain(boolean counted) throws HibernateException {
            return obtain(counted, null);
        }

        public synchronized Session obtain(boolean counted, Interceptor interceptor) throws HibernateException {
            if (session == null || (!session.isOpen()) || (!session.isConnected())) {
                if (interceptor == null) {
                    session = getSession();
                } else {
                    session = getSession(interceptor);
                }
                log.debug("Opening new Hibernate session.");
                
                counter = 0;
            }
            if (counted) {
                counter++;
            } else {
                counter = 1;
            }
            
            log.debug("Retrieving session (counter=" + counter + ", threadid=" + Thread.currentThread().getId() + ")");
            
            return session;
        }

        public synchronized void release() throws HibernateException {
            counter--;
            
            // To be as robust as possible, we check here for 0 and less than 0
            if (counter <= 0) {
                log.debug("Closing session (counter=" + counter + ", threadid=" + Thread.currentThread().getId() + ")");
                if (session != null) {
                    session.close();
                    session = null;
                }
                log.debug("Session closed.");
                
                if (counter < 0) {
                    // Finding where this happens in the code can be very tricky.
                    // Perhaps we
                    // should cut the programmers some slack and just ignore this
                    throw new IllegalStateException("Session given too many times");
                }
            }
        }
    }

    /**
     * Get a SessionAndCounter instance specific to the current thread
     * 
     * @return SessionAndCounter instance
     */
    private SessionAndCounter getSessionAndCounter() {
        if (currentThreadLocalSession.get() == null) {
            currentThreadLocalSession.set(new SessionAndCounter());
        }
        return currentThreadLocalSession.get();
    }

    /**
     * Sets the new session factory
     * 
     * @param newSF
     *            new session factory
     */
    protected void setSessionFactory(SessionFactory newSF) {
        this.sessionFactory = newSF;
    }

    /**
     * Sets the new thread local session
     * 
     * @param newThreadLocalSession
     *            new thread local session
     */
    protected void setThreadLocalSession(ThreadLocal<SessionAndCounter> newThreadLocalSession) {
        this.currentThreadLocalSession = newThreadLocalSession;
    }
    
    public synchronized int getCountedSessionCount() {
        return getSessionAndCounter().counter;
    }

    /**
     * Returns a thread-specific 'current' session object. Creates one if one
     * doesn't exist. This method is thread-safe.
     * 
     * @return 'Current' Session
     */
    public synchronized Session getCurrentSession() throws HibernateException {
        return getSessionAndCounter().obtain(false);
    }

    /**
     * Returns a thread-specific 'current' session object with a given
     * interceptor. Creates one if one doesn't exist. This method is
     * thread-safe.
     * 
     * @return 'Current' Session
     */
    public synchronized Session getCurrentSession(Interceptor interceptor) throws HibernateException {
        return getSessionAndCounter().obtain(false, interceptor);
    }

    /**
     * Returns a thread-specific 'current' session object. Creates one if one
     * doesn't exist. This method is thread-safe.
     * 
     * @return 'Current' Session
     */
    public synchronized Session getCountedSession() throws HibernateException {
        return getSessionAndCounter().obtain(true);
    }

    /**
     * Returns a thread-specific 'current' session object with a given
     * interceptor. Creates one if one doesn't exist. This method is
     * thread-safe.
     * 
     * @return 'Current' Session
     */
    public synchronized Session getCountedSession(Interceptor interceptor) throws HibernateException {
        return getSessionAndCounter().obtain(true, interceptor);
    }

    /**
     * Destroys the current thread-local session object. This method is
     * thread-safe.
     */
    public synchronized void closeCurrentSession() {
        try {
            getSessionAndCounter().release();
        } catch (HibernateException e) {
            this.log.warn("Failed to close the current Hibernate session", e);
        }
    }

    /**
     * Creates a new session from the SQL connection. The caller of this
     * function has to close the session once it does not need it anymore.
     * 
     * @param connection
     *            SQL connection (cannot be null)
     * @return a new Hiberante Session object
     */
    public Session getSession(Connection connection) {
        Session session = null;
        if (connection == null) {
            throw new NullPointerException("Connection object cannot be null");
        }

        session = this.sessionFactory.openSession(connection);
        return session;
    }

    /**
     * @see net.sf.hibernate.SessionFactory#openSession(java.sql.Connection)
     */
    public Session openSession(Connection connection) {
        return sessionFactory.openSession(connection);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#openSession(java.sql.Connection,
     *      net.sf.hibernate.Interceptor)
     */
    public Session openSession(Connection connection, Interceptor interceptor) {
        return sessionFactory.openSession(connection, interceptor);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#openDatabinder()
     */
    public Databinder openDatabinder() throws HibernateException {
        return sessionFactory.openDatabinder();
    }

    /**
     * @see net.sf.hibernate.SessionFactory#getClassMetadata(java.lang.Class)
     */
    public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
        return sessionFactory.getClassMetadata(persistentClass);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#getCollectionMetadata(java.lang.String)
     */
    public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
        return sessionFactory.getCollectionMetadata(roleName);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#getAllClassMetadata()
     */
    public Map getAllClassMetadata() throws HibernateException {
        return sessionFactory.getAllClassMetadata();
    }

    /**
     * @see net.sf.hibernate.SessionFactory#getAllCollectionMetadata()
     */
    public Map getAllCollectionMetadata() throws HibernateException {
        return sessionFactory.getAllCollectionMetadata();
    }

    /**
     * @see net.sf.hibernate.SessionFactory#close()
     */
    public void close() throws HibernateException {
        sessionFactory.close();
    }

    /**
     * @see net.sf.hibernate.SessionFactory#evict(java.lang.Class)
     */
    public void evict(Class persistentClass) throws HibernateException {
        sessionFactory.evict(persistentClass);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#evict(java.lang.Class,
     *      java.io.Serializable)
     */
    public void evict(Class persistentClass, Serializable id) throws HibernateException {
        sessionFactory.evict(persistentClass, id);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#evictCollection(java.lang.String)
     */
    public void evictCollection(String roleName) throws HibernateException {
        sessionFactory.evictCollection(roleName);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#evictCollection(java.lang.String,
     *      java.io.Serializable)
     */
    public void evictCollection(String roleName, Serializable id) throws HibernateException {
        sessionFactory.evictCollection(roleName, id);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#evictQueries()
     */
    public void evictQueries() throws HibernateException {
        sessionFactory.evictQueries();
    }

    /**
     * @see net.sf.hibernate.SessionFactory#evictQueries(java.lang.String)
     */
    public void evictQueries(String cacheRegion) throws HibernateException {
        sessionFactory.evictQueries(cacheRegion);
    }

    /**
     * @see javax.naming.Referenceable#getReference()
     */
    public Reference getReference() throws NamingException {
        return sessionFactory.getReference();
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Sets the configuration data for initialization of the factory instance.
     * 
     * @param params
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration params) {
        this.configuration = params;
    }

    /**
     * Returns the configuration of this factory instance.
     * 
     * @return IConfiguration
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Performs cleanup operations for this factory instance before garbage
     * collection.
     * 
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        try {
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        } catch (HibernateException e) {
            log.warn("Failed to dispose hibernate repository component", e);
        }
    }

    /**
     * Creates a new session object with the specified interceptor. Lifecycle of
     * this object is the responsibility of the caller, not the callee. Every
     * time this method is called, a new object will be returned. This method
     * should be thread-safe.
     * 
     * @param interceptor
     *            The interceptor for this session.
     * @return A new Hibernate Session object
     */
    public abstract Session getSession(Interceptor interceptor) throws HibernateException;

    private static final String UNKNOWN_DBTYPE_MESSAGE = "Can't determine the %s because '%s' is unexpected";
    
    @Override
    public Dialect getDialect() throws IllegalArgumentException {
        if(sessionFactory instanceof SessionFactoryImpl){
            return ((SessionFactoryImpl)sessionFactory).getDialect();
        }
        throw new IllegalArgumentException(String.format(UNKNOWN_DBTYPE_MESSAGE, "sessionFactory", sessionFactory));
    }
    
    @Override
    public DbType getDatabaseType() {
        Dialect dialect = getDialect();
        if (dialect instanceof net.sf.hibernate.dialect.PostgreSQLDialect) {
            return DbType.POSTGRESQL;
        } else if (dialect instanceof net.sf.hibernate.dialect.Oracle9Dialect) {
            return DbType.ORACLE;
        } else if (dialect instanceof net.sf.hibernate.dialect.SQLServerDialect) {
            return DbType.MS_SQL;
        }
        throw new IllegalArgumentException(String.format(UNKNOWN_DBTYPE_MESSAGE, "dialect", dialect));
    }
    
    
}
