/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This is the report result reader interface. The result reader allows walking
 * through a set of results from a report execution.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportResultReader.java#1 $
 */

public interface IReportResultReader {

    /**
     * Terminates the usage of the result object. The caller is supposed to call
     * this function once it has finished reading the results.
     */
    public void close();

    /**
     * Returns true if there are more results to fetch
     * 
     * @return true if there are more results to fetch, false otherwise
     */
    public boolean hasNextResult();

    /**
     * Returns the next result row
     * 
     * @return the next result row
     */
    public IResultData nextResult();

    /**
     * Returns statistics associated with the query results
     * 
     * @return statistics associated with the query results
     */
    public IReportResultStatistics getStatistics();
}