/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResultsStatistics;

/**
 * This is the base implementation for the stored query data objects.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredQueryDO.java#1 $
 */

class StoredQueryDO implements IStoredQuery {

    private Calendar creationTime;
    private Long id;
    private String dataObjectName;
    private String resultObjectName;
    private IStoredQueryResultsStatistics stats;
    private List idResults = new ArrayList();
    private List summaryResults = new ArrayList();

    /**
     * Constructor. This function sets the creation time to "now" by default
     */
    public StoredQueryDO() {
        super();
        setCreationTime(Calendar.getInstance());
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuery#getCreationTime()
     */
    public Calendar getCreationTime() {
        return this.creationTime;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuery#getDataObjectName()
     */
    public String getDataObjectName() {
        return this.dataObjectName;
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
        return this.resultObjectName;
    }

    /**
     * Returns the list of results
     * 
     * @return the list of results
     */
    public List getIdResults() {
        return this.idResults;
    }

    /**
     * Returns the list of summary results
     * 
     * @return the list of summary results
     */
    public List getSummaryResults() {
        return this.summaryResults;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuery#getStatistics()
     */
    public IStoredQueryResultsStatistics getStatistics() {
        return this.stats;
    }

    /**
     * Sets the creation time
     * 
     * @param newTime
     *            new creation time to set
     */
    public void setCreationTime(Calendar newTime) {
        this.creationTime = newTime;
    }

    /**
     * Sets the name of the data object
     * 
     * @param newDOName
     *            data object name to set
     */
    public void setDataObjectName(String newDOName) {
        this.dataObjectName = newDOName;
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
     * Sets the result data object name
     * 
     * @param newResultDOName
     *            new result data object name to set
     */
    public void setResultObjectName(String newResultDOName) {
        this.resultObjectName = newResultDOName;
    }

    /**
     * Sets the list of id results for this query
     * 
     * @param newIdResults
     *            list of id results to set
     */
    public void setIdResults(List newIdResults) {
        this.idResults = newIdResults;
    }

    /**
     * Sets the list of summary results for this query
     * 
     * @param newSummaryResults
     *            list of summary results to set
     */
    public void setSummaryResults(List newSummaryResults) {
        this.summaryResults = newSummaryResults;
    }

    /**
     * Sets the new statistics for the stored query results
     * 
     * @param newStats
     *            new statistics to set
     */
    public void setStatistics(IStoredQueryResultsStatistics newStats) {
        this.stats = newStats;
    }
}