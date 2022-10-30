/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults;

import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery;

/**
 * The result table manager redirects results of a given HQL query to a result
 * table. The result table can then be re-queried to fetch data. The result
 * table manager function is to execute the HQL and place the results in a given
 * table.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/IResultTableManager.java#1 $
 */

public interface IResultTableManager {

    public static final String DATA_SOURCE_CONFIG_PARAM = "dataSource";
    public static final String RESULT_TABLE_NAME_CONFIG_PARAM = "targetTable";

    /**
     * Returns the query definition
     * 
     * @param queryId
     *            if of the query
     * @return the query definition with this id
     */
    public IStoredQuery getStoredQuery(Long queryId);

    /**
     * Executes an HQL query and stores the results in the appropriate result
     * table.
     * 
     * @param queryToStore
     *            the query to execute
     * @param queriedDOClass
     *            the class name of the data object to query
     * @param parameterMap
     *            map of all the parameter names and values
     * @param maxResultsToFetch
     *            maximum number of results to store in the result table
     * @param totalNbOfResults
     *            total number of results for the query (if known, otherwise
     *            -1);
     * @return the id of the stored query
     * @throws ResultTableManagerException
     *             if something failed when execution the query or storing the
     *             results.
     */
    public Long storeResults(IQuery queryToStore, Class<?> queriedDOClass, int maxResultsToFetch, int totalNbOfResults) throws ResultTableManagerException;
}