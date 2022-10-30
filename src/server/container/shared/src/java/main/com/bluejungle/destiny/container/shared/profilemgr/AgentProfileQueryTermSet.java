/*
 * Created on Jan 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * AgentProfileQueryTermSet contains a set of individual AgentProfileQueryTerm
 * instances.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/AgentProfileQueryTermSet.java#1 $
 */
public class AgentProfileQueryTermSet {

    private Set<AgentProfileQueryTerm> agentProfileQueryTermSet = new HashSet<AgentProfileQueryTerm>();

    /**
     * Create an empty AgentProfileQueryTermSet
     */
    public AgentProfileQueryTermSet() {
        super();
    }

    /**
     * Add a query term to this query term set
     * 
     * @param agentProfileQueryTerm
     *            the query term to add
     */
    public void addQueryTerm(AgentProfileQueryTerm agentProfileQueryTerm) {
        if (agentProfileQueryTerm == null) {
            throw new IllegalArgumentException("agentProfileQueryTerm cannot be null");
        }

        agentProfileQueryTermSet.add(agentProfileQueryTerm);
    }

    /**
     * Retrieve an iterator of the query terms in this query term set
     * 
     * @return an iterator of the query terms in this query term set
     */
    public Iterator queryTerms() {
        return agentProfileQueryTermSet.iterator();
    }
}