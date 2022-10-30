/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import java.util.ArrayList;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.activityjournal.IActivityJournal;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.framework.comp.IComponentManager;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/commandengine/LogCommand.java#1 $:
 */

public class LogCommand extends AgentCommandBase implements IAgentCommandSet {

    BaseLogEntry logEntry;
    private IActivityJournal activityJournal;
    private final IComponentManager cm;

    /**
     * Constructor
     * @param logEntry
     * @param logCommandLog
     */
    public LogCommand(BaseLogEntry logEntry, Log log, IActivityJournal activityJournal, IComponentManager cm) {
        this.logEntry = logEntry;
        this.log = log;
        this.activityJournal = activityJournal;
        this.cm = cm;
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommand#init(java.util.ArrayList)
     */
    public void init(ArrayList paramArray) {
        this.logEntry = (BaseLogEntry) paramArray.get(0);
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommand#execute()
     */
    public int execute() {
        boolean infoEnabled = this.log.isInfoEnabled(); 
        if (infoEnabled) {
            this.log.info("Executing log command: Time: " + this.logEntry.getTimestamp());
        }
        if (this.activityJournal == null) {
            this.activityJournal = (IActivityJournal) this.cm.getComponent(IActivityJournal.NAME);
        }
        
        if (this.logEntry instanceof PolicyActivityLogEntryV5) {
            PolicyActivityLogEntryV5 policyLog = (PolicyActivityLogEntryV5) this.logEntry;
            if (infoEnabled) {
                this.log.info("User ID: " + policyLog.getUserId() + " Action: " + policyLog.getAction() + " Effect: " + policyLog.getPolicyDecision());
            }
            this.activityJournal.logPolicyActivity(policyLog);
        } else if (this.logEntry instanceof TrackingLogEntryV3) {
            this.activityJournal.logTrackingActivity((TrackingLogEntryV3) this.logEntry);
        } else if (this.logEntry instanceof PolicyAssistantLogEntry) {
            this.activityJournal.logPolicyAssistantActivity((PolicyAssistantLogEntry) this.logEntry);
        }


        return ErrorCode.SUCCESS;
    }

    /**
     * Returns the logEntry.
     * 
     * @return the logEntry.
     */
    public BaseLogEntry getLogEntry() {
        return this.logEntry;
    }

    /**
     * Sets the logEntry
     * 
     * @param logEntry
     *            The logEntry to set.
     */
    public void setLogEntry(BaseLogEntry logEntry) {
        this.logEntry = logEntry;
    }

    @Override
    public CommunicationType getCommunicationType() {
        //I don't use network
        return CommunicationType.LOCAL;
    }
    

}
