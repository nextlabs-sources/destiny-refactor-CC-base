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
 * CommProfileQueryTermSet contains a set of individual CommProfileQueryTerm
 * instances.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/CommProfileQueryTermSet.java#1 $
 */
public class CommProfileQueryTermSet {

    private Set<CommProfileQueryTerm> commProfileQueryTermSet = new HashSet<CommProfileQueryTerm>();

    /**
     * Create an empty CommProfileQueryTermSet
     */
    public CommProfileQueryTermSet() {
        super();
    }

    /**
     * Add a query term to this query term set
     * 
     * @param commProfileQueryTerm
     *            the query term to add
     */
    public void addQueryTerm(CommProfileQueryTerm commProfileQueryTerm) {
        commProfileQueryTermSet.add(commProfileQueryTerm);
    }

    /**
     * Retrieve an iterator of the query terms in this query term set
     * 
     * @return an iterator of the query terms in this query term set
     */
    public Iterator queryTerms() {
        return commProfileQueryTermSet.iterator();
    }
}