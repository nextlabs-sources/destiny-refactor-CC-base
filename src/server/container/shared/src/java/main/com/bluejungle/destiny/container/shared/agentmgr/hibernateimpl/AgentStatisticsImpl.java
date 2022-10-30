/*
 * Created on Feb 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the implementation class for agent statistics
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentStatisticsImpl.java#3 $
 */

public class AgentStatisticsImpl implements IAgentStatistics {

    private Map<IAgentType, Long> agentsCounts = new HashMap<IAgentType, Long>();
    private long heartbeatsInLastDayCount;
    private long agentsDisconnectedInLastDayCount;
    private long agentsWithOutOfDatePolicies;

    /**
     * Returns the total number of agents in the system
     * 
     * @return the total number of agents in the system
     */
    public long getTotalAgentCount() {
        long totalCount = 0;
        for (long nextCount : this.agentsCounts.values()) {
            totalCount += nextCount;
        }

        return totalCount;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics#getAgentCount(com.bluejungle.destiny.container.shared.agentmgr.IAgentType)
     */
    public long getAgentCount(IAgentType agentType) {
        long valueToReturn = 0;
        if (this.agentsCounts.containsKey(agentType)) {
            valueToReturn = this.agentsCounts.get(agentType);
        }

        return valueToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics#getAgentCounts()
     */
    public Map<IAgentType, Long> getAgentCounts() {
        return this.agentsCounts;
    }

    /**
     * Returns the heartbeatsInLastDayCount.
     * 
     * @return the heartbeatsInLastDayCount.
     */
    public long getHeartbeatsInLastDayCount() {
        return this.heartbeatsInLastDayCount;
    }

    /**
     * Returns the agentsDisconnectedInLastDayCount.
     * 
     * @return the agentsDisconnectedInLastDayCount.
     */
    public long getAgentsDisconnectedInLastDayCount() {
        return this.agentsDisconnectedInLastDayCount;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics#getAgentsWithOutOfDatePolicies()
     */
    public long getAgentsWithOutOfDatePolicies() {
        return this.agentsWithOutOfDatePolicies;
    }

    /**
     * Sets the agentsWithOutOfDatePolicies
     * 
     * @param agentsWithOutOfDatePolicies
     *            The agentsWithOutOfDatePolicies to set.
     */
    void setAgentsWithOutOfDatePolicies(long agentsWithOutOfDatePolicies) {
        this.agentsWithOutOfDatePolicies = agentsWithOutOfDatePolicies;
    }

    /**
     * Sets the number of agents for the specified agent type
     * 
     * @param count
     *            number of agents for the specified agent type
     */
    void setAgentCount(IAgentType agentType, long count) {
        if (agentType == null) {
            throw new NullPointerException("agentType cannot be null.");
        }

        this.agentsCounts.put(agentType, count);
    }

    /**
     * Sets the heartbeatsInLastDayCount
     * 
     * @param heartbeatsInLastDayCount
     *            The heartbeatsInLastDayCount to set.
     */
    void setHeartbeatsInLastDayCount(long heartbeatsInLastDayCount) {
        this.heartbeatsInLastDayCount = heartbeatsInLastDayCount;
    }

    /**
     * Sets the agentsDisconnectedInLastDayCount
     * 
     * @param agentsDisconnectedInLastDayCount
     *            The agentsDisconnectedInLastDayCount to set.
     */
    void setAgentsDisconnectedInLastDayCount(long agentsDisconnectedInLastDayCount) {
        this.agentsDisconnectedInLastDayCount = agentsDisconnectedInLastDayCount;
    }
}