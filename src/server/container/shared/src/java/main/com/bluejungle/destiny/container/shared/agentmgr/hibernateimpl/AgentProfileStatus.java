/*
 * Created on Jan 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatus;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;

/**
 * Persisted component of the AgentDO that stores the agent profile status
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentProfileStatus.java#1 $
 */

public class AgentProfileStatus implements IAgentProfileStatus {

    private String lastAcknowledgedAgentProfileName;
    private Calendar lastAcknowledgedAgentProfileTS;
    private String lastAcknowledgedCommProfileName;
    private Calendar lastAcknowledgedCommProfileTS;

    /**
     * 
     * Constructor
     *  
     */
    public AgentProfileStatus() {
        this.lastAcknowledgedAgentProfileTS = null;
        this.lastAcknowledgedCommProfileTS = null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatus#getLastAcknowledgedAgentProfileTimestamp()
     */
    public Calendar getLastAcknowledgedAgentProfileTimestamp() {
        return this.lastAcknowledgedAgentProfileTS;
    }

    /**
     * Sets the comm profile timestamp
     * 
     * @param timestamp
     */
    public void setLastAcknowledgedAgentProfileTimestamp(Calendar timestamp) {
        this.lastAcknowledgedAgentProfileTS = timestamp;
    }

    /**
     * Sets the agent profile timestamp
     * 
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatus#getLastAcknowledgedCommProfileTimestamp()
     */
    public Calendar getLastAcknowledgedCommProfileTimestamp() {
        return this.lastAcknowledgedCommProfileTS;
    }

    /**
     * Sets the agent profile timestamp
     * 
     * @param timestamp
     */
    public void setLastAcknowledgedCommProfileTimestamp(Calendar timestamp) {
        this.lastAcknowledgedCommProfileTS = timestamp;
    }

    /**
     * Returns the lastAcknowledgedAgentProfileName.
     * 
     * @return the lastAcknowledgedAgentProfileName.
     */
    public String getLastAcknowledgedAgentProfileName() {
        return this.lastAcknowledgedAgentProfileName;
    }

    /**
     * Sets the lastAcknowledgedAgentProfileName
     * 
     * @param lastAcknowledgedAgentProfileName
     *            The lastAcknowledgedAgentProfileName to set.
     */
    public void setLastAcknowledgedAgentProfileName(String lastAcknowledgedAgentProfileName) {
        this.lastAcknowledgedAgentProfileName = lastAcknowledgedAgentProfileName;
    }

    /**
     * Returns the lastAcknowledgedCommProfileName.
     * 
     * @return the lastAcknowledgedCommProfileName.
     */
    public String getLastAcknowledgedCommProfileName() {
        return this.lastAcknowledgedCommProfileName;
    }

    /**
     * Sets the lastAcknowledgedCommProfileName
     * 
     * @param lastAcknowledgedCommProfileName
     *            The lastAcknowledgedCommProfileName to set.
     */
    public void setLastAcknowledgedCommProfileName(String lastAcknowledgedCommProfileName) {
        this.lastAcknowledgedCommProfileName = lastAcknowledgedCommProfileName;
    }

    
    protected boolean isAgentProfileUpToDateWith(String agentProfileName, Calendar agentProfileTS) {
        boolean result = false;
        // If profile names don't match, or if the ack timestamp is older than
        // the current timestamp, we send an update:
        if ((this.lastAcknowledgedAgentProfileName != null) && (this.lastAcknowledgedAgentProfileTS != null)) {
            boolean namesEqual = agentProfileName.equals(this.lastAcknowledgedAgentProfileName);
            boolean isUpToDate = !agentProfileTS.after(this.lastAcknowledgedAgentProfileTS);
            result = (namesEqual) && (isUpToDate);
        }
        return result;
    }
    
    /**
     * Returns whether the agent profile status reflects an up-to-date agent
     * 
     * @param agentProfile
     * @return boolean
     */
    public boolean isUpToDateWith(IAgentProfileDO agentProfile) {
        return isAgentProfileUpToDateWith(agentProfile.getName(), agentProfile.getModifiedDate());
    }
    
    protected boolean isCommProfileUpToDateWith(String commProfileName, Calendar commProfileTS) {
        boolean result = false;
        // If profile names don't match, or if the ack timestamp is older than
        // the current timestamp, we send an update:
        if ((this.lastAcknowledgedCommProfileName != null) && (this.lastAcknowledgedCommProfileTS != null)) {
            boolean namesEqual = commProfileName.equals(this.lastAcknowledgedCommProfileName);
            boolean isUpToDate = !commProfileTS.after(this.lastAcknowledgedCommProfileTS);
            result = (namesEqual) && (isUpToDate);
        }
        return result;
    }

    /**
     * Returns whether the comm profile status reflects an up-to-date agent
     * 
     * @param commProfile
     * @return
     */
    public boolean isUpToDateWith(ICommProfileDO commProfile) {
        return isCommProfileUpToDateWith(commProfile.getName(), commProfile.getModifiedDate());
    }
}