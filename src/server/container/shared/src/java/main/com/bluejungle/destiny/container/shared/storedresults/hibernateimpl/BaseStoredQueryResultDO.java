/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResult;

/**
 * This is the base class for the stored query results. All results classes for
 * stored query results extend this base class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/BaseStoredQueryResultDO.java#1 $
 */

abstract class BaseStoredQueryResultDO implements IStoredQueryResult {

    private Long id;
    private IStoredQuery query;

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResult#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResult#getQuery()
     */
    public IStoredQuery getQuery() {
        return this.query;
    }

    /**
     * Sets the result record id
     * 
     * @param newId
     *            new id to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the stored query associated with this result
     * 
     * @param newQuery
     *            new query to set
     */
    public void setQuery(IStoredQuery newQuery) {
        this.query = newQuery;
    }
}