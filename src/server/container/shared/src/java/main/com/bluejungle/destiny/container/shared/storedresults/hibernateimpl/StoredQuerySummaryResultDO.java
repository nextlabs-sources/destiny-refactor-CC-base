/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import com.bluejungle.destiny.container.shared.storedresults.IStoredQuerySummaryResult;

/**
 * This is the stored query summary result data object. This data object is used
 * to store summary query results in the result table.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredQuerySummaryResultDO.java#1 $
 */

public class StoredQuerySummaryResultDO extends BaseStoredQueryResultDO implements IStoredQuerySummaryResult {

    private Long id;
    private Long count;
    private String value;

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuerySummaryResult#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuerySummaryResult#getName()
     */
    public Long getCount() {
        return this.count;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuerySummaryResult#getValue()
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the record id
     * 
     * @param newId
     *            new id to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the summary count
     * 
     * @param newName
     *            new summary count
     */
    public void setCount(Long newCount) {
        this.count = newCount;
    }

    /**
     * Sets the summary value
     * 
     * @param newValue
     *            the new summary value to set
     */
    public void setValue(String newValue) {
        this.value = newValue;
    }
}