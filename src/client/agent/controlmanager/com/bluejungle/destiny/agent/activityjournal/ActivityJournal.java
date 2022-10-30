/*
 * Created on Mar 28, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.activityjournal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StreamCorruptedException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.commandengine.CommandExecutor;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.communication.ICommunicationManager;
import com.bluejungle.destiny.agent.controlmanager.EventMessages;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.LogUtils;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;
import com.nextlabs.destiny.types.log.v5.LogStatus;

/**
 * @author fuad
 * @version $Id:
 *          //depot/personal/sasha/main/Destiny/src/client/agent/controlmanager/com/bluejungle/destiny/agent/activityjournal/ActivityJournal.java#16 $:
 */

public class ActivityJournal implements IActivityJournal, ILogEnabled, IInitializable, IStartable, IConfigurable, IManagerEnabled {
    public static final PropertyKey<String> BASE_DIR_PROPERTY_NAME = new PropertyKey<String>("baseDirProperty");
    private static final String DEFAULT_BASE_DIR = ".";
    private static String baseDir = DEFAULT_BASE_DIR;


    public static final String POLICY_ACTIVITY_FILENAME_PREFIX = "pa5_";
    public static final String POLICY_ASSISTANT_FILENAME_PREFIX = "pass_";
    public static final String TRACKING_ACTIVITY_FILENAME_PREFIX = "trk3_";

    public static final int LOG_PERSISTENCE_TIMEOUT = 60000;
    public static final String LOG_SUB_DIR = "logs";
    public static String logDirectory = LOG_SUB_DIR;

    protected static final String AGENT_LOG_FILE = "activity.log";

    static final int WRITE_BUFFER_SIZE = 1024;
    static final int READ_BUFFER_SIZE = 16384;

    // number determined experimentally
    static final int SINGLE_LOG_SIZE = 300;

    static final int KB_MULTIPLE = 1024;
    static final int MB_MULTIPLE = 1024 * 1024;
    static final double LOG_UPLOAD_TRIGGER_RATIO = 0.8;

    long totalLogSize = 0;
    long logLimit = 1000000;
    LogIdGenerator logIdGenerator = null;

    protected LogPersistenceThread logPersistenceThread = null;

    protected ICommunicationManager communicationManager = null;
    protected IControlManager controlManager = null;

    protected boolean uploadingLogs = false;

    protected Log log;
    protected IConfiguration config;
    protected IComponentManager cm;

    private boolean lastUploadSucceded = true;
    private final static int LOG_COUNT_SINCE_LAST_FAILED_UPLOAD = 100;

    // This counter is used to prevent from trying to upload everytime we reach
    // 80% or more of the max log size
    // if we already failed to upload (otherwise we'll try to upload for every
    // log)
    private int logCountSinceLastFailedUpload = 0;

    static final int SINGLE_FILE_LIMIT = 1024 * KB_MULTIPLE;
    private static IOSWrapper osWrapper = ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    private LogWriter paLogWriter = new PALogWriter();
    private LogWriter passLogWriter = new PAssLogWriter();
    private LogWriter trLogWriter = new TRLogWriter();
    
    private CommandExecutor commandExecutor;

    /**
     * Tells the activity journal that agentId is available and activity journal
     * can be enabled
     */
    public synchronized void setEnabled() {
        BigInteger registrationId = controlManager.getRegistrationId();
        if (registrationId != null) {
            HashMapConfiguration config = new HashMapConfiguration();
            config.setProperty(LogIdGenerator.AGENT_REGISTRATION_ID, registrationId.longValue());
            config.setProperty(LogIdGenerator.BASE_DIR_PROPERTY_NAME, baseDir);
            ComponentInfo<LogIdGenerator> info =
					new ComponentInfo<LogIdGenerator>(LogIdGenerator.NAME, 
							LogIdGenerator.class,
							LogIdGenerator.class, 
							LifestyleType.SINGLETON_TYPE, 
							config);
            
            logIdGenerator = ComponentManagerFactory.getComponentManager().getComponent(info);
        }
    }

