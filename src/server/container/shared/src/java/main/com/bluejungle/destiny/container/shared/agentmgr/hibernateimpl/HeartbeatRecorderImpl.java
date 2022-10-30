/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.usertypes.CalendarToLongUserType;

/**
 * A concrete implementation of the IHeartbeatRecorder interface which records
 * each heartbeat into the database. To protect against infinite table size,
 * heartbeat records are deleted at regular configurable intervals. Heartbeat
 * records are also stored at regular configurable intervals to prevent database
 * access on every heartbeat. <br />
 * <br />
 * Note that this class should always be used as a Singleton to prevent having
 * multiple threads performing the record cleanup
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/HeartbeatRecorderImpl.java#2 $
 */
public class HeartbeatRecorderImpl implements IHeartbeatRecorder, IInitializable, ILogEnabled, IConfigurable, IStartable {

    public static final ComponentInfo<IHeartbeatRecorder> COMP_INFO = new ComponentInfo<IHeartbeatRecorder>(IHeartbeatRecorder.COMP_NAME,
                                                                                                            HeartbeatRecorderImpl.class.getName(), 
                                                                                                            IHeartbeatRecorder.class.getName(),
                                                                                                            LifestyleType.SINGLETON_TYPE);
    /**
     * Configuration property for heartbeat record store process time interval,
     * the period at which a process is run to store heartbeats records
     */
    public static final String RECORD_PROCESS_TIME_INTERVAL_PROPERTY_NAME = "RecordProcessTimeInterval";

    /**
     * Configuration property for max heartbeat record buffer size, the size at
     * which the store process is run regardless of the store process time
     * interval
     */
    public static final String MAX_RECORD_BUFFER_SIZE_PROPERTY_NAME = "RecordMaxBufferSizeTimeInterval";

    /**
     * Configuration property for hibernate recrod cleanup interval, the period
     * at which a process is run to delete heartbeats records over a day old
     */
    public static final String RECORD_CLEANUP_TIME_INTERVAL_PROPERTY_NAME = "RecordCleanupTimeInterval";

    /**
     * Configuraton property for the time at which heartbeat records are
     * available for cleanup (deletion)
     */
    public static final String RECORD_EXPIRATION_TIME_PROPERTY_NAME = "RecordExpirationPropertyName";

    /**
     * Default heartbeat cleanup interval - 2 days
     */
    public static final Long DEFAULT_RECORD_CLEANUP_TIME_INTERVAL = new Long(2 * 24 * 60 * 60 * 1000);

    /**
     * Defaul max heartbeat record buffer size
     */
    public static final Long DEFAULT_MAX_RECORD_QUEUE_SIZE = new Long(400);

    /**
     * Default heartbeat record store interval - 30 seconds
     */
    public static final Long DEFAULT_RECORD_PROCESS_TIME_INTERVAL = new Long(30 * 1000);

    /**
     * Default heartbeat record expiration time - 36 hours
     */
    public static final Long DEFAULT_RECORD_EXPIRATION_TIME = new Long(36 * 60 * 60 * 1000);

    private static final int HIBERNATE_SESSION_FLUSH_THRESHOLD = 20;
    private static final String LAST_DAY_QUERY_PARAM_NAME = "lastDay";
    private static final String HEARTBEAT_COUNT_QUERY = "select count(*) from HeartbeatRecordDO where timestamp > :" + LAST_DAY_QUERY_PARAM_NAME;
    private static final String HEARTBEAT_DELETE_QUERY = "from HeartbeatRecordDO where timestamp < :" + LAST_DAY_QUERY_PARAM_NAME;

    private static final Object RECORD_QUEUE_LOCK = new Object();
    private final List<HeartbeatRecordDO> recordQueue = Collections.synchronizedList(new LinkedList<HeartbeatRecordDO>());
    private Log log;
    private IConfiguration config;

    private Long recordCleanupTimeInterval;
    private Long recordStoreTimeInterval;
    private Long recordMaxBufferSize;
    private Long recordExpirationTime;
    private Timer recordCleanupTimer;
    private Thread recordingThread;

