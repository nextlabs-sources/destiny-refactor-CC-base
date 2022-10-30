/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatus;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;

import java.util.Calendar;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/MockAgentHeartbeatData.java#1 $
 */

public class MockAgentHeartbeatData implements IAgentHeartbeatData {

    private MockAgentProfileStatusData profileStatus;

    /**
     * 
     * Constructor
     *  
     */
    public MockAgentHeartbeatData() {
        this.profileStatus = new MockAgentProfileStatusData();
    }

    /**
     * Sets the agent profile. This is used to generate the status information
     * that is queried via the IAgentHeartbeatData interface
     * 
     * @param agentProfile
     */
    public void acknowledgeAgentProfile(IAgentProfileDO agentProfile) {
        this.profileStatus.acknowledgeAgentProfile(agentProfile);
    }

    /**
     * Sets the comm profile. This is used to generate the status information
     * that is queried via the IAgentHeartbeatData interface
     * 
     * @param agentProfile
     */
    public void acknowledgeCommProfile(ICommProfileDO commProfile) {
        this.profileStatus.acknowledgeCommProfile(commProfile);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData#getPolicyAssemblyStatus()
     */
    public IAgentPolicyAssemblyStatusData getPolicyAssemblyStatus() {
        return new IAgentPolicyAssemblyStatusData() {

            /**
             * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatusData#getLastCommittedDeploymentBundleTimestamp()
             */
            public Calendar getLastCommittedDeploymentBundleTimestamp() {
                return Calendar.getInstance();
            }

        
            
        };
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData#getProfileStatus()
     */
    public IAgentProfileStatusData getProfileStatus() {
        return this.profileStatus;
    }
}