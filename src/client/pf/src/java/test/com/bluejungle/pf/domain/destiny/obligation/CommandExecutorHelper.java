/*
 * Created on Dec 30, 2004
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.obligation;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.agent.commandengine.CommandSetSpec;
import com.bluejungle.destiny.agent.commandengine.CommandSpecBase;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/test/com/bluejungle/pf/domain/destiny/obligation/CommandExecutorHelper.java#1 $:
 */

public class CommandExecutorHelper implements ICommandExecutor, IHasComponentInfo<CommandExecutorHelper> {
    public static final String CLASSNAME = CommandExecutorHelper.class.getName();

    private ComponentInfo<CommandExecutorHelper> COMP_INFO =
        new ComponentInfo<CommandExecutorHelper>(
        		ICommandExecutor.NAME, 
        		CommandExecutorHelper.class, 
        		LifestyleType.SINGLETON_TYPE);

    private List<BaseLogEntry> logEntries = new ArrayList<BaseLogEntry>();
    private String emailAddresses;
    private String fromAddress;
    private String subject;
    private String body;
    
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
        logEntries.add(logEntry);
    }

    /**
     * Returns the last logged activity, if any.
     * This method is for testing.
     * @return the last logged activity, if any.
     */
    public BaseLogEntry getLastLoggedActivity() {
        if ( !logEntries.isEmpty() ) {
            return (BaseLogEntry)logEntries.get(logEntries.size()-1);
        } else {
            return null;
        }
    }

    /**
     * Returns the content of log entries, and optionally resets the log.
     * This method is for testing.
     * @param reset when true, the log is reset after the action.
     * @return The content of log entries.
     */
    public BaseLogEntry[] getLogEntriesSinceReset(boolean reset) {
        BaseLogEntry[] res = (BaseLogEntry[])logEntries.toArray(new BaseLogEntry[logEntries.size()]);
        if ( reset ) {
            resetLog();
        }
        return res;
    }

    /**
     * Clears all log entries accumulated since the last reset.
     * This method is for testing.
     */
    public void resetLog() {
        logEntries.clear();
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#deleteFile(java.lang.String, java.util.ArrayList)
     */
    public void deleteFile(String decisionId, ArrayList fileList) {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#moveFile(java.lang.String, java.util.ArrayList, java.lang.String)
     */
    public void moveFile(String decisionId, ArrayList fileList, String destination) {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#renameFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void renameFile(String decisionId, String fileName, String newFileName) {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#compressFile(java.lang.String, java.util.ArrayList)
     */
    public void compressFile(String decisionId, ArrayList fileList) {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#encryptFile(java.lang.String, java.util.ArrayList)
     */
    public void encryptFile(String decisionId, ArrayList fileList) {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#sendHeartBeat()
     */
    public void sendHeartBeat() {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#recordHeartbeatInfo()
     */
    public void recordHeartbeatInfo() {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#uploadLogs()
     */
    public void uploadLogs() {
    }

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<CommandExecutorHelper> getComponentInfo() {
        return COMP_INFO;
    }

    public void start () {
    }

    public void stop () {
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.ICommandExecutor#notify(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void notify(String fromAddress, String emailAddresses, String subject, String body) {
        this.fromAddress = fromAddress;
        this.emailAddresses = emailAddresses;
        this.subject = subject;
        this.body = body;
    }
    
    /**
     * Returns the to emailAddresses of the last e-mail.
     * @return the emailAddresses.
     */
    public String getEmailAddresses() {
        return emailAddresses;
    }

    /**
     * Returns the fromAddress of the last e-mail.
     * @return the fromAddress.
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * Returns the body of the last e-mail.
     * @return the body of the last e-mail.
     */
    public String getEmailBody() {
        return body;
    }

    /**
     * Returns the subject.
     * @return the subject.
     */
    public String getSubject() {
        return this.subject;
    }

}
