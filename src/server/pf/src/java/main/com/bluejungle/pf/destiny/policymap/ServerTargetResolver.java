package com.bluejungle.pf.destiny.policymap;

/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.datastore.hibernate.usertypes.StringArrayAsString;
import com.bluejungle.framework.utils.MultipartKey;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.IHasPQL;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.nextlabs.framework.messaging.IMessageHandler;
import com.nextlabs.framework.messaging.IMessageHandlerManager;
import com.nextlabs.framework.messaging.MessagingException;
import com.nextlabs.framework.messaging.handlers.EmailMessageHandler;
import com.nextlabs.framework.messaging.impl.MessageHandlerManagerImpl;
import com.nextlabs.framework.messaging.impl.StringMessage;

/**
 * @author sasha
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/policymap/ServerTargetResolver.java#1 $:
 */

public class ServerTargetResolver implements IManagerEnabled, IInitializable, ILogEnabled {

    private final MapBuildManager mapBuildManager = new MapBuildManager();

    public static final String MAP_BUILDER_THREAD_NAME = MapBuilder.class.getName();

    private static final long BUILD_REQUEST_MAX_WAIT = 1000 * 30 * 60;

    public static final ComponentInfo<ServerTargetResolver> COMP_INFO = 
        new ComponentInfo<ServerTargetResolver>(
            ServerTargetResolver.class
          , LifestyleType.SINGLETON_TYPE
    );

    private IComponentManager manager;
    private Log log;

    private LifecycleManager lm;
    private SessionFactory sf;
    private IDictionary dictionary;

    private Cache strCache;

    /**
     * Builds all the target resolution maps from scratch. replaces whatever
     * exists. Same as call to
     * {@link IServerTargetResolver#buildMaps(String[], long)}
     * with the second argument equal to {@link IServerTargetResolver}{@link #BUILD_REQUEST_MAX_WAIT}
     *
     * @param domains an array of domain names.
     * May not be empty, null, or contain null elements.
     */
    public void buildMaps(String[] domains) {
        buildMaps(domains, BUILD_REQUEST_MAX_WAIT);
    }

    /**
     * Builds all the target resolution maps from scratch. replaces whatever
     * exists. Same as call to
     * {@link IServerTargetResolver#buildMaps(String[], long)}
     * with the second argument equal to {@link IServerTargetResolver}{@link #BUILD_REQUEST_MAX_WAIT}
     *
     * @param domains an array of domain names.
     * May not be empty, null, or contain null elements.
     */
    public void buildMaps(String[] domains, long waitTime) {
        /*
         * Using new Date() (ie DABS' current time) below is ok, since it works
         * regardless of whether DABS clock is ahead or behind. If its behind,
         * we might miss a policy/component which we wouldn't have, had the
         * clocks been synchronized. However, the situation rectifies itself in
         * the next hearbeat. DABS being ahead is not an issue for the purposes
         * of getting latest deployment time.
         */
        buildMaps(domains, new Date(), waitTime);
    }

    /**
     * Although this method is private, it is called from unit tests through
     * reflection.
     * 
     * @param now
     *            The date as of which the map is to be built.
     */
    void buildMaps(String[] domains, Date now, long waitTime) {
        try {
            MapBuild mapBuild = mapBuildManager.getMapBuild(domains, now);
            mapBuild.waitForBuildToComplete(waitTime);
        } catch (InterruptedException e) {
            log.warn("Interupted while waiting for build to complete", e);
        }
    }

    /**
     * Returns true if there have been changes to the directory since the
     * specified <code>Date</code>.
     *
     * @param sinceWhen a <code>Date</code> specifying the exclusive
     * lower limit for the query (i.e. if the latest change happened
     * exactly at <code>sinceWhen</code>, it will not be reported.
     */
    public boolean haveDirectoryChangesSince(Date sinceWhen) {
        try {
            Date latestConsistentTime = dictionary.getLatestConsistentTime();
            return latestConsistentTime != null && latestConsistentTime.after(sinceWhen);
        } catch (DictionaryException de) {
            return false;
        }
    }

