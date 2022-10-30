/*
 * Created on May 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.List;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentQueryResults;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentQueryResultsImpl.java#1 $
 */

public class AgentQueryResultsImpl implements IAgentQueryResults {

    private IAgentDO[] matchingAgents;

    /**
     * Create an instance of AgentQueryResultsImpl
     * 
     * @param matchingAgents list of agents fetch by the query
     */
    public AgentQueryResultsImpl(List matchingAgentsList) {
        super();

        if (matchingAgentsList == null) {
            throw new NullPointerException("matchingAgentsList cannot be null.");
        }
        
        this.matchingAgents = (IAgentDO[]) matchingAgentsList.toArray(new IAgentDO[matchingAgentsList.size()]);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentQueryResults#getAgents()
     */
    public IAgentDO[] getAgents() {
        return this.matchingAgents;
    }
}
