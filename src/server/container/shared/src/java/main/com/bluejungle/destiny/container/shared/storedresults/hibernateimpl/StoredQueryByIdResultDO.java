/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryByIdResult;

/**
 * This is the data object for the query by id result record. Each instance of
 * this class represents one record in the result table for query by id.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredQueryByIdResultDO.java#1 $
 */

public class StoredQueryByIdResultDO implements IStoredQueryByIdResult {

    private Long id;
    private IStoredQuery query;
    private Long resultId;

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
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQueryByIdResult#getResultId()
     */
    public Long getResultId() {
        return this.resultId;
    }

    /**
     * Sets the id for this record
     * 
     * @param newId
     *            id to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the stored query associated with this result record
     * 
     * @param newQuery
     *            query to set
     */
    public void setQuery(IStoredQuery newQuery) {
        this.query = newQuery;
    }

    /**
     * Sets the result id. This id is the id of the real record that has been
     * returned by the stored query.
     * 
     * @param newResultId
     *            new result id to set.
     */
    public void setResultId(Long newResultId) {
        this.resultId = newResultId;
    }
}