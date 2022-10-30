/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults;

import java.util.Calendar;

/**
 * This is the stored query by id result interface. A stored query by id can
 * link to any type of data object. The name of the data object is needed so
 * that a join can be done between the id and the data object.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/IStoredQuery.java#1 $
 */

public interface IStoredQuery {

    /**
     * Returns the time of the stored query creation
     * 
     * @return the time of the stored query creation
     */
    public Calendar getCreationTime();

    /**
     * Returns the name of the data object associated with the query results
     * 
     * @return
     */
    public String getDataObjectName();

    /**
     * Returns the stored query result id
     * 
     * @return the stored query result id
     */
    public Long getId();

    /**
     * Returns the name of the DO class which id is stored in the result table
     * 
     * @return the name of the DO class
     */
    public String getResultObjectName();

    /**
     * Returns the stored query results statistics
     * 
     * @return the stored query results statistics
     */
    public IStoredQueryResultsStatistics getStatistics();
}