    /**
     * Create an instance of HeartbeatRecorderImpl
     * 
     */
    public HeartbeatRecorderImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IHeartbeatRecorder#recordHeartbeat(IAgentHeartbeatData)
     */
    public void recordHeartbeat(IAgentHeartbeatData heartbeat) {
        enqueueRecord(heartbeat);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.IHeartbeatRecorder#getNumHeartbeatsInLastDay()
     */
    public long getNumHeartbeatsSinceTime(Calendar time) throws PersistenceException {
        if (time == null) {
            throw new NullPointerException("time cannot be null.");
        }

        if (time.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() - recordExpirationTime.longValue()) {
            getLog().warn("Record count begin time is prior to last record expiration time.  Results may not be accurate.");
        }

        long heartbeatsToReturn = 0;
        Session hSession = null;
        try {
            hSession = getSession();
            Query hqlQuery = hSession.createQuery(HEARTBEAT_COUNT_QUERY);
            hqlQuery.setParameter(LAST_DAY_QUERY_PARAM_NAME, time, CalendarToLongUserType.TYPE);

            Iterator<Integer> results = hqlQuery.iterate();

            // We may lose some precision here, but not much we can do.
            // Hibernate returns count as an Integer. We should be okay given
            // the expected amounts
            heartbeatsToReturn = results.next().intValue();
        } catch (HibernateException exception) {
            throw new PersistenceException(exception);
        } finally {
            HibernateUtils.closeSession(hSession, getLog());
        }

        return heartbeatsToReturn;
    }

    /**
     * Enqueue a heartbeat record to be stored.
     * 
     * @param heartbeat
     *            the heartbeat to record
     */
    private void enqueueRecord(IAgentHeartbeatData heartbeat) {
        recordQueue.add(new HeartbeatRecordDO());

        /*
         * We don't want to lock every time because we don't want to prevent
         * heart beats while storing record.
         * 
         * Double locking scheme here is okay because: 1. Accuracy isn't
         * essential 2. Assignment isn't taking place, therefore there's no way
         * to access uninitialized data
         */
        if (recordQueue.size() >= recordMaxBufferSize.longValue()) {
            synchronized (RECORD_QUEUE_LOCK) {
                if (recordQueue.size() >= recordMaxBufferSize.longValue()) {
                    RECORD_QUEUE_LOCK.notify();
                }
            }
        }
    }

    /**
     * Retrieve a Hibernate Session
     * 
     * @return a Hibernate Session
     */
    private Session getSession() throws HibernateException {
        IHibernateRepository dataSource = this.getDataSource();
        return dataSource.getSession();
    }

    /**
     * Returns a data source object that can be used to create sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for ProfileManager.");
        }

        return dataSource;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration myConfig = getConfiguration();
        if (myConfig != null) {
            this.recordCleanupTimeInterval = (Long) myConfig.get(RECORD_CLEANUP_TIME_INTERVAL_PROPERTY_NAME, DEFAULT_RECORD_CLEANUP_TIME_INTERVAL);
            this.recordStoreTimeInterval = (Long) myConfig.get(RECORD_PROCESS_TIME_INTERVAL_PROPERTY_NAME, DEFAULT_RECORD_PROCESS_TIME_INTERVAL);
            this.recordMaxBufferSize = (Long) myConfig.get(MAX_RECORD_BUFFER_SIZE_PROPERTY_NAME, DEFAULT_MAX_RECORD_QUEUE_SIZE);
            this.recordExpirationTime = (Long) myConfig.get(RECORD_EXPIRATION_TIME_PROPERTY_NAME, DEFAULT_RECORD_EXPIRATION_TIME);
        } else {
            this.recordCleanupTimeInterval = DEFAULT_RECORD_CLEANUP_TIME_INTERVAL;
            this.recordStoreTimeInterval = DEFAULT_RECORD_PROCESS_TIME_INTERVAL;
            this.recordMaxBufferSize = DEFAULT_MAX_RECORD_QUEUE_SIZE;
            this.recordExpirationTime = DEFAULT_RECORD_EXPIRATION_TIME;
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
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
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {
        this.recordCleanupTimer = new Timer("HeartbeatRecordCleaner", true);
        TimerTask recordDeletionTask = new DeletionTask();
        recordCleanupTimer.schedule(recordDeletionTask, 0, this.recordCleanupTimeInterval.longValue());

        this.recordingThread = new Thread(new RecordingRunnable(), "HeartbeatRecorder");
        recordingThread.setDaemon(true);
        recordingThread.start();
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {
        this.recordCleanupTimer.cancel();
        this.recordingThread.interrupt();
    }

    /**
     * A Runnable which persists heart records at regular intervals
     * 
     * @author sgoldstein
     */
    private class RecordingRunnable implements Runnable {

        public void run() {
            synchronized (RECORD_QUEUE_LOCK) {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        while (HeartbeatRecorderImpl.this.recordQueue.size() <= 0) {
                            RECORD_QUEUE_LOCK.wait(recordStoreTimeInterval.longValue());
                        }

                        insertHeartbeatRecords();
                    }
                } catch (InterruptedException exception) {
                    /*
                     * Make sure we insert heartbeat records even when
                     * interrupted
                     */
                    if (HeartbeatRecorderImpl.this.recordQueue.size() > 0) {
                        insertHeartbeatRecords();
                    }
                }
            }
        }

