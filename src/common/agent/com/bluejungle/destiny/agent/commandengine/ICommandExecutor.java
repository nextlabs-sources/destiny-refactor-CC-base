/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import java.util.ArrayList;

import com.bluejungle.domain.log.BaseLogEntry;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface ICommandExecutor {

    public static final String NAME = "Agent Command Executor";
    /**
     * This method is used to add command sets that are returned in the response
     * of the heartbeat.
     * 
     * @param commandSetSpec
     *            CommandSetSpec to add to the queue
     */
    public void queueCommandSet(CommandSetSpec commandSetSpec);

    /**
     * Adds command specified by spec to the queue
     * 
     * @param spec
     *            CommandSpec of the command to add.
     */
    public void queueCommand(CommandSpecBase spec);

    /**
     * Adds a log command to the command queue
     * 
     * @param logEntry
     *            ActivityLogEntry instance to log
     */
    public void logActivity(BaseLogEntry logEntry);

    /**
     * Adds a delete command to the command queue
     * 
     * @param decisionId
     *            policy decision id.
     * @param fileList
     *            list of files to delete
     */
    public void deleteFile(String decisionId, ArrayList fileList);

    /**
     * Adds a move command to the command queue
     * 
     * @param decisionId
     *            policy decision id.
     * @param fileList
     *            list of files to move
     * @param destination
     *            files specified by fileList are moved to this location
     */
    public void moveFile(String decisionId, ArrayList fileList, String destination);

    /**
     * Adds a reame command to the command queue
     * 
     * @param decisionId
     *            policy decision id.
     * @param fileName
     *            name of file to rename
     * @param newFileName
     *            file name to rename to.
     */
    public void renameFile(String decisionId, String fileName, String newFileName);

    /**
     * Adds a compress command to the command queue
     * 
     * @param decisionId
     *            policy decision id.
     * @param fileList
     *            list of files to compress
     */
    public void compressFile(String decisionId, ArrayList fileList);

    /**
     * Adds an encrypt command to the command queue
     * 
     * @param decisionId
     *            policy decision id.
     * @param fileList
     *            list of files to encrypt
     */
    public void encryptFile(String decisionId, ArrayList fileList);

    /**
     * Adds a heartbeat command to the command queue
     */
    public void sendHeartBeat();

    /**
     * Adds an log upload command to the command queue
     */
    public void uploadLogs();

    /**
     * Adds an record heartbeat info command to the command queue
     */
    public void recordHeartbeatInfo();

    /**
     * Start the Command Executor 
     */
    public void start ();

    /**
     * Stop the Command Executor 
     */
    public void stop ();


    /**
     * Notifies recipient(s) by email.
     *  
     * @param fromAddress the from email address that will show up in the message
     * @param emailAddresses comma-separated, or space-separated list of email addresses
     * @param subject message subject
     * @param body message body
     */
    public void notify(String fromAddress, String emailAddresses, String subject, String body);
    
}
