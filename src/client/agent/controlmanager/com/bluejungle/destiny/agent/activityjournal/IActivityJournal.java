/*
 * Created on Mar 28, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.activityjournal;

import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IStartable;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/activityjournal/IActivityJournal.java#1 $:
 */

public interface IActivityJournal extends IStartable, IInitializable {

    public static final String NAME = "AgentActivityJournal";
    
    /**
     * Adds policy activity log entry to the log.
     * 
     * @param logEntry log entry to add
     */
    void logPolicyActivity (PolicyActivityLogEntryV5 logEntry);

    /**
     * Adds tracking log entry to the log.
     * @param logEntry log entry to add 
     */
    void logTrackingActivity(TrackingLogEntryV3 logEntry);

    /**
     * Adds policy assistant entry to the log.
     * @param logEntry log entry to add 
     */
    void logPolicyAssistantActivity(PolicyAssistantLogEntry logEntry);
    
    /**
     * uploads activity logs to the server.
     */
    void uploadActivityLogs ();

    /**
     * Tells the activity journal that agentId is available and activity journal
     * can be enabled
     */
    public void setEnabled();
    
}
