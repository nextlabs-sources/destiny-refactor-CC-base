/*
 * Created on Feb 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import java.util.Map;

/**
 * This interface exposes statistics about the agents
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentStatistics.java#4 $
 */

public interface IAgentStatistics {

    /**
     * Returns the total number of agents in the system
     * 
     * @return the total number of agents in the system
     */
    public long getTotalAgentCount();

    /**
     * Returns the total number of agents of the specified type in the system
     * 
     * @param agentType
     *            the agent type for which to retrieve statistics
     * @return the total number of agents of the specified type in the system
     */
    public long getAgentCount(IAgentType agentType);

    /**
     * Retrieve the total number of agents of each type in the system
     * 
     * @return the total number of agents of each type in the system
     */
    public Map<IAgentType, Long> getAgentCounts();

    /**
     * Returns the heartbeatsInLastDayCount.
     * 
     * @return the heartbeatsInLastDayCount.
     */
    public long getHeartbeatsInLastDayCount();

    /**
     * Returns the agentsDisconnectedInLastDayCount.
     * 
     * @return the agentsDisconnectedInLastDayCount.
     */
    public long getAgentsDisconnectedInLastDayCount();

    /**
     * Returns the number of agents with out of date policies
     * 
     * @return the number of agents with out of date policies
     */
    public long getAgentsWithOutOfDatePolicies();
}
