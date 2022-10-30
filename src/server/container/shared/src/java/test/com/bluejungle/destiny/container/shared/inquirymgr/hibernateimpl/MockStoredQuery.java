/*
 * Created on Apr 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResultsStatistics;

/**
 * This is a mock stored query object
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/MockStoredQuery.java#1 $
 */

class MockStoredQuery implements IStoredQuery {

    private Long id;

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuery#getCreationTime()
     */
    public Calendar getCreationTime() {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuery#getDataObjectName()
     */
    public String getDataObjectName() {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuery#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuery#getResultObjectName()
     */
    public String getResultObjectName() {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuery#getStatistics()
     */
    public IStoredQueryResultsStatistics getStatistics() {
        return null;
    }

    /**
     * Sets a new id
     * 
     * @param newId
     *            id to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

}