    /**
     * Access the STRLog for the given combination of domain names.
     * @return the log entry of the last map build, may be null
     * if maps have never been built.
     */
    public STRLog getSTRLog(String[] domains) {
        // Try getting a cached value
        MultipartKey key;
        if (domains != null && domains.length != 0) {
            key = new MultipartKey((Object[])domains);
        } else {
            // MultipartKey does not allow zero-length arrays:
            key = new MultipartKey(new Object[] {""});
        }
        STRLog cached = null;
        if (strCache != null) {
            try {
                Element cachedElement = strCache.get(key);
                if (cachedElement != null) {
                    cached = (STRLog)cachedElement.getValue();
                }
            } catch (CacheException ignored) {
                // Cached stays null
            }
        }
        // We need to get an updated value if the new record
        // for the same set of domains is built at a later time.
        Session session = null;
        try {
            session = sf.openSession();
            // See if the cached result is still current; return it if it is.
            if (cached != null) {
                Query check = session.createQuery(
                    "select count(*) from STRLog s "
                +   "where s.domains=:domains"
                +   "  and s.version=:version"
                +   "  and s.buildTime=:buildTime");
                check.setParameter("domains", domains, StringArrayAsString.TYPE);
                check.setParameter("version", cached.getVersion());
                check.setParameter("buildTime", cached.getBuildTime(), DateToLongUserType.TYPE);
                if ((Integer)check.uniqueResult() == 1) {
                    return cached;
                }
            }
            // Query the new data, and cache the result.
            Query query = session.createQuery("from STRLog s where s.domains=:domains");
            query.setParameter("domains", domains, StringArrayAsString.TYPE);
            STRLog res = (STRLog)query.uniqueResult();
            if (strCache != null) {
                strCache.put(new Element(key, res));
            }
            return res;
        } catch (HibernateException e) {
            throw new RuntimeException("Error getting resolution map version", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (HibernateException e) {
                    throw new RuntimeException("Error closing session", e);
                }
            }
        }
    }

