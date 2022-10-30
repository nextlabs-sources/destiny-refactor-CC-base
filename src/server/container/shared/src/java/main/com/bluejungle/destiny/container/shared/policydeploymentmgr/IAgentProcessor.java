/*
 * Created on Feb 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

/**
 * This is the agent processor class. The agent processor is a component that
 * actively pushes policy updates to a set of agents.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/IAgentProcessor.java#1 $
 */

public interface IAgentProcessor {

    /**
     * Returns the id of the agent processor
     * 
     * @return the id of the agent processor
     */
    public Long getId();

    /**
     * Returns the name of the agent processor
     * 
     * @return the name of the agent processor
     */
    public String getName();
}