        /**
         * Insert heart beat records into the persistent store
         */
        private void insertHeartbeatRecords() {
            List<HeartbeatRecordDO> recordToInsert = new LinkedList<HeartbeatRecordDO>(recordQueue);

            Session hSession = null;
            Transaction transaction = null;

            try {
                hSession = getSession();
                transaction = hSession.beginTransaction();

                ListIterator<HeartbeatRecordDO> recordIterator = recordToInsert.listIterator();
                while (recordIterator.hasNext()) {
                    HeartbeatRecordDO heartBeatRecord = recordIterator.next();
                    hSession.save(heartBeatRecord);

                    /*
                     * Ideally, we could just persist the data in the log entry.
                     * Unfortunately, Hibernate can't do this. We need an
                     * implementation class
                     */
                    if (recordIterator.previousIndex() + 1 % HIBERNATE_SESSION_FLUSH_THRESHOLD == 0) {
                        /*
                         * Flush the Hibernate session. Avoids
                         * OutOfMemoryExceptions
                         */
                        hSession.flush();
                        hSession.clear();
                    }
                }

                transaction.commit();

                recordQueue.removeAll(recordToInsert);
            } catch (HibernateException exception) {
                HibernateUtils.rollbackTransation(transaction, HeartbeatRecorderImpl.this.getLog());
            } finally {
                HibernateUtils.closeSession(hSession, HeartbeatRecorderImpl.this.getLog());
            }
        }
    }

    /**
     * A Runnable which deletes heartbeat records at regular intervals
     * 
     * @author sgoldstein
     */
    private class DeletionTask extends TimerTask {

        /**
         * @see java.util.TimerTask#run()
         */
        public void run() {
            Session hSession = null;
            Transaction transaction = null;
            try {
                hSession = getSession();
                Iterator<HeartbeatRecordDO> heartbeatRecordsToDelete = retrieveHeartbeatRecordsToDelete();
                transaction = hSession.beginTransaction();
                while (heartbeatRecordsToDelete.hasNext()) {
                    hSession.delete(heartbeatRecordsToDelete.next());
                }
                transaction.commit();
            } catch (HibernateException exception) {
                getLog().warn("Failed to clean up heartbeat records.", exception);

                HibernateUtils.rollbackTransation(transaction, HeartbeatRecorderImpl.this.getLog());
            } finally {
                HibernateUtils.closeSession(hSession, HeartbeatRecorderImpl.this.getLog());
            }
        }

        /**
         * Retrieve heart beat records which are to be deleted
         * 
         * @return An iterator of HeartbeatRecordDO instances to be deleted
         * @throws HibernateException
         *             if a failure occurs while accessing the persistent store
         */
        private Iterator<HeartbeatRecordDO> retrieveHeartbeatRecordsToDelete() throws HibernateException {
            Iterator<HeartbeatRecordDO> valuesToReturn = null;

            Session hSession = getSession();
            try {
                Query hqlQuery = hSession.createQuery(HEARTBEAT_DELETE_QUERY);
                Calendar currentDate = Calendar.getInstance();

                // The calendar add method doesn't take a long. Doesn't make
                // much sense. But, the int value should be enough, given the
                // valuea involved here
                currentDate.add(Calendar.MILLISECOND, -HeartbeatRecorderImpl.this.recordExpirationTime.intValue());
                hqlQuery.setParameter(LAST_DAY_QUERY_PARAM_NAME, currentDate, CalendarToLongUserType.TYPE);

                List<HeartbeatRecordDO> retrievedItems = hqlQuery.list();

                // We need to clone the list in order to allow deletion of items
                valuesToReturn = new LinkedList<HeartbeatRecordDO>(retrievedItems).iterator();
            } finally {
                HibernateUtils.closeSession(hSession, HeartbeatRecorderImpl.this.getLog());
            }

            return valuesToReturn;
        }
    }
}
