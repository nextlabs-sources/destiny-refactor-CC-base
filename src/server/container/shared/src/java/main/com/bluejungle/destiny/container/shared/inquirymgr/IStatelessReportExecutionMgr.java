/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.destiny.container.shared.inquirymgr.ILogDetailResult;
import com.nextlabs.destiny.container.shared.inquirymgr.InvalidActivityLogIdException;

/**
 * This is the stateless report execution manager interface. The stateless
 * report execution allows the caller to execute the report in a stateless way.
 * In a stateless way, the caller can come back later on and fetch additionnal
 * records from the query result set.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IStatelessReportExecutionMgr.java#1 $
 */

/**
 * @author rlin
 */
public interface IStatelessReportExecutionMgr extends IReportExecutionMgr {

    /**
     * Executes a report for the first time. This API is supposed to be called
     * by the caller only the first time a report has to be executed.
     * 
     * @param report
     *            report to execute
     * @param fetchRows
     *            maximum number of rows to fetch
     * @param maxStoredRows
     *            maximum number of rows that could ever be fetched for this
     *            query.
     * @return the execution results
     * @throws DataSourceException
     *             if the persistence layer could not be queried properly
     * @throws InvalidReportArgumentException
     *             if the report arguments are not consistent. In some cases,
     *             the implementation may decide to ignore the inconsistent
     *             parameters and ingore them without throwing this exception.
     */
    public IStatelessReportExecutionResult executeReport(IReport report, int fetchRows, int maxStoredRows) throws DataSourceException, InvalidReportArgumentException;

    /**
     * Returns the next set of results
     * 
     * @param state
     *            current state of the query
     * @param fetchRows
     *            maximum number of rows to fetch
     * @return the execution results
     * @throws DataSourceException
     *             if the persistence layer could not be queried properly
     */
    public IStatelessReportExecutionResult gotoNextSet(IReportResultState state, int fetchRows) throws DataSourceException;
    
    /**
     * Returns the details of a single log record.  
     * 
     * TODO: this may have to be moved to the stateful report execution manager because
     *       theoretically the user do not need to first make a report execution and 
     *       come back to get the log detail.  The user can simply supply a log ID directly 
     *       if they know what is going on under the hood.
     * 
     * @param state
     * @param recordId
     * @return the details of a single log record
     * @throws DataSourceException
     */
    public ILogDetailResult getLogDetail(IReport report, long recordId) throws DataSourceException, InvalidActivityLogIdException;
}