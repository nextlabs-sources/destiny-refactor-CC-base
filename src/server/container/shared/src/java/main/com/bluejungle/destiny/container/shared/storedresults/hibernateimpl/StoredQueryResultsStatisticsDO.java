/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResultsStatistics;

/**
 * This is the query summary result statistics data object. It exposes
 * statistics about the query summary results.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredQuerySummaryResultsStatisticsDO.java#1 $
 */

public class StoredQueryResultsStatisticsDO implements IStoredQueryResultsStatistics {

    private Long availRowCount;
    private Long minValue;
    private Long maxValue;
    private Long sumValue;
    private Long totalRowCount;

    /**
     * Default Constructor
     */
    public StoredQueryResultsStatisticsDO() {
        super();
    }

    /**
     * Copy Constructor
     * 
     * @param stats
     *            statistics to copy from
     */
    public StoredQueryResultsStatisticsDO(IStoredQueryResultsStatistics stats) {
        super();
        this.availRowCount = stats.getAvailableRowCount();
        this.minValue = stats.getMinValue();
        this.maxValue = stats.getMaxValue();
        this.sumValue = stats.getSumValue();
        this.totalRowCount = stats.getTotalRowCount();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResultsStatistics#getAvailableRowCount()
     */
    public Long getAvailableRowCount() {
        return this.availRowCount;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuerySummaryResultsStatistics#getMinValue()
     */
    public Long getMinValue() {
        return this.minValue;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuerySummaryResultsStatistics#getMaxValue()
     */
    public Long getMaxValue() {
        return this.maxValue;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQuerySummaryResultsStatistics#getSumValue()
     */
    public Long getSumValue() {
        return this.sumValue;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResultsStatistics#getTotalRowCount()
     */
    public Long getTotalRowCount() {
        return this.totalRowCount;
    }

    /**
     * Sets the number of available records to fetch
     * 
     * @param newCount
     *            new count to set
     */
    public void setAvailableRowCount(Long newCount) {
        this.availRowCount = newCount;
    }

    /**
     * Sets the minimum summary count
     * 
     * @param newValue
     *            new count to set
     */
    public void setMinValue(Long newValue) {
        this.minValue = newValue;
    }

    /**
     * Sets the maximum summary count
     * 
     * @param newValue
     *            new count to set
     */
    public void setMaxValue(Long newValue) {
        this.maxValue = newValue;
    }

    /**
     * Sets the sum of all the summary counts
     * 
     * @param sumValue
     *            new sum to set
     */
    public void setSumValue(Long sumValue) {
        this.sumValue = sumValue;
    }

    /**
     * Sets the total row count
     * 
     * @param newValue
     *            new count to set
     */
    public void setTotalRowCount(Long newValue) {
        this.totalRowCount = newValue;
    }
}