/*
 * Created on Feb 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is the report execution manager. The report execution manager executes
 * reports and returns results to the caller.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportExecutionMgr.java#1 $
 */

public interface IReportExecutionMgr {

    String COMP_NAME = "reportExecutionMgr";
    PropertyKey<IHibernateRepository> DATA_SOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("dataSource");

    /**
     * Executes a report
     * 
     * @param report
     *            report to execute
     * @return result for the report execution
     * @throws DataSourceException
     *             if the persistence layer failed during the query execution.
     * @throws InvalidReportArgumentException
     *             if some arguments in the report are not consistent. In some
     *             cases though, the implementation could decide to swallow the
     *             bad arguments and run the report after ignoring them.
     */
    public IReportResultReader executeReport(IReport report) throws InvalidReportArgumentException, DataSourceException;

    /**
     * Executes a report
     * 
     * @param report
     *            report to execute
     * @param maxFetchRows
     *            maximum number of rows to fetch.
     * @return result for the report execution
     * @throws DataSourceException
     *             if the persistence layer failed during the query execution.
     * @throws InvalidReportArgumentException
     *             if some arguments in the report are not consistent. In some
     *             cases though, the implementation could decide to swallow the
     *             bad arguments and run the report after ignoring them.
     */
    public IReportResultReader executeReport(IReport report, int maxFetchRows) throws InvalidReportArgumentException, DataSourceException;
}