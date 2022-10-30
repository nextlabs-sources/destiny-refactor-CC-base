/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/MockAgentProfileStatusData.java#1 $
 */

/**
 * Inner class to represent profile status data.
 * 
 * @author safdar
 */
public class MockAgentProfileStatusData implements IAgentProfileStatusData {

    private String agentProfileName;
    private Calendar agentProfileModifiedDate;
    private String commProfileName;
    private Calendar commProfileModifiedDate;

    /**
     * Sets the comm profile
     * 
     * @param commProfile
     */
    public void acknowledgeCommProfile(ICommProfileDO commProfile) {
        if (commProfile != null) {
            this.commProfileName = commProfile.getName();
            this.commProfileModifiedDate = (Calendar) commProfile.getModifiedDate().clone();
        }
    }

    /**
     * Sets the agent profile
     * 
     * @param agentProfile
     */
    public void acknowledgeAgentProfile(IAgentProfileDO agentProfile) {
        if (agentProfile != null) {
            this.agentProfileName = agentProfile.getName();
            this.agentProfileModifiedDate = (Calendar) agentProfile.getModifiedDate().clone();
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData#getLastCommittedAgentProfileName()
     */
    public String getLastCommittedAgentProfileName() {
        return this.agentProfileName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData#getLastCommittedAgentProfileTimestamp()
     */
    public Calendar getLastCommittedAgentProfileTimestamp() {
        return this.agentProfileModifiedDate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData#getLastCommittedCommProfileName()
     */
    public String getLastCommittedCommProfileName() {
        return this.commProfileName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData#getLastCommittedCommProfileTimestamp()
     */
    public Calendar getLastCommittedCommProfileTimestamp() {
        return this.commProfileModifiedDate;
    }
}