    private void updateStrLog(STRLog logToUpdate) {
        Session session = null;
        Transaction tx = null;

        try {
            session = sf.openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(logToUpdate);
            try {
                tx.commit();
            } catch (HibernateException he) {
                tx.rollback();
            }
        } catch (Exception e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (HibernateException he) {
                    log.error("Failed to rollback STRLog", he);
                }
            }
            log.error("Exception while updating STRLog", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (HibernateException he) {
                    log.error("Failed to finally close STRLog");
                }
            }
        }
        return;
    }

    /**
     * @see IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }

    /**
     * @see IManagerEnabled#setManager(IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see IInitializable#init()
     */
    public void init() {
        manager.registerComponent(ServerSpecManager.COMP_INFO, true);
        lm = (LifecycleManager) manager.getComponent(LifecycleManager.COMP_INFO);
        sf = (SessionFactory) manager.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
        dictionary = (IDictionary) manager.getComponent(Dictionary.COMP_INFO);

        // Setup EHCache
        CacheManager cacheManager;
        try {
            cacheManager = CacheManager.create();
            final String cacheName = getClass().getName();
            strCache = cacheManager.getCache(cacheName);
            if (strCache == null) {
                strCache = new Cache(cacheName, 8, false, true, 0, 0);
                cacheManager.addCache(strCache);
            }
        } catch (CacheException e) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Unable to create reverse DNS cache, proceeding in uncached mode.", e);
            }
            strCache = null;
            // no big deal, no caching
            return;
        }
    }

    private class MapBuildManager {

        private final Map<String,MapBuild> currentBuilds = new HashMap<String,MapBuild>();

        /**
         * @param domains
         * @param now
         * @return
         */
        public MapBuild getMapBuild(String[] domains, Date now) {
            MapBuild buildToReturn = null;

            final String buildKey = MapBuildKeyGenerator.buildKey(domains);

            synchronized (currentBuilds) {
                buildToReturn = currentBuilds.get(buildKey);
                if (buildToReturn == null) {
                    buildToReturn = new MapBuild(domains, now);
                    currentBuilds.put(buildKey, buildToReturn);
                    buildToReturn.start(new MapBuildCallback() {

                        public void buildComplete() {
                            synchronized (currentBuilds) {
                                currentBuilds.remove(buildKey);
                            }
                        }
                    });
                }
            }

            return buildToReturn;
        }

    }

    /**
     * A Map build callback interface.
     * 
     * @author sgoldstein
     */
    private interface MapBuildCallback {

        void buildComplete();
    }

    private class MapBuild {

        private Thread buildThread;
        private String[] domains;
        private Date asOf;

        /**
         * Create an instance of MapBuild
         * 
         * @param domains
         * @param asOf
         */
        public MapBuild(String[] domains, Date asOf) {
            this.domains = domains;
            this.asOf = asOf;
        }

        /**
         * Start the map build
         * 
         * @param callback
         *            a callback which will be notified when the build is
         *            complete
         */
        private void start(final MapBuildCallback callback) {
            boolean mapBuilderThreadStarted = false;
            try {
                STRLog strLog = getSTRLog(domains);
                if (strLog == null) {
                    strLog = new STRLog(domains, UnmodifiableDate.START_OF_TIME);
                    // We need a persistent record later if we need to build maps
                    // (i.e. pass it to the MapBuilder)
                    updateStrLog(strLog);
                }

                Date buildTime = strLog.getBuildTime();
              
                try {
                    Date lastPolicyDeployment = lm.getLatestDeploymentTime(buildTime, asOf, DeploymentType.PRODUCTION);
                    Date latestDictionaryConsistentTime = dictionary.getLatestConsistentTime();
                    Date lastChange = lastPolicyDeployment.after(latestDictionaryConsistentTime) ? lastPolicyDeployment : latestDictionaryConsistentTime;

                    /*
                     * This check is necessary because getLatestDeploymentTime in
                     * LifecycleManager uses "d.timeRelation.activeFrom > :since"
                     * condition. Also, is there hasn't been an enrollment yet, the
                     * latest dictionary consistent time will also be START_OF_TIME
                     */
                    if (lastChange.equals(UnmodifiableDate.START_OF_TIME)) {
                        lastChange = buildTime;
                    }

                    if (!lastChange.after(buildTime)) {
                        // We're done
                        callback.buildComplete();
                    } else {
                        // have updates or directory changes, need to rebuild
                        // everything
                        try {
                            Collection<? extends IHasPQL> deployedEntities = lm.getAllDeployedEntities(lastChange, DeploymentType.PRODUCTION);
                            final MapBuilder builder = new MapBuilder(deployedEntities, strLog);
                            final STRLog logToUpdate = strLog;
                            logToUpdate.setBuildTime(lastChange);
                            buildThread = new Thread() {

                                public void run() {
                                    try {
                                        builder.run();
                                    
                                        if (builder.runWasSuccessful()) {
                                            updateStrLog(logToUpdate);
                                        } else {
                                            throw builder.getFailureException();
                                        }
                                    } catch (Throwable problem) {
                                        log.error("Encountered a problem when preparing policies for deployment.", problem);
                                        sendDeploymentErrorMessage(problem);
                                    } finally {
                                        callback.buildComplete();
                                    }
                                }

                            };
                            buildThread.setName(MAP_BUILDER_THREAD_NAME + lastChange);
                            buildThread.start();
                            mapBuilderThreadStarted = true;
                        } catch (EntityManagementException exception) {
                            log.error("Failed to load deployment entities while building resolution map.  Bundles may be stale", exception);
                        }
                    }
                } catch (EntityManagementException e) {
                    log.error("Failed to obtain last deployment time while building resolution map.  Bundles may be stale", e);
                } catch (DictionaryException exception) {
                    log.error("Failed to last consistent time from diction while building resolution map.  Bundles may be stale", exception);
                }
            } finally {
                 /*  The  callback.buildComplete has to be called - either from the builder thread
                      or from here. But it must not be called from here if the builder thread has
                     successfully started and is working at this point. This makes sure that if 
                     builder thread somehow fails to start, we call it here
                 */
                if (!mapBuilderThreadStarted)
                    callback.buildComplete();
            }
        }

        private static final String DEPLOYMENT_ERROR_MESSAGE = "Control Center has encountered a problem during policy deployement.  Details on the error appear below.  Please consult the Policy Server documentation for suggestions on how to proceed.\n\n";

        private void sendDeploymentErrorMessage(Throwable exception) {
            IMessageHandlerManager messageHandlerManager = manager.getComponent(MessageHandlerManagerImpl.class);
            IMessageHandler messageHandler = messageHandlerManager.getMessageHandler(EmailMessageHandler.DEFAULT_HANDLER_NAME);

            if (messageHandler == null) {
                return;
            }
            
            String subject = "Error Notification: Problem with deployment";

            StringWriter body = new StringWriter();
            body.append(DEPLOYMENT_ERROR_MESSAGE);
            if (exception != null) {
                exception.printStackTrace(new PrintWriter(body));
            }

            StringMessage emailMessage = new StringMessage(subject, body.toString());

            try {
                messageHandler.sendMessage(emailMessage, null);
            } catch (MessagingException me) {
                log.error("Unable to send message with subject " + subject + "\n" + me);
            }
        }

        private void waitForBuildToComplete(long maxWaitTime) throws InterruptedException {
            // FIXME - What happens with maxWaitTime is reached? How does it
            // affect the client?
            if (buildThread != null) {
                buildThread.join(maxWaitTime);
            }
        }
    }

    /**
     * This class is used for namespace separation.
     * 
     * @author sgoldstein
     */
    private static class MapBuildKeyGenerator {

        private static String buildKey(String[] domains) {
            if (domains == null) {
                throw new NullPointerException("domains cannot be null.");
            }

            String[] caseInsensitiveDomains = new String[domains.length];
            for (int i = 0; i != domains.length; i++) {
                if (domains[i] == null) {
                    throw new NullPointerException("domains[" + i + "]");
                }
                caseInsensitiveDomains[i] = domains[i].toLowerCase();
            }
            Arrays.sort(domains);

            StringBuffer keyBuffer = new StringBuffer();
            for (int i = 0; i < domains.length; i++) {
                if (i != 0) {
                    keyBuffer.append(";");
                }
                keyBuffer.append(caseInsensitiveDomains[i].toLowerCase());
            }

            // Add an ending semi-colon for the empty domain array case
            keyBuffer.append(";");

            return keyBuffer.toString();
        }
    }

}
