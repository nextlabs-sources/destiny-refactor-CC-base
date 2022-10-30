/*
 * Created on Jan 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

/**
 * Contains agent status updates and also contains acknowledgement data for any
 * previous updates sent to the agent.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentHeartbeatData.java#1 $
 */

public interface IAgentHeartbeatData {

    /**
     * Return the policy assembly status data
     * 
     * @return status object
     */
    public IAgentPolicyAssemblyStatusData getPolicyAssemblyStatus();

    /**
     * Return the profile status data
     * 
     * @return status object
     */
    public IAgentProfileStatusData getProfileStatus();
}