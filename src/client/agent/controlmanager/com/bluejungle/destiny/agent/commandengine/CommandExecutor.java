/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.activityjournal.IActivityJournal;
import com.bluejungle.destiny.agent.commandengine.IAgentCommand.CommunicationType;
import com.bluejungle.destiny.services.agent.types.UserNotification;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.ThreadPool;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class CommandExecutor implements ICommandExecutor, IInitializable, ILogEnabled, IManagerEnabled {

    private static final int NUM_THREADS = 1;
    private static final String POOL_NAME = "CommandExecutor";
    private ThreadPool threadPool;
    
    // a network call may take a long time and will block the other command such as LogCommand
    // LogCommand will persist the log to the local file system. That command should not be blocked
    //   by the network
    // instead of implement priority queue. We just create the other pool for non-network command
    private static final String LOCAL_POOL_NAME = "CommandExecutor(Local)";
    private ThreadPool localThreadPool;

    private Log log = null;

    private Log logCommandLog;
    private IComponentManager cm;
    private IActivityJournal aj = null;

    public void queueCommandSet(CommandSetSpec commandSetSpec) {
        AgentCommandSet commandSet = new AgentCommandSet();
        commandSet.init(commandSetSpec);
        threadPool.doWork(commandSet);
    }

    public void queueCommand(CommandSpecBase spec) {
        IAgentCommand command = AgentCommandFactory.createCommand(spec);
        addCommandToQueue(command);
    }

    public void setActivityJournal(IActivityJournal aj) {
        this.aj = aj;
    }

    public void logActivity(BaseLogEntry logEntry) {
        if (getLog().isInfoEnabled()) {
            getLog().info(Thread.currentThread().getName() + ": Log command added to Command queue");
        }
        LogCommand command = new LogCommand(logEntry, logCommandLog, aj, getManager());
        
        if (command.getCommunicationType() == CommunicationType.LOCAL) {
            localThreadPool.doWork(command);
        } else {
            threadPool.doWork(command);
        }
    }

    public void deleteFile(String decisionId, ArrayList fileList) {
    }

    public void moveFile(String decisionId, ArrayList fileList, String destination) {
    }

    public void renameFile(String decisionId, String fileName, // do we need
            // more info?
            String newFileName) {
    }

    public void compressFile(String decisionId, ArrayList fileList) {
    }

    public void encryptFile(String decisionId, ArrayList fileList) {
    }

    public void sendHeartBeat() {
        if (getLog().isInfoEnabled()) {
            getLog().info(Thread.currentThread().getName() + ": Send Heartbeat command added to Command queue");
        }

        HeartBeatCommand command = (HeartBeatCommand) AgentCommandFactory.createCommand(AgentCommandFactory.HEARTBEAT_COMMAND);
        addCommandToQueue(command);
    }

    public void recordHeartbeatInfo() {
        if (getLog().isInfoEnabled()) {
            getLog().info(Thread.currentThread().getName() + ": Record heartbeat info command added to Command queue");
        }

        RecordHeartbeatCommand command = (RecordHeartbeatCommand) AgentCommandFactory.createCommand(AgentCommandFactory.RECORD_HEARTBEAT_COMMAND);
        addCommandToQueue(command);
    }

    public void uploadLogs() {
        if (getLog().isInfoEnabled()) {
            getLog().info(Thread.currentThread().getName() + ": Upload Logs command added to Command queue");
        }

        UploadLogsCommand command = (UploadLogsCommand) AgentCommandFactory.createCommand(AgentCommandFactory.UPLOAD_LOGS_COMMAND);
        addCommandToQueue(command);
    }

    /**
     * Creates a command set for the command and adds it to the queue
     * 
     * @param command
     *            command to add to queue
     */
    private void addCommandToQueue(IAgentCommand command) {
        AgentCommandSet commandSet = new AgentCommandSet(command);
        if (command.getCommunicationType() == CommunicationType.LOCAL) {
            localThreadPool.doWork(commandSet);
        } else {
            threadPool.doWork(commandSet);
        }
    }

    /**
     * 
     * Initializes an instance of ThreadPool. ThreadPool will be used as the
     * command queue. We will only have one thread handling commands for now.
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        // Create ThreadPool
        getLog().info("Command Execution Component Init...");
        
        threadPool = createThreadPool(POOL_NAME);
        localThreadPool = createThreadPool(LOCAL_POOL_NAME);
        logCommandLog = LogFactory.getLog(LogCommand.class.getName());
    }
    
    private ThreadPool createThreadPool(String poolName){
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IThreadPool.THREADPOOL_NAME, poolName);
        config.setProperty(IThreadPool.THREADPOOL_SIZE, new Integer(NUM_THREADS));
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, CommandExecutionWorker.class.getName());
        config.setProperty(IThreadPool.MANUAL_START, Boolean.TRUE);
        
        ComponentInfo<ThreadPool> info = new ComponentInfo<ThreadPool>(
                CommandExecutor.class.getName() + ThreadPool.class.getName(), 
                ThreadPool.class, 
                IThreadPool.class, 
                LifestyleType.TRANSIENT_TYPE, 
                config);
        return getManager().getComponent(info);
    }

    /**
     * Start the Command Executor
     */
    public void start() {
        getLog().info("Command Execution Component Started...");
        threadPool.startThreads();
        localThreadPool.startThreads();
    }

    public void stop() {
        getLog().info("Command Execution Component Stopped...");
        threadPool.stop();
        localThreadPool.stop();
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
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#notify(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void notify(String fromAddress, String emailAddresses, String subject, String body) {
        if (getLog().isInfoEnabled()) {
            getLog().info(Thread.currentThread().getName() + ": Notification command added to Command queue");
        }

        UserNotification notification = new UserNotification();
        
        notification.setFrom(fromAddress);
        notification.setTo(emailAddresses);
        notification.setSubject(filter(subject));
        notification.setBody(filter(body));

        NotifyCommand command = (NotifyCommand) AgentCommandFactory.createCommand(AgentCommandFactory.NOTIFY_COMMAND);
        command.setNotification(notification);

        addCommandToQueue(command);
    }
    
    /**
     * bug 10317, Axis doesn't like non UTF8 in the String.
     * All non utf8 character will be replaced with underline. 
     * @param input
     * @return
     */
    private String filter(String input) {
        //remove all non utf-8 characters
        return input != null ? input.replaceAll("\\p{Cntrl}", "_") : null;
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

}
