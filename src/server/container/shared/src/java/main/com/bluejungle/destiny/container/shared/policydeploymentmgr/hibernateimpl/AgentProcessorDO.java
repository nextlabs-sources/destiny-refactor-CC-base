/*
 * Created on Feb 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.IAgentProcessor;

/**
 * This is the agent processor data object. The agent processor represents an
 * active DABS component that takes care of pushing policy updates to an agent.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/hibernateimpl/AgentProcessorDO.java#1 $
 */

public class AgentProcessorDO implements IAgentProcessor {

    private Long id;
    private String name;

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IAgentProcessor#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IAgentProcessor#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the new id for the agent processor.
     * 
     * @param newId
     *            new id to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the new component name for the agent processor.
     * 
     * @param newName
     *            new name to set
     */
    public void setName(String newName) {
        this.name = newName;
    }
}