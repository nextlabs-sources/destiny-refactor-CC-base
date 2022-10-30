/*
 * Created on Mar 8, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentMgrSortTerm.java#1 $
 */

public interface IAgentMgrSortTerm {

    /**
     * Returns the name of the sorted field
     * 
     * @return the name of the sorted field
     */
    public AgentMgrSortFieldType getFieldName();

    /**
     * Returns true if the sort is ascending, false if descending
     * 
     * @return true if the sort is ascending, false if descending
     */
    public boolean isAscending();
}
