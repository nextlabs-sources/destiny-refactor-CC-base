/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatus;

/**
 * This is the data object the deployment request.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/policydeploymentmgr/hibernateimpl/DeploymentRequestDO.java#1 $
 */

public class DeploymentRequestDO implements IDeploymentRequest {

    private Long id;
    private boolean executed;
    private List targets = new ArrayList();
    private Calendar time;
    private Calendar scheduleTime;
    

    /**
     * Adds a new target to the list of deployment targets
     * 
     * @param newTarget
     *            new target to be added
     */
    public void addTargetHost(String newHostname) {
        TargetHostDO newTarget = new TargetHostDO();
        newTarget.setHostname(newHostname);
        newTarget.setDeploymentRequest(this);
        newTarget.setStatus(ITargetStatus.NOT_STARTED);
        this.targets.add(newTarget);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest#isExecuted()
     */
    public boolean getExecuted() {
        return this.executed;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest#getTargets()
     */
    public List getTargetHosts() {
        return this.targets;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest#getTime()
     */
    public Calendar getTime() {
        return this.time;
    }
    
	/**
	 * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest#getScheduledTime()
	 */
	public Calendar getScheduleTime() {
		return this.scheduleTime;
	}

    /**
     * Sets a new id for the deployment request.
     * 
     * @param newId
     *            new id value
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets whether a deployment request is executed
     * 
     * @param newExecuted
     *            executed flag
     */
    public void setExecuted(boolean newExecuted) {
        this.executed = newExecuted;
    }

    /**
     * Sets the set of deployment targets associated with the deployment
     * request.
     * 
     * @param newTargets
     *            set of new targets
     */
    public void setTargetHosts(List newTargets) {
        this.targets = newTargets;
    }

    /**
     * Sets the time of the policy deployment request
     * 
     * @param newTime
     *            time to be set
     */
    public void setTime(Calendar newTime) {
        this.time = newTime;
    }

	public void setScheduleTime(Calendar scheduleTime) {
		this.scheduleTime = scheduleTime;
	}
    
    
}