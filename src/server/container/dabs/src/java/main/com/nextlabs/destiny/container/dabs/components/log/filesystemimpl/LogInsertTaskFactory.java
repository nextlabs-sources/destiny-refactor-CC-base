/*
 * Created on May 30, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log.filesystemimpl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.destiny.container.dabs.components.log.ILogQueueMgr;
import com.nextlabs.destiny.container.dabs.components.log.ITaskFactory;
import com.nextlabs.destiny.container.dabs.components.log.LogInsertTaskObserver;
import com.nextlabs.destiny.container.dabs.components.log.LogQueueException;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/container/dabs/components/log/filesystemimpl/LogInsertTaskFactory.java#1 $
 */
public class LogInsertTaskFactory implements ITaskFactory{
    //parameters
    public static final PropertyKey<ILogWriter> PERSISTENCE_LOG_WRITER_PARAM = new PropertyKey<ILogWriter>("PersistenceLogWriter");
 
    public static final PropertyKey<Integer> LOG_INSERT_TASK_IDLE_TIME = new PropertyKey<Integer>("LogInsertTaskIdleTime");
    public static final int DEFUALT_LOG_INSERT_TASK_IDLE_TIME = 120000; 
    
    private final int logInsertTaskIdleTime;
    private final ILogQueueMgr logQueueMgr;
    private final ILogWriter persistenceLogWriter;
    private static final Log LOG = LogFactory.getLog(LogInsertTaskFactory.class);
    
    public LogInsertTaskFactory(ILogWriter persistenceLogWriter, ILogQueueMgr logQueueMgr){
        this(persistenceLogWriter, logQueueMgr, DEFUALT_LOG_INSERT_TASK_IDLE_TIME);
    }
    
    public LogInsertTaskFactory(ILogWriter persistenceLogWriter, ILogQueueMgr logQueueMgr, int logInsertTaskIdleTime){
        this.persistenceLogWriter = persistenceLogWriter;
        this.logQueueMgr = logQueueMgr;
        this.logInsertTaskIdleTime = logInsertTaskIdleTime;
    }

    public Runnable getTask(){
        return new LogInsertTask(logInsertTaskIdleTime);
    }
    
    /**
     * 
     * @param idleTime must be non-negative, zero means wait forever
     * @return
     */
    public Runnable getTask(int idleTime){
        return new LogInsertTask(idleTime);
    }
    

    class LogInsertTask implements Runnable, LogInsertTaskObserver {
        private final int idle;
     
        LogInsertTask(int idle){
            this.idle = idle;
        }
     
        public void run() {
            while (true) {
                try {
                    Map<Long, BaseLogEntry[]> retrievedLogs;
                    try {
                        retrievedLogs = logQueueMgr.getLogsToWrite();
                    } catch (LogQueueException e) {
                        //must be FileSystemLogQueueMgr is stopped
                        return;
                    }
                    if (retrievedLogs != null) {
                        writeLogs(retrievedLogs);
                    } else {
                        LOG.trace("No logs to write to persistence from the file system at this time");
                        boolean isTimeout;
                        logQueueMgr.addLogInsertTaskObserver(this);
                        synchronized (this) {
                            try {
                                long sleepBeginTime = System.currentTimeMillis();
                                this.wait(idle);
                                isTimeout = (idle > 0 && idle < System.currentTimeMillis() - sleepBeginTime);
                            } catch (InterruptedException e) {
                                isTimeout = true;
                            }
                        }
                        logQueueMgr.removeLogInsertTaskObserver(this);
                        if (isTimeout) {
                            return;
                        }
                    }
                } catch (LogQueueException e) {
                    LOG.error("Error retrieving logs to write", e);
                } catch (DataSourceException e) {
                    LOG.error("Could not write logs into persistence", e);
                }
            }
        }
        
