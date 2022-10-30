/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults;

/**
 * This is the stored query results statistics interface. This interface exposes
 * a few statistics about the result set returned by the stored query.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/IStoredQueryResultsStatistics.java#1 $
 */

public interface IStoredQueryResultsStatistics {

    /**
     * Returns the number of rows available to be fetched
     * 
     * @return the number of rows available to be fetched
     */
    public Long getAvailableRowCount();

    /**
     * Returns the minimum value for the summary count
     * 
     * @return the minimum value for the summary count
     */
    public Long getMinValue();

    /**
     * Returns the maximum value for the summary count
     * 
     * @return the maximum value for the summary count
     */
    public Long getMaxValue();

    /**
     * Returns the sum of all the values for the summary count
     * 
     * @return the sum of all the values for the summary count
     */
    public Long getSumValue();

    /**
     * Returns the total number of rows matching in the query
     * 
     * @return the total number of rows matching in the query
     */
    public Long getTotalRowCount();
}