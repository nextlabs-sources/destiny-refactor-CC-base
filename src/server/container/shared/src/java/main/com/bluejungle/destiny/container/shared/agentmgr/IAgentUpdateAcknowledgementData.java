/*
 * Created on Jan 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

/**
 * Interface to acknowledge updates that were sent from the server to the agent
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentUpdateAcknowledgementData.java#1 $
 */

public interface IAgentUpdateAcknowledgementData {

    /**
     * Gets the acknowledgement data for the last committed policy assembly
     * 
     * @return policy assembly acknowledgement data
     */
    public IAgentPolicyAssemblyStatusData getPolicyAssemblyStatus();

    /**
     * Gets the acknowledgement data for the last committed profiles
     * 
     * @return profile acknowledgement data
     */
    public IAgentProfileStatusData getProfileStatus();
}