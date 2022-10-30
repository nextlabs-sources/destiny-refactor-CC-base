/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdateAcknowledgementData;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/MockAgentUpdateAcknowledgementData.java#1 $
 */

public class MockAgentUpdateAcknowledgementData implements IAgentUpdateAcknowledgementData {

    private MockAgentProfileStatusData profileStatus = new MockAgentProfileStatusData();

    /**
     * 
     * Constructor
     *  
     */
    public MockAgentUpdateAcknowledgementData() {
    }

    /**
     * Sets the agent profile. This is used to generate the status information
     * that is queried via the IAgentUpdateAcknowledgementData interface
     * 
     * @param agentProfile
     */
    public void acknowledgeAgentProfile(IAgentProfileDO agentProfile) {
        this.profileStatus.acknowledgeAgentProfile(agentProfile);
    }

    /**
     * Sets the comm profile. This is used to generate the status information
     * that is queried via the IAgentUpdateAcknowledgementData interface
     * 
     * @param agentProfile
     */
    public void acknowledgeCommProfile(ICommProfileDO commProfile) {
        this.profileStatus.acknowledgeCommProfile(commProfile);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdateAcknowledgementData#getPolicyAssemblyStatus()
     */
    public IAgentPolicyAssemblyStatusData getPolicyAssemblyStatus() {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdateAcknowledgementData#getProfileStatus()
     */
    public IAgentProfileStatusData getProfileStatus() {
        return this.profileStatus;
    }

}