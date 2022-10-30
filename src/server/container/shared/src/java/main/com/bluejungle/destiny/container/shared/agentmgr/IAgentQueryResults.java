/*
 * Created on May 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

/**
 * IAgentQueryResults represents the results of an Agent Query
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentQueryResults.java#1 $
 */
public interface IAgentQueryResults {

    /**
     * Retrieve an array of Agents which matched the specified query
     * 
     * @return an array of Agents which matched the specified query
     */
    public IAgentDO[] getAgents();
}