    /**
     * @see com.bluejungle.destiny.agent.activityjournal.IActivityJournal#logTrackingActivity(com.bluejungle.domain.getLog().TrackingLogEntry)
     */
    public synchronized void logTrackingActivity(TrackingLogEntryV3 logEntry) {
        trLogWriter.logActivity(logEntry);
    }

    /**
     * @see com.bluejungle.destiny.agent.activityjournal.IActivityJournal#logPolicyActivity(com.bluejungle.domain.getLog().PolicyActivityLogEntry)
     */
    public synchronized void logPolicyActivity(PolicyActivityLogEntryV5 logEntry) {
        paLogWriter.logActivity(logEntry);
    }

    /**
     * @see com.bluejungle.destiny.agent.activityjournal.IActivityJournal#logTrackingActivity(com.bluejungle.domain.getLog().TrackingLogEntry)
     */
    public synchronized void logPolicyAssistantActivity(PolicyAssistantLogEntry logEntry) {
        passLogWriter.logActivity(logEntry);
    }

    /**
     * Upload logs to the server. If successfully uploaded, delete the file
     * associated with the uploaded logs and remove the file from the index file
     * 
     * @see com.bluejungle.destiny.agent.activityjournal.IActivityJournal#uploadActivityLogs()
     */
    public void uploadActivityLogs() {
        if (!isEnabled()) {
            getLog().trace("Agent not registered. Logging is disabled.");
            return;
        }

        synchronized (this) {
            if (uploadingLogs) {
                return;
            }
            uploadingLogs = true;
            totalLogSize += paLogWriter.prepareForUpload();
            totalLogSize += passLogWriter.prepareForUpload();
            totalLogSize += trLogWriter.prepareForUpload();
        }

        long sizeUploaded = paLogWriter.uploadLog();
        sizeUploaded += trLogWriter.uploadLog();
        sizeUploaded += passLogWriter.uploadLog();
        if ((paLogWriter.sentLogs || passLogWriter.sentLogs || trLogWriter.sentLogs)  && getLog().isInfoEnabled()) {
            osWrapper.logEvent(EventMessages.EVENTLOG_INFORMATION_TYPE, EventMessages.MSG_LOG_UPLOAD, new String[0]);
        }
        synchronized (this) {
            getLog().trace("Uploaded " + sizeUploaded + " bytes of logs. " + totalLogSize + " remaining on disk");
            totalLogSize -= sizeUploaded;
            uploadingLogs = false;
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
        return log;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration configuration = getConfiguration();
        if (configuration != null) {
            baseDir = configuration.get(BASE_DIR_PROPERTY_NAME, DEFAULT_BASE_DIR);
        }

        logDirectory = baseDir + File.separator + LOG_SUB_DIR;

        IProfileManager profileManager = (IProfileManager) getManager().getComponent(IProfileManager.NAME);
        if (profileManager == null) {
            getLog().error("Activity Journal init failed. Profile Manager not found.");
            return;
        }
        logLimit = profileManager.getCommunicationProfile().getLogLimit().intValue() * MB_MULTIPLE;

        // check for overflow
        if (logLimit < 0) {
            logLimit = Long.MAX_VALUE;
        }

        controlManager = (IControlManager) getConfiguration().get(IControlManager.NAME);
        if (controlManager == null) {
            getLog().error("Activity Journal init failed. Could not obtain Control Manager from config.");
            return;
        }

        if (communicationManager == null) {
            communicationManager = (ICommunicationManager) getManager().getComponent(ICommunicationManager.NAME);
        }

        setEnabled();
        
        File logDir = new File(logDirectory);
        if (!logDir.exists()) {
            logDir.mkdir();
            if (getLog().isInfoEnabled()) {
                getLog().info("Created log directory: " + logDir.getAbsolutePath());
            }
        }
        paLogWriter.init();
        passLogWriter.init();
        trLogWriter.init();

        logPersistenceThread = new LogPersistenceThread(this);
        logPersistenceThread.start();

        commandExecutor = (CommandExecutor) getManager().getComponent(ICommandExecutor.NAME);
        commandExecutor.setActivityJournal(this);
    }

    /**
     * Persists logs to disk
     */
    synchronized void persistLogs() {
        paLogWriter.persistLogs();
        passLogWriter.persistLogs();
        trLogWriter.persistLogs();
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public synchronized void stop() {
        logPersistenceThread.interrupt();
        try {
            logPersistenceThread.join();
        } catch (InterruptedException e1) {
            getLog().error("Error while stopping Activity Journal.");
        }
        paLogWriter.stop();
        passLogWriter.stop();
        trLogWriter.stop();

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
        return config;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        cm = manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return cm;
    }

    /**
     * package visibility for unit tests
     * 
     * @return total log size
     */
    protected long getTotalLogSize() {
        return totalLogSize;
    }

    /**
     * @return the sum of the file size for the current log files of all
     *         LogWriter
     */
    protected long getCurrentLogFileSize() {
        return paLogWriter.logSize + trLogWriter.logSize + passLogWriter.logSize;
    }

    /**
     * Returns true if activity journal is ready.
     *  
     */
    public boolean isEnabled() {
        return (logIdGenerator != null);
    }

    private abstract class LogWriter {
        final String filenamePrefix;
        boolean sentLogs;
        File logFile;
        ObjectOutputStream logOutputStream;
        int logSize;
        final HashSet<String> fileNameSet = new HashSet<String>();
        HashSet<String> uploadFileNameSet = new HashSet<String>();

        LogWriter(String filenamePrefix) {
            this.filenamePrefix = filenamePrefix;
        }

        /**
         * Writes the log entry to disk. If the log limit is reached, force log
         * upload
         * 
         * Called from synchronized method
         *
         * @param logEntry instace of BaseLog to add to the log
         */
        void logActivity(BaseLogEntry logEntry) {
            if (!isEnabled()) {
                getLog().trace("Agent not registered. Logging is disabled.");
                return;
            }

            try {
                // if logLimit reached, stop logging
                if ((totalLogSize + getCurrentLogFileSize()) > ActivityJournal.this.logLimit) {
                    if (getLog().isInfoEnabled()) {
                        getLog().info("Not logging, log size too big (total=" + totalLogSize + " logSize=" + logSize + " limit=" + logLimit + ')');
                    }
                    return;
                }

                long entryUid = logEntry.getUid();

                if (entryUid == 0) {
                    logEntry.setUid(ActivityJournal.this.logIdGenerator.getNextId());
                }

                if (logSize > SINGLE_FILE_LIMIT) {
                    logOutputStream.close();
                    totalLogSize += logFile.length();
                    createLogFile();
                }
                logEntry.writeExternal(logOutputStream);

                // This is just an estimate so that we have some idea when the log file gets too big
                logSize += SINGLE_LOG_SIZE;

                if ((totalLogSize + getCurrentLogFileSize()) > logLimit * LOG_UPLOAD_TRIGGER_RATIO) {
                    if (lastUploadSucceded || logCountSinceLastFailedUpload++ >= LOG_COUNT_SINCE_LAST_FAILED_UPLOAD) {
                        //don't make a network call, it will block the thread pool if the network is blocked
                        if (!uploadingLogs) {
                            commandExecutor.uploadLogs();
                        }
                    }
                }
            } catch (IOException e) {
                getLog().error("Could not write log to disk.", e);
            }
        }

        /**
         * removes file from index and deletes the log file. this is called
         * after logs from this file have been successfully uploaded to the
         * server
         * 
         * @param fileName  file name to remove from file index
         */
        void removeLogFile(String fileName) {
            synchronized (fileNameSet) {
                File file = new File(logDirectory, fileName);
                if (!file.delete()) {
                    getLog().warn("Could not delete " + fileName);
                }
                fileNameSet.remove(fileName);
            }
        }

        /**
         * Persists logs to disk and read the correct logsize
         */
        void persistLogs() {
            try {
                logOutputStream.flush();
                logOutputStream.reset();
                getLog().trace("Computed log size: " + logSize);
                logSize = (int) logFile.length();
                getLog().trace("Actual log size: " + logSize);
            } catch (IOException e) {
                getLog().error("Error writing logs to disk.", e);
            }

        }

        /**
         * finds an available name for a log file, adds the name to the file
         * name index, creates the file, and makes it the current one
         */
        void createLogFile() {
            int i = 0;
            String newFileName;

            synchronized (fileNameSet) {
                do {
                    newFileName = filenamePrefix + AGENT_LOG_FILE + Integer.toString(i);
                    logFile = new File(logDirectory, newFileName);
                    i++;
                } while (fileNameSet.contains(newFileName));

                fileNameSet.add(newFileName);
            }

            try {
                if (logFile.exists()) {
                    logFile.delete();
                }
                logOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(logFile, false), WRITE_BUFFER_SIZE));
                getLog().info("New log created " + logFile.getAbsolutePath());
            } catch (IOException e) {
                getLog().error("Unable to create log file: " + logFile.getAbsolutePath(), e);
            }
            logSize = 0;
        }

        /**
         * Compute the size of the current logging file, create a new one for future logs,
         * and build a set of all the files we should upload at this time (the sizes of files
         * other than the current one have already been included in totalLogSize)
         *
         * @return size of currently active log file
         */
        long prepareForUpload() {
            try {
                logOutputStream.close();
                long size = logFile.length();

                // Tread with caution -- at this point ActivityJournal.this is
                // locked as well
                // so have to be careful to avoid deadlock
                synchronized (fileNameSet) {
                    uploadFileNameSet = (HashSet<String>) fileNameSet.clone();
                }
                createLogFile();
                return size;
            } catch (IOException e) {
                getLog().error("Unable to close log file " + logFile.getName(), e);
                return 0;
            }

        }

        /**
         * Upload the logs specified in uploadFileNameSet. Returns the size of all the files
         * that set (which is not exactly the same as the size of the logs uploaded, because
         * a file containing no logs still has the 4 byte Java serialization header. We still
         * need to count this, however, because otherwise the value in totalLogSize will not
         * be adjusted correctly).
         *
         * This code is not thread safe and is not called from a synchronized method, but we
         * check to see if uploading is happening before we call this method, so we avoid
         * thread collision.
         *
         * @return size of files that were uploaded
         */
        long uploadLog() {
            long totalSize = 0;
            sentLogs = false;
            for (String currentFileName : uploadFileNameSet) {
                File currentLogFile = new File(logDirectory, currentFileName);
                long currentFileSize = currentLogFile.length();
                ArrayList<BaseLogEntry> logEntries = new ArrayList<BaseLogEntry>();
                ObjectInputStream ois = null;
                FileInputStream fis = null;
                if (currentLogFile.exists()) {
                    boolean makeLogBackup = false;
                    try {
                        fis = new FileInputStream(currentLogFile);
                        ois = new ObjectInputStream(new BufferedInputStream(fis, READ_BUFFER_SIZE));
                        readLogEntries(logEntries, ois);
                    } catch (EOFException e) {
                        // no error
                    } catch (FileNotFoundException e) {
                        getLog().error("Unable to read log file" + currentLogFile.getAbsolutePath(), e);
                        if (getLog().isErrorEnabled()) {
                            osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Unable to read log file" });
                        }
                    } catch (StreamCorruptedException e) {
                        getLog().error("Unknown exception (log corruption?) reading log file: " + currentLogFile.getAbsolutePath(), e);
                        makeLogBackup = true;
                        if (getLog().isErrorEnabled()) {
                            osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Unable to read log file" });
                        }
                    } catch (IOException e) {
                        getLog().error("Unable to read log file" + currentLogFile.getAbsolutePath(), e);
                        if (getLog().isErrorEnabled()) {
                            osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Unable to read log file" });
                        }
                    } catch (ClassNotFoundException e) {
                        getLog().error("Unable to read log file" + currentLogFile.getAbsolutePath(), e);
                        if (getLog().isErrorEnabled()) {
                            osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Unable to read log file" });
                        }
                    } catch (Throwable e) {
                        getLog().error("Unknown exception (log corruption?) reading log file: " + currentLogFile.getAbsolutePath(), e);
                        makeLogBackup = true;
                    } finally {
                        if (ois != null) {
                            try {
                                ois.close();
                            } catch (IOException e1) {
                                getLog().warn("Could not close " + currentFileName, e1);
                            }
                        }
                        if (fis != null && ois == null) {
                            try {
                                // this can happen if the file exists, but is
                                // corrupted
                                fis.close();
                            } catch (IOException e1) {
                                getLog().warn("Could not close " + currentFileName, e1);
                            }
                        }
                    }

                    try {
                        if (logEntries.size() > 0) {
                            sendLogsToServer(logEntries);
                        }
                        totalSize += currentFileSize;

                        if (makeLogBackup) {
                            File file = new File(logDirectory, currentFileName);
                            File toFile = new File(logDirectory, "ALERT_NEXTLABS");
                            file.renameTo(toFile);
                        }

                        removeLogFile(currentFileName);
                        ActivityJournal.this.lastUploadSucceded = true;
                    } catch (ServiceNotReadyFault e) {
                        ActivityJournal.this.logCountSinceLastFailedUpload = 0;
                        ActivityJournal.this.lastUploadSucceded = false;
                        getLog().error("Log upload failed. Service not ready", e);
                        if (getLog().isErrorEnabled()) {
                            osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "ICENet Service Not Ready" });
                        }
                        break;
                    } catch (UnauthorizedCallerFault e) {
                        ActivityJournal.this.logCountSinceLastFailedUpload = 0;
                        ActivityJournal.this.lastUploadSucceded = false;
                        getLog().error("Log upload failed. Access denied by server", e);
                        if (getLog().isErrorEnabled()) {
                            osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Access Denied" });
                        }
                        break;
                    } catch (RemoteException e) {
                        ActivityJournal.this.logCountSinceLastFailedUpload = 0;
                        ActivityJournal.this.lastUploadSucceded = false;
                        if (e.detail instanceof SocketTimeoutException) {
                            getLog().error("Log upload failed. Socket timeout.", e);
                            if (getLog().isInfoEnabled()) {
                                osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Socket timeout. The ICENet server did not respond" });
                            }
                        } else {
                            getLog().error("Log upload failed. Service threw exception", e);
                            if (getLog().isInfoEnabled()) {
                                osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Could not connect to ICENet server" });
                            }

                        }
                        break;
                    } catch (ServiceException e) {
                        ActivityJournal.this.logCountSinceLastFailedUpload = 0;
                        ActivityJournal.this.lastUploadSucceded = false;
                        getLog().error("Log upload failed.", e);
                        if (getLog().isInfoEnabled()) {
                            osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Service Exception" });
                        }
                        break;
                    } catch (IOException e) {
                        ActivityJournal.this.logCountSinceLastFailedUpload = 0;
                        ActivityJournal.this.lastUploadSucceded = false;
                        getLog().error("Log upload failed.", e);
                        if (getLog().isInfoEnabled()) {
                            osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_LOG_UPLOAD_FAILED, new String[] { "Service Exception" });
                        }
                        break;
                    }
                }
            }
            return totalSize;

        }

        abstract void readLogEntries(Collection<BaseLogEntry> logEntries, ObjectInputStream stream) throws ClassNotFoundException, IOException;

        abstract void sendEncodedEntries(String encodedEntries) throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException;

        /**
         * 
         * send logs to server.
         * 
         * @param policyLogs
         *            ArrayList of BaseLog instances to be uploaded.
         * @throws ServiceException
         * @throws RemoteException
         * @throws UnauthorizedCallerFault
         * @throws ServiceNotReadyFault
         */
        void sendLogsToServer(Collection<BaseLogEntry> logEntries) throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException, IOException {
            String encodedEntries = LogUtils.encodeLogEntries(logEntries);
            sentLogs = true;
            sendEncodedEntries(encodedEntries);
        }

        void init() {
            if (logFile == null) {
                File logDir = new File(logDirectory);
                final String nameStart = filenamePrefix + AGENT_LOG_FILE;

                logDir.listFiles(new FileFilter() {

                    public boolean accept(File file) {
                        String name = file.getName();
                        if (name.startsWith(nameStart)) {
                            LogWriter.this.fileNameSet.add(name);
                            totalLogSize += file.length();
                        }
                        return false;
                    }
                });
            }
            createLogFile();
        }

        void stop() {
            try {
                logOutputStream.close();
                totalLogSize += logFile.length();
            } catch (IOException e) {
                getLog().error("Unable to close log file: " + logFile.getAbsolutePath(), e);
            }
        }
    }

    private class PALogWriter extends LogWriter {

        PALogWriter() {
            super(POLICY_ACTIVITY_FILENAME_PREFIX);
        }

        void readLogEntries(Collection<BaseLogEntry> logEntries, ObjectInputStream stream) throws ClassNotFoundException, IOException {
            while (true) {
                PolicyActivityLogEntryV5 entry = new PolicyActivityLogEntryV5();
                entry.readExternal(stream);
                logEntries.add(entry);
            }
        }

        void sendEncodedEntries(String encodedEntries) throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException {
            if (ActivityJournal.this.communicationManager.getLogServiceIF().logPolicyActivityV5(encodedEntries) != LogStatus.Success) {
                throw new ServiceException("Upload PA Log failed");
            }
        }
    }

    private class PAssLogWriter extends LogWriter {

        PAssLogWriter() {
            super(POLICY_ASSISTANT_FILENAME_PREFIX);
        }

        void readLogEntries(Collection<BaseLogEntry> logEntries, ObjectInputStream stream) throws ClassNotFoundException, IOException {
            while (true) {
                PolicyAssistantLogEntry entry = new PolicyAssistantLogEntry();
                entry.readExternal(stream);
                logEntries.add(entry);
            }
        }

        void sendEncodedEntries(String encodedEntries) throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException {
            if (ActivityJournal.this.communicationManager.getLogServiceIF().logPolicyAssistant(encodedEntries) != LogStatus.Success) {
                throw new ServiceException("Upload PAss Log failed");
            }
        }
    }

    private class TRLogWriter extends LogWriter {

        /**
         * Constructor
         */
        TRLogWriter() {
            super(TRACKING_ACTIVITY_FILENAME_PREFIX);
        }

        /**
         * @see com.bluejungle.destiny.agent.activityjournal.ActivityJournal.LogWriter#readLogEntries(java.util.Collection, java.io.ObjectInputStream)
         */
        void readLogEntries(Collection<BaseLogEntry> logEntries, ObjectInputStream stream) throws IOException, ClassNotFoundException {
            while (true) {
                TrackingLogEntryV3 entry = new TrackingLogEntryV3();
                entry.readExternal(stream);
                logEntries.add(entry);
            }
        }

        /**
         * @see com.bluejungle.destiny.agent.activityjournal.ActivityJournal.LogWriter#sendEncodedEntries(java.lang.String)
         */
        void sendEncodedEntries(String encodedEntries) throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException {
            if (ActivityJournal.this.communicationManager.getLogServiceIF().logTrackingV3(encodedEntries) != LogStatus.Success) {
                throw new ServiceException("Upload tracking log failed");
            }
        }

    }
}
