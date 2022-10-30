/*
 * Created on Oct 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationDO;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentRegistrationDO.java#1 $
 */

public class AgentRegistrationDO implements IAgentRegistrationDO {

    private Long id;
    private Calendar registrationTime;
    private AgentDO agent;

    /**
     * Constructor
     *  
     */
    public AgentRegistrationDO() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationDO#getTime()
     */
    public Calendar getRegistrationTime() {
        return this.registrationTime;
    }

    /**
     * Sets the registration time
     * 
     * @param time
     */
    public void setRegistrationTime(Calendar time) {
        this.registrationTime = time;
    }

    /**
     * @see com.bluejungle.framework.domain.IHasId#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id
     * 
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the agent.
     * 
     * @return the agent.
     */
    public AgentDO getAgent() {
        return this.agent;
    }

    /**
     * Sets the agent
     * 
     * @param agent
     *            The agent to set.
     */
    public void setAgent(AgentDO agent) {
        this.agent = agent;
    }
}