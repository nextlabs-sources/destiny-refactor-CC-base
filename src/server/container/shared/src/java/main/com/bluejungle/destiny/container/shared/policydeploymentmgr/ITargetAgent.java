/*
 * Created on Feb 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;

/**
 * This is the target agent interface. There can be many target agents on a
 * given host. Each target agent refers to a "live" agent in the system, and has
 * an deployment execution status.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/ITargetAgent.java#1 $
 */

public interface ITargetAgent {

    /**
     * Returns the agent associated with this record
     * 
     * @return the agent associated with this record
     */
    public IAgentDO getAgent();

    /**
     * Returns the processor of this agent push
     * 
     * @return the processor of this agent push
     */
    public IAgentProcessor getProcessor();

    /**
     * Returns the host that the agent sits in
     * 
     * @return the host that the agent sits in
     */
    public ITargetHost getHost();

    /**
     * Returns the record id
     * 
     * @return the record id
     */
    public Long getId();

    /**
     * Returns the deployment status for this agent
     * 
     * @return the deployment status for this agent
     */
    public ITargetStatus getStatus();
}