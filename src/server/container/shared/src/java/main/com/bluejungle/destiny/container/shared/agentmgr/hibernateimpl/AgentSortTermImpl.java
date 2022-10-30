/*
 * Created on Mar 8, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrSortFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrSortTerm;

/**
 * This is the agent sort term implementation class. It represents one sort term
 * element with the agent query specification.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentSortTermImpl.java#1 $
 */

public class AgentSortTermImpl implements IAgentMgrSortTerm {

    private final AgentMgrSortFieldType fieldName;
    private final boolean ascending;

    /**
     * Constructor
     * 
     * @param fieldName
     *            field to sort on
     * @param asc
     *            sort direction (true if ascending, false if descending)
     */
    public AgentSortTermImpl(AgentMgrSortFieldType fieldName, boolean asc) {
        super();
        this.ascending = asc;
        this.fieldName = fieldName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrSortTerm#getFieldName()
     */
    public AgentMgrSortFieldType getFieldName() {
        return this.fieldName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrSortTerm#isAscending()
     */
    public boolean isAscending() {
        return this.ascending;
    }

}