        void writeLogs(Map<Long, BaseLogEntry[]> retrievedLogs) throws DataSourceException, LogQueueException{
            BaseLogEntry[] logsToWrite = null;
            List<Long> logFileIds = new LinkedList<Long>();
            for (Map.Entry<Long, BaseLogEntry[]> entry : retrievedLogs.entrySet()) {
                BaseLogEntry[] logEntries = entry.getValue();
                logFileIds.add(entry.getKey());

                if (logsToWrite != null) {
                    BaseLogEntry[] tempArray;
                    int length = logsToWrite.length + logEntries.length;
                    if (logsToWrite[0] instanceof TrackingLogEntry) {
                        tempArray = new TrackingLogEntry[length];
                    } else if (logsToWrite[0] instanceof TrackingLogEntryV2) {
                        tempArray = new TrackingLogEntryV2[length];
                    } else if (logsToWrite[0] instanceof TrackingLogEntryV3) {
                        tempArray = new TrackingLogEntryV3[length];
                    } else if (logsToWrite[0] instanceof PolicyAssistantLogEntry) {
                        tempArray = new PolicyAssistantLogEntry[length];
                    } else if (logsToWrite[0] instanceof PolicyActivityLogEntry) {
                        tempArray = new PolicyActivityLogEntry[length];
                    } else if (logsToWrite[0] instanceof PolicyActivityLogEntryV2) {
                        tempArray = new PolicyActivityLogEntryV2[length];
                    } else if (logsToWrite[0] instanceof PolicyActivityLogEntryV3) {
                        tempArray = new PolicyActivityLogEntryV3[length];
                    } else if (logsToWrite[0] instanceof PolicyActivityLogEntryV4) {
                        tempArray = new PolicyActivityLogEntryV4[length];
                    } else if (logsToWrite[0] instanceof PolicyActivityLogEntryV5) {
                        tempArray = new PolicyActivityLogEntryV5[length];
                    } else {
                        LOG.error("BaseLogEntry not one of Tracking (v1/v2/v3), Activity (v1/v2/v3/v4/v5), or Assistant log");
                        tempArray = null;
                    }
                    System.arraycopy(logsToWrite, 0, tempArray, 0, logsToWrite.length);
                    System.arraycopy(logEntries, 0, tempArray, logsToWrite.length, logEntries.length);
                    logsToWrite = tempArray;
                } else {
                    logsToWrite = logEntries;
                }
            }

            if (logsToWrite != null) {
                if (logsToWrite[0] instanceof TrackingLogEntry) {
                    persistenceLogWriter.log((TrackingLogEntry[]) logsToWrite);
                } else if (logsToWrite[0] instanceof TrackingLogEntryV2) {
                    persistenceLogWriter.log((TrackingLogEntryV2[]) logsToWrite);
                } else if (logsToWrite[0] instanceof TrackingLogEntryV3) {
                    persistenceLogWriter.log((TrackingLogEntryV3[]) logsToWrite);
                } else if (logsToWrite[0] instanceof PolicyAssistantLogEntry) {
                    persistenceLogWriter.log((PolicyAssistantLogEntry[]) logsToWrite);
                } else if (logsToWrite[0] instanceof PolicyActivityLogEntry) {
                    persistenceLogWriter.log((PolicyActivityLogEntry[]) logsToWrite);
                } else if (logsToWrite[0] instanceof PolicyActivityLogEntryV2) {
                    persistenceLogWriter.log((PolicyActivityLogEntryV2[]) logsToWrite);
                } else if (logsToWrite[0] instanceof PolicyActivityLogEntryV3) {
                    persistenceLogWriter.log((PolicyActivityLogEntryV3[]) logsToWrite);
                } else if (logsToWrite[0] instanceof PolicyActivityLogEntryV4) {
                    persistenceLogWriter.log((PolicyActivityLogEntryV4[]) logsToWrite);
                } else if (logsToWrite[0] instanceof PolicyActivityLogEntryV5) {
                    persistenceLogWriter.log((PolicyActivityLogEntryV5[]) logsToWrite);
                } else {
                    LOG.error("logsToWrite not one of Tracking (v1/v2), Activity (v1/v2/v3/v4/v5), or Assistant log");
                }
                logQueueMgr.dequeueLogs(logFileIds.toArray(new Long[] {}));
            }
        }
    }
}
