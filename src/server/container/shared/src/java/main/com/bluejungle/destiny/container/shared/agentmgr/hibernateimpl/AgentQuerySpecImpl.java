/*
 * Created on Mar 8, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTerm;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrSortTerm;

/**
 * This is the agent manager query specification implementation class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentQuerySpecImpl.java#1 $
 */

public class AgentQuerySpecImpl implements IAgentMgrQuerySpec {

    private final List searchSpecList;
    private final List sortSpecList;
    private int limit = 0;

    /**
     * Constructor
     */
    public AgentQuerySpecImpl() {
        super();
        
        this.searchSpecList = new ArrayList();
        this.sortSpecList = new ArrayList();
    }

    
    /**
     * Create an instance of AgentQuerySpecImpl
     * @param searchSpecList
     * @param sortSpecList
     * @param limit
     */
    public AgentQuerySpecImpl(List searchSpecList, List sortSpecList, int limit) {
        super();
        this.searchSpecList = searchSpecList;
        this.sortSpecList = sortSpecList;
        this.limit = limit;
    }


    /**
     * Adds a new search spec term
     * 
     * @param newTerm
     *            new term to add
     */
    public void addSearchSpecTerm(IAgentMgrQueryTerm newTerm) {
        this.searchSpecList.add(newTerm);
    }

    /**
     * Adds a new search spec term
     * 
     * @param newTerm
     *            new term to add
     */
    public void addSortSpecTerm(IAgentMgrSortTerm newTerm) {
        this.sortSpecList.add(newTerm);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec#getSearchSpecTerms()
     */
    public IAgentMgrQueryTerm[] getSearchSpecTerms() {
        return (IAgentMgrQueryTerm[]) this.searchSpecList.toArray(new IAgentMgrQueryTerm[this.searchSpecList.size()]);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec#getSortSpecTerms()
     */
    public IAgentMgrSortTerm[] getSortSpecTerms() {
        return (IAgentMgrSortTerm[]) this.sortSpecList.toArray(new IAgentMgrSortTerm[this.sortSpecList.size()]);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec#getLimit()
     */
    public int getLimit() {
        return this.limit;
    }

    /**
     * Sets the limit for the query results (0 indicates no limit)
     * 
     * @param newLimit
     *            new limit to set
     */
    public void setLimit(int newLimit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be < 0");
        }
        this.limit = newLimit;
    }

}
