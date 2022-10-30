/*
 * Created on May 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This interface exposes statistics about the results returned. It makes sense
 * to look at values exposed in this API only whern the reader is used to query
 * data and group it with a certain dimension.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportResultStatistics.java#1 $
 */

public interface IReportResultStatistics {

    /**
     * Returns the number of rows that can be read
     * 
     * @return the number of rows that can be read
     */
    public Long getAvailableRowCount();

    /**
     * Returns the minimum occurence number of a record. This value makes only
     * sense when a query with some kind of grouping was performed.
     * 
     * @return the minimum number of occurence of a record
     */
    public Long getMinValue();

    /**
     * Returns the maximum occurence number of a record. This value makes only
     * sense when a query with some kind of grouping was performed.
     * 
     * @return the maximum number of occurence of a record
     */
    public Long getMaxValue();

    /**
     * Returns the sum of all record occurences. This value makes only sense
     * when a query with some kind of grouping was performed.
     * 
     * @return the sum of all record occurences.
     */
    public Long getSumValue();

    /**
     * Returns the total number of rows that matched the query specification
     * 
     * @return the total number of rows that matched the query specification
     */
    public Long getTotalRowCount();
}