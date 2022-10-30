/*
 * Created on Dec 14, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.commandengine.tests;

import java.util.ArrayList;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.commandengine.CommandSetSpec;
import com.bluejungle.destiny.agent.commandengine.CommandSpecBase;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.framework.comp.ILogEnabled;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/commandengine/tests/TestCommandExecutor.java#1 $:
 */

public class TestCommandExecutor implements ICommandExecutor, ILogEnabled {

    private Log log = null;
    private Integer heartbeatCount = new Integer (0); 
    private Integer uploadLogsCount = new Integer (0); 
    private Integer recordHeartbeatInfoCount = new Integer (0); 
    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#queueCommandSet(com.bluejungle.destiny.agent.commandengine.CommandSetSpec)
     */
    public void queueCommandSet(CommandSetSpec commandSetSpec) {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#queueCommand(com.bluejungle.destiny.agent.commandengine.CommandSpecBase)
     */
    public void queueCommand(CommandSpecBase spec) {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#logActivity(com.bluejungle.destiny.services.log.types.BaseLog)
     */
    public void logActivity(BaseLogEntry logEntry) {
        this.log.info (Thread.currentThread().getName() + ": Log command added to Command queue");
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#deleteFile(java.lang.String, java.util.ArrayList)
     */
    public void deleteFile(String decisionId, ArrayList fileList) {
        this.log.info (Thread.currentThread().getName() + ": Delete command added to Command queue");
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#moveFile(java.lang.String, java.util.ArrayList, java.lang.String)
     */
    public void moveFile(String decisionId, ArrayList fileList, String destination) {
        this.log.info (Thread.currentThread().getName() + ": Move command added to Command queue");
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#renameFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void renameFile(String decisionId, String fileName, String newFileName) {
        this.log.info (Thread.currentThread().getName() + ": Rename command added to Command queue");
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#compressFile(java.lang.String, java.util.ArrayList)
     */
    public void compressFile(String decisionId, ArrayList fileList) {
        this.log.info (Thread.currentThread().getName() + ": Compress command added to Command queue");
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#encryptFile(java.lang.String, java.util.ArrayList)
     */
    public void encryptFile(String decisionId, ArrayList fileList) {
        this.log.info (Thread.currentThread().getName() + ": Encrypt command added to Command queue");
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#sendHeartBeat()
     */
    public void sendHeartBeat() {
        synchronized (this.heartbeatCount){
            this.heartbeatCount = new Integer (this.heartbeatCount.intValue() + 1);
        }
        this.log.info (Thread.currentThread().getName() + ": Heartbeat command added to Command queue");
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#uploadLogs()
     */
    public void uploadLogs() {
        synchronized (this.uploadLogsCount){
            this.uploadLogsCount = new Integer (this.uploadLogsCount.intValue() + 1);
        }
        this.log.info (Thread.currentThread().getName() + ": Update Logs command added to Command queue");
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#recordHeartbeatInfo()
     */
    public void recordHeartbeatInfo() {
        synchronized (this.recordHeartbeatInfoCount){
            this.recordHeartbeatInfoCount = new Integer (this.recordHeartbeatInfoCount.intValue() + 1);
        }
        this.log.info (Thread.currentThread().getName() + ": Record heartbeat info command added to Command queue");
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
     * Returns the heartbeatCount.
     * @return the heartbeatCount.
     */
    public int getHeartbeatCount() {
        return this.heartbeatCount.intValue();
    }
    /**
     * Returns the uploadLogsCount.
     * @return the uploadLogsCount.
     */
    public int getUploadLogsCount() {
        return this.uploadLogsCount.intValue();
    }
    /**
     * Returns the heartbeatInfoCount.
     * @return the heartbeatInfoCount.
     */
    public int getRecordHeartbeatInfoCount() {
        return this.recordHeartbeatInfoCount.intValue();
    }
    /**
     * Sets the uploadLogsCount
     * @param uploadLogsCount The uploadLogsCount to set.
     */
    public void setUploadLogsCount(Integer uploadLogsCount) {
        this.uploadLogsCount = uploadLogsCount;
    }
    /**
     * Sets the heartbeatCount
     * @param heartbeatCount The heartbeatCount to set.
     */
    public void setHeartbeatCount(Integer heartbeatCount) {
        this.heartbeatCount = heartbeatCount;
    }
    
    /**
     * Sets the recordHeartbeatInfoCount
     * @param recordHeartbeatInfoCount The recordHeartbeatInfoCount to set.
     */
    public void setHeartbeatInfoCount(Integer recordHeartbeatInfoCount) {
        this.recordHeartbeatInfoCount = recordHeartbeatInfoCount;
    }
    
    /**
     * Start the Command Executor 
     */
    public void start (){
        
    }

    /**
     * Stop the Command Executor 
     */
    public void stop (){
        
    }


    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#notify(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void notify(String fromAddress, String emailAddresses, String subject, String body) {
    }

}
