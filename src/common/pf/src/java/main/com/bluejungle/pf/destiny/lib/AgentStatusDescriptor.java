package com.bluejungle.pf.destiny.lib;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $ Id: $
 */

import java.util.Date;

import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * Describes the status of an agent.
 */
public class AgentStatusDescriptor {

    /** ID of the agent */
    private final long id;

    /** Name of the host on which the agent resides. */
    private final String hostName;

    /** The type of the agent (client or server). */
    private final AgentTypeEnumType agentType;

    /** The date/time the policies and components on the host were last updated. */
    private final Date lastUpdated;

    /** The number of policies deployed on the agent. */
    private int numPolicies;

    /** The number of components deployed on the agent. */
    private int numComponents;

    /**
     * Constructs a new AgentStatusDescriptor.
     * @param hostName the name of the host on which the agent resides.
     * @param agentType the type of the agent (client or server).
     * @param lastUpdated the date/time the policies and components
     *        on the host were last updated.
     * @param numPolicies the number of policies deployed on the agent.
     * @param numComponents the number of components deployed on the agent.
     */
    public AgentStatusDescriptor(
        long id
    ,   String hostName
    ,   AgentTypeEnumType agentType
    ,   Date lastUpdated
    ,   int numPolicies
    ,   int numComponents
    ) {
        this.id = id;
        this.hostName = hostName;
        this.agentType = agentType;
        this.lastUpdated = UnmodifiableDate.forDate( lastUpdated );
        this.numPolicies = numPolicies;
        this.numComponents = numComponents;
    }

    /**
     * Returns agent ID.
     * @return agent ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Obtain the number of policies deployed on the agent. 
     * @return the number of policies deployed on the agent.
     */
    public int getNumPolicies() {
        return numPolicies;
    }

    /**
     * Adds the required number of policies. 
     */
    public void addNumPolicies( int toAdd ) {
        numPolicies += toAdd;
    }

    /**
     * Obtain the number of components deployed on the agent.
     * @return the number of components deployed on the agent.
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * Obtain the date/time the policies and components on the host
     * were last updated.
     * @return the date/time the policies and components on the host
     * were last updated.
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Obtain the type of the agent (client or server).
     * @return the type of the agent (client or server).
     */
    public AgentTypeEnumType getAgentType() {
        return agentType;
    }

    /**
     * Obtain the name of the host on which the agent resides.
     * @return the name of the host on which the agent resides.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets the number of deployed components.
     * @param numComponents The numComponents to set.
     * This method is package-private. Only PF can access it.
     */
    public void setNumComponents(int numComponents) {
        this.numComponents = numComponents;
    }

    /**
     * Sets the number of deployed policies.
     * @param numPolicies The numPolicies to set.
     * This method is package-private. Only PF can access it.
     */
    void setNumPolicies(int numPolicies) {
        this.numPolicies = numPolicies;
    }

}
