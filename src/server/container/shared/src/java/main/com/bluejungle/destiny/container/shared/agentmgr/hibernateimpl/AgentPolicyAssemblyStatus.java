/*
 * Created on Jan 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatus;

/**
 * Persisted component of the AgentDO that stores the agent profile status
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentPolicyAssemblyStatus.java#1 $
 */

public class AgentPolicyAssemblyStatus implements IAgentPolicyAssemblyStatus {

    private Calendar lastAcknowledgedDeploymentBundleTimestamp;

    /**
     * Returns the timestamp of the last policy update that this agent has
     * received.
     * 
     * @return timestamp
     */
    public Calendar getLastAcknowledgedDeploymentBundleTimestamp() {
        return this.lastAcknowledgedDeploymentBundleTimestamp;
    }

    /**
     * Sets the timestamp of the last policy update that this agent has
     * received.
     * 
     * @param timestamp
     */
    public void setLastAcknowledgedDeploymentBundleTimestamp(Calendar timestamp) {
        this.lastAcknowledgedDeploymentBundleTimestamp = timestamp;
    }
}