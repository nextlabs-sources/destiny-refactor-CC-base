/*
 * Created on Feb 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.IAgentProcessor;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetHost;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatus;

/**
 * This is the target agent data object.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/hibernateimpl/TargetAgentDO.java#1 $
 */

public class TargetAgentDO implements ITargetAgent {

    private Long id;
    private IAgentDO agent;
    private IAgentProcessor processor;
    private ITargetHost host;
    private ITargetStatus status;

    /**
     * Constructor
     */
    public TargetAgentDO() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent#getAgent()
     */
    public IAgentDO getAgent() {
        return this.agent;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent#getProcessor()
     */
    public IAgentProcessor getProcessor() {
        return this.processor;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent#getHost()
     */
    public ITargetHost getHost() {
        return this.host;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent#getStatus()
     */
    public ITargetStatus getStatus() {
        return this.status;
    }

    /**
     * Returns the status id
     * 
     * @return the status id
     */
    public Long getStatusId() {
        Long result = null;
        if (this.status != null) {
            result = this.status.getId();
        }
        return result;
    }

    /**
     * Associates an agent
     * 
     * @param newAgent
     *            new agent
     */
    public void setAgent(IAgentDO newAgent) {
        this.agent = newAgent;
    }

    /**
     * Sets the processor object
     * 
     * @param newProcessor
     *            new procesor object to set
     */
    public void setProcessor(IAgentProcessor newProcessor) {
        this.processor = newProcessor;
    }

    /**
     * Associates a host with the target agent
     * 
     * @param newHost
     *            new host to associate with the target agent
     */
    public void setHost(ITargetHost newHost) {
        this.host = newHost;
    }

    /**
     * Sets the record row id
     * 
     * @param newId
     *            record row id
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the deployment status for the target agent
     * 
     * @param newStatus
     *            new status
     */
    public void setStatus(ITargetStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * Sets the status id.
     * 
     * @param newId
     *            new status id to set
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