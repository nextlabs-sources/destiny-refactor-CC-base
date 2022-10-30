/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;

/**
 * IAgentStatisticsCollector is reponsible for collecting statistics associated
 * with Agents
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/IAgentStatisticsCollector.java#2 $
 */
public interface IAgentStatisticsCollector {

    public static final String COMP_NAME = "AgentStatisticsCollector";

    /**
     * Collect the statistics
     * 
     * @return statistics associated with Agents
     * @throws PersistenceException
     *             is a failure occurs while accessing the persistence store
     */
    public IAgentStatistics collectStatistics() throws PersistenceException;
}