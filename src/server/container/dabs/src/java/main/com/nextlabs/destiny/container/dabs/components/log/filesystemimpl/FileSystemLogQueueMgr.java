/*
 * Created on Feb 12, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log.filesystemimpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.utils.StringUtils;
import com.nextlabs.destiny.container.dabs.components.log.ITaskFactory;
import com.nextlabs.destiny.container.dabs.components.log.IThreadPool;
import com.nextlabs.destiny.container.dabs.components.log.ILogQueueMgr;
import com.nextlabs.destiny.container.dabs.components.log.LogInsertTaskObserver;
import com.nextlabs.destiny.container.dabs.components.log.LogQueueException;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;

/**
 * This is the log queue manager implementation class for a file system
 * log queue.  
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/container/dabs/components/log/filesystemimpl/FileSystemLogQueueMgr.java#1 $
 */
public class FileSystemLogQueueMgr implements ILogQueueMgr, ILogEnabled, IInitializable,
                                              IConfigurable, IStartable {
    private static final String POLICY_ASSISTANT_FILENAME_PREFIX = "pass_";
    private static final String POLICY_ACTIVITY_FILENAME_PREFIX = "pa_";
    private static final String POLICY_ACTIVITY_V2_FILENAME_PREFIX = "pa2_";
    private static final String POLICY_ACTIVITY_V3_FILENAME_PREFIX = "pa3_";
    private static final String POLICY_ACTIVITY_V4_FILENAME_PREFIX = "pa4_";
    private static final String POLICY_ACTIVITY_V5_FILENAME_PREFIX = "pa5_";
    private static final String TRACKING_ACTIVITY_FILENAME_PREFIX = "trk_";
    private static final String TRACKING_ACTIVITY_V2_FILENAME_PREFIX = "trk2_";
    private static final String TRACKING_ACTIVITY_V3_FILENAME_PREFIX = "trk3_";
    private static final String POLICY_ASSISTANT_LOG_TYPE = "PASS";
    private static final String POLICY_LOG_TYPE = "PA";
    private static final String POLICY_V2_LOG_TYPE = "PA2";
    private static final String POLICY_V3_LOG_TYPE = "PA3";
    private static final String POLICY_V4_LOG_TYPE = "PA4";
    private static final String POLICY_V5_LOG_TYPE = "PA5";
    private static final String TRACKING_LOG_TYPE = "TR";
    private static final String TRACKING_V2_LOG_TYPE = "TR2";
    private static final String TRACKING_V3_LOG_TYPE = "TR3";

    protected static final String LOG_FILE_PREFIX = "activity.log";

    protected static String LOG_DIR = "logqueue";

    private static final long TIMER_STARTUP_DELAY = 2000;

    /**
     * configuration parameters
     */
    public static final PropertyKey<String> LOG_QUEUE_LOCATION = new PropertyKey<String>("LogQueueLocation");
    public static final PropertyKey<Integer> TIMEOUT_CHECKER_FREQUENCY = new PropertyKey<Integer>("TimeoutCheckerFrequency");
    private static final int DEFAULT_TIMEOUT_CHECKER_FREQUENCY = 90000;

    public static final PropertyKey<Integer> LOG_TIMEOUT = new PropertyKey<Integer>("LogTimeout");
    public static final int DEFAULT_LOG_TIMEOUT = 60000;

    public static final PropertyKey<Long> MAX_BYTES_UPLOAD_SIZE_PARAM = new PropertyKey<Long>("maxBytesUploadSize");
    private static final long DEFAULT_MAX_BYTES_UPLOAD_SIZE = 1048576; //1MB

    public static final PropertyKey<Long> MAX_HANDLE_FILE_SIZE_PER_THREAD_PARAM = new PropertyKey<Long>("maxHandleFileSizePerThread");
    private static final long DEFAULT_MAX_HANDLE_FILE_SIZE_PER_THREAD = 10485760; //100MB

    public static final PropertyKey<IThreadPool> FILE_SYSTEM_THREAD_POOL_PARAM = new PropertyKey<IThreadPool>("fileSystemThreadPool");

    private static Long currentLogFileNum = new Long(0); // current log file number
    private static Long maxLogFileNum = new Long(1000000); // max log file number

    Map<Long, String> allLockedLogFiles = new ConcurrentHashMap<Long, String>();
    Map<Long, String> allUnlockedLogFiles = new ConcurrentHashMap<Long, String>();
    Queue<Long> unlockedLogFilesOrdering = new ConcurrentLinkedQueue<Long>();
    Map<Long, Long> timeoutTracker = new ConcurrentHashMap<Long, Long>();
    Collection<LogInsertTaskObserver> logInsertTaskObservers;


    //make sure the queue size is up to date and allow concurrent read
    protected AtomicLong queueSize = new AtomicLong(0L);
    protected IThreadPool fileSystemThreadPool;

    private Timer timeoutTimer;
    private IConfiguration configuration;
    private Log log;

    private long timerFrequency; // how often we check for timeout
    private long logUnlockTimeOut; // the actual timeout
    private long logSizeLimit; // the maximum size of logs to upload
    private long maxHandleFileSizePerThread;


    /**
     * @throws LogQueueException 
     * @see com.nextlabs.destiny.container.dabs.components.log.ILogQueueMgr#queueLogs(com.bluejungle.domain.log.BaseLogEntry[])
     */
    public Long queueLogs(BaseLogEntry[] logs) throws LogQueueException {
        if (!isRunningState) {
            throw new LogQueueException("FileSystemLogQueueMgr is not in running state.");
        }

        String filePrefix = null;
        if (logs[0] instanceof TrackingLogEntry) {
            filePrefix = TRACKING_ACTIVITY_FILENAME_PREFIX;
        } else if (logs[0] instanceof TrackingLogEntryV2) {
            filePrefix = TRACKING_ACTIVITY_V2_FILENAME_PREFIX;
        } else if (logs[0] instanceof TrackingLogEntryV3) {
            filePrefix = TRACKING_ACTIVITY_V3_FILENAME_PREFIX;
        } else if (logs[0] instanceof PolicyActivityLogEntry) {
            filePrefix = POLICY_ACTIVITY_FILENAME_PREFIX;
        } else if (logs[0] instanceof PolicyActivityLogEntryV2) {
            filePrefix = POLICY_ACTIVITY_V2_FILENAME_PREFIX;
        } else if (logs[0] instanceof PolicyActivityLogEntryV3) {
            filePrefix = POLICY_ACTIVITY_V3_FILENAME_PREFIX;
        } else if (logs[0] instanceof PolicyActivityLogEntryV4) {
            filePrefix = POLICY_ACTIVITY_V4_FILENAME_PREFIX;
        } else if (logs[0] instanceof PolicyActivityLogEntryV5) {
            filePrefix = POLICY_ACTIVITY_V5_FILENAME_PREFIX;
        } else if (logs[0] instanceof PolicyAssistantLogEntry) {
            filePrefix = POLICY_ASSISTANT_FILENAME_PREFIX;
        } else {
            throw new LogQueueException("Unknown log type.  Not one of TrackingLogEntry(V2/V3), PolicyActivityLogEntry(V2/V3/V4/v5), PolicyAssistantLogEntry");
        }

        boolean isFileExist;

        long logFileNum;
        File logFile;
        do{
            //logFileNum is guarantee unique in multi-thread env
            logFileNum = getCurrentLogFileNum();
            logFile = new File(LOG_DIR, filePrefix + LOG_FILE_PREFIX + logFileNum);

            isFileExist = logFile.exists();
            if(isFileExist){
                this.getLog().error(
                    "Log file already exists in the log queue: " + logFile.getAbsolutePath()
                + " , will attempt to create a different log file");
            }

        }while (isFileExist); // log file already exists

        try {
            ObjectOutputStream logOutputStream = new ObjectOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(logFile, false)));
            logOutputStream.writeObject(logs);
            logOutputStream.flush();
            logOutputStream.reset();
            logOutputStream.close();

            addLogFile(new Long(logFileNum), logFile.getName());

            addFileSize(logFile.length());

            wakeUpSleepingThread();
        } catch (IOException e) {
            this.getLog().error("Unable to create log file: " + logFile.getAbsolutePath(), e);
            throw new LogQueueException(e);
        }

        return new Long(logFileNum);
    }

    private void wakeUpSleepingThread() {
        int numOfNeededThreads = (int) (queueSize.longValue() / maxHandleFileSizePerThread) + 1;

        Iterator<LogInsertTaskObserver> iterator = logInsertTaskObservers.iterator();
        for (int i = 0; iterator.hasNext() && i < numOfNeededThreads; i++) {
            LogInsertTaskObserver observer = iterator.next();
            synchronized (observer) {
                observer.notify();
            }
        }

    }

    private void addFileSize(long size) {
        synchronized (queueSize) {
            queueSize.addAndGet(size);

            if (maxHandleFileSizePerThread * fileSystemThreadPool.getPoolSize() < queueSize.longValue()) {
                if (fileSystemThreadPool.addTask()) {
                    getLog().trace("create new threads, queueSize=" + queueSize
                                   + ", maxHandleFileSizePerThread=" + maxHandleFileSizePerThread);
                }
            }
        }
    }

    /**
     * 
     * @throws LogQueueException 
     * @see com.nextlabs.destiny.container.dabs.components.log.ILogQueueMgr#dequeueLogs(java.lang.Long)
     */
    public boolean dequeueLogs(Long... logGroupIds) throws LogQueueException {
        List<File> deleteFiles = new LinkedList<File>();

        for(Long logGroupId : logGroupIds){
            String fileName = allLockedLogFiles.get(logGroupId);
            // if the LogTimeoutTask moved the logGroupId to allUnlockedLogFiles
            // we just dequeue from it also
            if (fileName == null) {
            	fileName = allUnlockedLogFiles.get(logGroupId);
            }
            if (fileName == null) {
                String errorMessage = "The log file with ID " + logGroupId
                                      + " is not locked, which means it either does not exist, or has not been persisted";
                this.getLog().error(errorMessage);
                throw new LogQueueException(errorMessage);
            }

            File logFile = new File(LOG_DIR, fileName);
            if (!(logFile.exists() && logFile.isFile())) {
                this.getLog().error(
                    "The log file " + logFile.getAbsolutePath()
                + " either does not exist or is not a file");
                throw new LogQueueException("The log file " + logFile.getAbsolutePath()
                                            + " either does not exist or is not a file");
            }

            deleteFiles.add(logFile);

            allLockedLogFiles.remove(logGroupId);
            allUnlockedLogFiles.remove(logGroupId);
            unlockedLogFilesOrdering.remove(logGroupId);
        }

        for (File deleteFile : deleteFiles) {
            queueSize.addAndGet(-deleteFile.length());
            deleteFile.delete();
        }
        return true;
    }

    /**
     * @throws LogQueueException 
     * @see com.nextlabs.destiny.container.dabs.components.log.ILogQueueMgr#getLogsToWrite()
     */
    public Map<Long, BaseLogEntry[]> getLogsToWrite() throws LogQueueException {
        if (!isRunningState) {
            throw new LogQueueException("FileSystemLogQueueMgr is not in running state.");
        }

        Map<Long, BaseLogEntry[]> result = getNextLogFilesToWrite();
        if (result != null) {
            for (Long logFileId : result.keySet()) {
                timeoutTracker.put(logFileId, new Long(System.currentTimeMillis()));
            }
        }
        return result;
    }

    /**
     * Returns the currentLogFileNum.
     * @return the currentLogFileNum.
     */
    private long getCurrentLogFileNum() {
        synchronized (currentLogFileNum) {
            long result = currentLogFileNum;
            if (currentLogFileNum >= maxLogFileNum) {
                currentLogFileNum = new Long(0);
            } else {
                currentLogFileNum++;
            }
            return result;
        }
    }

    private void addLogFile(Long logFileId, String fileName) {
        this.allUnlockedLogFiles.put(logFileId, fileName);
        this.unlockedLogFilesOrdering.add(logFileId);
    }



    private Map<Long, BaseLogEntry[]> getNextLogFilesToWrite() throws LogQueueException {
        if (allUnlockedLogFiles.isEmpty()) {
            return null;
        }

        Map<Long, BaseLogEntry[]> result = new HashMap<Long, BaseLogEntry[]>();

        long currentLogSize = 0;
        String logType = null;
        boolean firstLog = true;
        for (Long logFileId : unlockedLogFilesOrdering) {
            if (!isRunningState) {
                return result;
            }
            if (currentLogSize < logSizeLimit) {
                String fileName;
                synchronized (allUnlockedLogFiles) {
                    synchronized (allLockedLogFiles) {
                        if (!allLockedLogFiles.containsKey(logFileId) && allUnlockedLogFiles.containsKey(logFileId)) {
                            fileName = allUnlockedLogFiles.get(logFileId);

                            if (logType == null) {
                                if (fileName.startsWith(TRACKING_ACTIVITY_V2_FILENAME_PREFIX)) {
                                    logType = TRACKING_V2_LOG_TYPE;
                                } else if (fileName.startsWith(TRACKING_ACTIVITY_V3_FILENAME_PREFIX)) {
                                    logType = TRACKING_V3_LOG_TYPE;
                                } else if (fileName.startsWith(TRACKING_ACTIVITY_FILENAME_PREFIX)) {
                                    logType = TRACKING_LOG_TYPE;
                                } else if (fileName.startsWith(POLICY_ACTIVITY_V5_FILENAME_PREFIX)) {
                                    logType = POLICY_V5_LOG_TYPE;
                                } else if (fileName.startsWith(POLICY_ACTIVITY_V4_FILENAME_PREFIX)) {
                                    logType = POLICY_V4_LOG_TYPE;
                                } else if (fileName.startsWith(POLICY_ACTIVITY_V3_FILENAME_PREFIX)) {
                                    logType = POLICY_V3_LOG_TYPE;
                                } else if (fileName.startsWith(POLICY_ACTIVITY_V2_FILENAME_PREFIX)) {
                                    logType = POLICY_V2_LOG_TYPE;
                                } else if (fileName.startsWith(POLICY_ACTIVITY_FILENAME_PREFIX)) {
                                    logType = POLICY_LOG_TYPE;
                                } else if (fileName.startsWith(POLICY_ASSISTANT_FILENAME_PREFIX)) {
                                    logType = POLICY_ASSISTANT_LOG_TYPE;
                                } else {
                                    throw new LogQueueException("File " + fileName + " does not start with one of " +
                                                                StringUtils.join(new String[] { TRACKING_ACTIVITY_FILENAME_PREFIX,
                                                                                                TRACKING_ACTIVITY_V2_FILENAME_PREFIX,
                                                                                                TRACKING_ACTIVITY_V3_FILENAME_PREFIX,
                                                                                                POLICY_ACTIVITY_FILENAME_PREFIX,
                                                                                                POLICY_ACTIVITY_V2_FILENAME_PREFIX,
                                                                                                POLICY_ACTIVITY_V3_FILENAME_PREFIX,
                                                                                                POLICY_ACTIVITY_V4_FILENAME_PREFIX,
                                                                                                POLICY_ACTIVITY_V5_FILENAME_PREFIX,
                                                                                                POLICY_ASSISTANT_FILENAME_PREFIX }, ", ")
                                                                + ". Unknown file type");
                                }
                                firstLog = true;
                            }

                            if (   (logType.equals(TRACKING_LOG_TYPE) && !fileName.startsWith(TRACKING_ACTIVITY_FILENAME_PREFIX))
                                   || (logType.equals(TRACKING_V2_LOG_TYPE) && !fileName.startsWith(TRACKING_ACTIVITY_V2_FILENAME_PREFIX))
                                   || (logType.equals(TRACKING_V3_LOG_TYPE) && !fileName.startsWith(TRACKING_ACTIVITY_V3_FILENAME_PREFIX))
                                   || (logType.equals(POLICY_LOG_TYPE) && !fileName.startsWith(POLICY_ACTIVITY_FILENAME_PREFIX))
                                   || (logType.equals(POLICY_V5_LOG_TYPE) && !fileName.startsWith(POLICY_ACTIVITY_V5_FILENAME_PREFIX))                                   
                                   || (logType.equals(POLICY_V4_LOG_TYPE) && !fileName.startsWith(POLICY_ACTIVITY_V4_FILENAME_PREFIX))
                                   || (logType.equals(POLICY_V3_LOG_TYPE) && !fileName.startsWith(POLICY_ACTIVITY_V3_FILENAME_PREFIX))
                                   || (logType.equals(POLICY_V2_LOG_TYPE) && !fileName.startsWith(POLICY_ACTIVITY_V2_FILENAME_PREFIX))
                                   || (logType.equals(POLICY_ASSISTANT_LOG_TYPE) && !fileName.startsWith(POLICY_ASSISTANT_FILENAME_PREFIX))) {
                                // We handle one policy type at a time
                                continue;
                            }

                            allUnlockedLogFiles.remove(logFileId);
                            allLockedLogFiles.put(logFileId, fileName);
                        }else{
                            continue;
                        }
                    }
                }

                File logFile = new File(LOG_DIR, fileName);
                try {
                    ObjectInputStream logInputStream = new ObjectInputStream(
                        new BufferedInputStream(new FileInputStream(logFile)));
                    BaseLogEntry[] entries = (BaseLogEntry[])logInputStream.readObject();
                    result.put(logFileId, entries);
                    currentLogSize += logFile.length();
                    logInputStream.close();
                } catch (FileNotFoundException e) {
                    this.getLog().error("File Not Found: " + logFile.getAbsolutePath(), e);
                    if (firstLog) {
                        logType = null;
                        firstLog = false;
                    }
                } catch (IOException e) {
                    this.getLog().error("Unable to read from file: "
                                        + logFile.getAbsolutePath(), e);
                    if (firstLog) {
                        logType = null;
                        firstLog = false;
                    }
                } catch (ClassNotFoundException e) {
                    this.getLog().error("Class Not Found", e);
                    if (firstLog) {
                        logType = null;
                        firstLog = false;
                    }
                } catch (Exception e) {
                    this.getLog().error("Error retrieving log files from log queue", e);
                    if (firstLog) {
                        logType = null;
                        firstLog = false;
                    }
                }
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * for unit test only
     * @param logFileId
     */
    protected void lockLogFile(Long logFileId){
        String fileName = "";
        synchronized (this.allUnlockedLogFiles) {
            synchronized (this.allLockedLogFiles) {
                fileName = this.allUnlockedLogFiles.get(logFileId);
                this.allUnlockedLogFiles.remove(logFileId);
                this.allLockedLogFiles.put(logFileId, fileName);
            }
        }
    }

    protected void setLogSizeLimit(int limit) {
        this.logSizeLimit = limit;
    }

    /**
     * 
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * 
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        LOG_DIR = this.configuration.get(LOG_QUEUE_LOCATION);
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdir();
        }

        logSizeLimit = this.configuration.get(MAX_BYTES_UPLOAD_SIZE_PARAM,
                                              DEFAULT_MAX_BYTES_UPLOAD_SIZE);
        if (logSizeLimit <= 0) {
            throw new IllegalArgumentException("logSizeLimit must be a positive number.");
        }

        ILogWriter persistenceLogWriter = this.configuration
                                          .get(LogInsertTaskFactory.PERSISTENCE_LOG_WRITER_PARAM);

        ILogQueueMgr logQueueMgr = this;
        int logInsertTaskIdleTime = this.configuration.get(
            LogInsertTaskFactory.LOG_INSERT_TASK_IDLE_TIME,
        LogInsertTaskFactory.DEFUALT_LOG_INSERT_TASK_IDLE_TIME);

        ITaskFactory logInsertTaskFactory = createTaskFactory(
            persistenceLogWriter, 
        logQueueMgr,
        logInsertTaskIdleTime);

        fileSystemThreadPool = this.configuration.get(FILE_SYSTEM_THREAD_POOL_PARAM);
        if (fileSystemThreadPool == null) {
            throw new NullPointerException("fileSystemThreadPool");
        }

        fileSystemThreadPool.setTaskFactory(logInsertTaskFactory);

        maxHandleFileSizePerThread = this.configuration.get(
            MAX_HANDLE_FILE_SIZE_PER_THREAD_PARAM, DEFAULT_MAX_HANDLE_FILE_SIZE_PER_THREAD);
        if (maxHandleFileSizePerThread <= 0) {
            throw new IllegalArgumentException(
                "maxHandleFileSizePerThread must be a positive number.");
        }
        // initialize the timer for checking out log writing timeout
        timerFrequency = this.configuration.get(TIMEOUT_CHECKER_FREQUENCY, 
                                                DEFAULT_TIMEOUT_CHECKER_FREQUENCY);
        logUnlockTimeOut = this.configuration.get(LOG_TIMEOUT, DEFAULT_LOG_TIMEOUT);

        timeoutTimer = new Timer("FileSystemLogQueueTimeoutTimer");
        if(timerFrequency > 0){
            timeoutTimer.scheduleAtFixedRate(new LogTimeoutTask(), TIMER_STARTUP_DELAY,
                                             this.timerFrequency);
        }


        logInsertTaskObservers = new ConcurrentLinkedQueue<LogInsertTaskObserver>();

        // read in any existing log files already in the log queue
        File[] logFileArray = logDir.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return name.startsWith(POLICY_ACTIVITY_FILENAME_PREFIX + LOG_FILE_PREFIX)
                    || name.startsWith(POLICY_ACTIVITY_V2_FILENAME_PREFIX + LOG_FILE_PREFIX)
                    || name.startsWith(POLICY_ACTIVITY_V3_FILENAME_PREFIX + LOG_FILE_PREFIX)
                    || name.startsWith(POLICY_ACTIVITY_V4_FILENAME_PREFIX + LOG_FILE_PREFIX)
                    || name.startsWith(POLICY_ACTIVITY_V5_FILENAME_PREFIX + LOG_FILE_PREFIX)
                    || name.startsWith(TRACKING_ACTIVITY_FILENAME_PREFIX + LOG_FILE_PREFIX)
                    || name.startsWith(TRACKING_ACTIVITY_V2_FILENAME_PREFIX + LOG_FILE_PREFIX)
                    || name.startsWith(TRACKING_ACTIVITY_V3_FILENAME_PREFIX + LOG_FILE_PREFIX)
                    || name.startsWith(POLICY_ASSISTANT_FILENAME_PREFIX + LOG_FILE_PREFIX);
            }
        });
        if (log.isDebugEnabled()) {
            log.debug("Number of logs detected at startup: " + 
                      logFileArray.length);
        }

        Long maxId = new Long(0);
        for (int i = 0; i < logFileArray.length; i++) {
            File logFile = logFileArray[i];
            Long logFileId = new Long(logFile.getName().substring(
                                          logFile.getName().indexOf("log") + 3));
            addLogFile(logFileId, logFile.getName());
            if (logFileId > maxId) {
                maxId = logFileId;
            }
            addFileSize(logFile.length());
        }

        maxId++;
        currentLogFileNum = maxId;
    }

    protected LogInsertTaskFactory createTaskFactory(ILogWriter persistenceLogWriter,
                                                     ILogQueueMgr logQueueMgr, int logInsertTaskIdleTime) {
        return new LogInsertTaskFactory(persistenceLogWriter,
                                        logQueueMgr, logInsertTaskIdleTime);
    }

    public void start() {
        isRunningState = true;
        fileSystemThreadPool.start();
    }

    private volatile boolean isRunningState = true;

    public void stop() {
        isRunningState = false;
        fileSystemThreadPool.stop();
        timeoutTimer.cancel();
    }

    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    private class LogTimeoutTask extends TimerTask {
        public void run() {
            List<Long> removeFromTimeout = new LinkedList<Long>();
            for (Map.Entry<Long, Long> entry : timeoutTracker.entrySet()) {
                if (unlockedLogFilesOrdering.contains(entry.getKey())) {
                    if ((System.currentTimeMillis() - entry.getValue()) > logUnlockTimeOut) {
                        // this file has timed out, needs to be unlocked
                        getLog().warn("The log file " + entry.getKey() + " has timed out.");
                        String fileName = allLockedLogFiles.get(entry.getKey());
                        allLockedLogFiles.remove(entry.getKey());
                        allUnlockedLogFiles.put(entry.getKey(), fileName);

                        // then remove it from the timeout tracker
                        removeFromTimeout.add(entry.getKey());
                    }
                    //else{
                    //   still not timeout yet
                    //}
                } else {
                    // this file has already been dequeued, need to be removed from the timeout tracker
                    removeFromTimeout.add(entry.getKey());
                }
            }

            // now do the actual removal of the logs from the timeout tracker
            for (long removeKey : removeFromTimeout) {
                timeoutTracker.remove(removeKey);
            }
        }
    }

    public void addLogInsertTaskObserver(LogInsertTaskObserver logInsertTaskObserver) {
        logInsertTaskObservers.add(logInsertTaskObserver);
    }

    public void removeLogInsertTaskObserver(LogInsertTaskObserver logInsertTaskObserver) {
        logInsertTaskObservers.remove(logInsertTaskObserver);
    }
}
