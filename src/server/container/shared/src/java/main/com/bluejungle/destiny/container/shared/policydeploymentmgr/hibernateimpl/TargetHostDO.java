/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetHost;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatus;

/**
 * This is the target host data object class. This class represent a given host
 * that has to be reached in the context of a deployment request.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/policydeploymentmgr/hibernateimpl/DeploymentTargetDO.java#1 $
 */

public class TargetHostDO implements ITargetHost {

    private List agents = new ArrayList();
    private IDeploymentRequest deploymentRequest;
    private String hostname;
    private ITargetStatus status;
    private Long id;

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetHost#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetHost#getAgent()
     */
    public List getAgents() {
        return this.agents;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetHost#getDeploymentRequest()
     */
    public IDeploymentRequest getDeploymentRequest() {
        return this.deploymentRequest;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetHost#getHostname()
     */
    public String getHostname() {
        return this.hostname;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetHost#getStatus()
     */
    public ITargetStatus getStatus() {
        return this.status;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetHost#getStatusId()
     */
    public Long getStatusId() {
        Long result = null;
        if (this.status != null) {
            result = this.status.getId();
        }
        return result;
    }

    /**
     * Sets the agent associated with this deployment target
     * 
     * @param newAgents
     *            agent associated with this deployment target
     */
    public void setAgents(List newAgents) {
        this.agents = newAgents;
    }

    /**
     * Set the deployment request associated with this deployment target
     * 
     * @param deployRequest
     *            deployment request object to be associated
     */
    public void setDeploymentRequest(IDeploymentRequest deployRequest) {
        this.deploymentRequest = deployRequest;
    }

    /**
     * Sets the hostname where the deployment should occur
     * 
     * @param newHostname
     *            hostname
     */
    public void setHostname(String newHostname) {
        this.hostname = newHostname;
    }

    /**
     * Sets the deployment target row id.
     * 
     * @param newId
     *            new row Id to set.
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the status of the deployment target
     * 
     * @param newStatus
     *            new status to be set
     */
    public void setStatus(ITargetStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * Sets the status id (the lookup is temporary)
     * 
     * @param id
     */
    public void setStatusId(Long newId) {
        if (newId == null) {
            return;
        }
        if (newId.equals(ITargetStatus.FAILED.getId())) {
            setStatus(ITargetStatus.FAILED);
        } else if (newId.equals(ITargetStatus.IN_PROGRESS.getId())) {
            setStatus(ITargetStatus.IN_PROGRESS);
        } else if (newId.equals(ITargetStatus.NOT_STARTED.getId())) {
            setStatus(ITargetStatus.NOT_STARTED);
        } else if (newId.equals(ITargetStatus.SUCCEEDED.getId())) {
            setStatus(ITargetStatus.SUCCEEDED);
        }
    }
}