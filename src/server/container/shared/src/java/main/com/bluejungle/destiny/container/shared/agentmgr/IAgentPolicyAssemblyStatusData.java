/*
 * Created on Jan 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import java.util.Calendar;

/**
 * Aggregates data to reflect the status of the policy assembly on the agent
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentPolicyAssemblyStatusData.java#1 $
 */

public interface IAgentPolicyAssemblyStatusData {

    /**
     * Returns the timestamp of the last policy update that this agent has.
     * 
     * This is used to calculate the policy updates that this agent must
     * receive. However, this data is ignored if any user logs into the agent,
     * and instead, all relevant policies (for all active users on that agent)
     * are returned to the agent.
     * 
     * @return timestamp
     */
    public Calendar getLastCommittedDeploymentBundleTimestamp();
}