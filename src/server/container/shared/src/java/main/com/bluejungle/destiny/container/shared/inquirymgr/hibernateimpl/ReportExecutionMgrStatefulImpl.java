/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.storedresults.IResultTableManager;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;

/**
 * This is the in memory implementation for report execution. In this
 * implementation, the results are pulled directly from the real tables in the
 * database, and returned to the caller without going to any result table. The
 * result are wrapped inside our own result interface, so that Hibernate is not
 * known to the caller.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionMgrImpl.java#1 $
 */

public class ReportExecutionMgrStatefulImpl extends BaseReportExecutionMgrStatefulImpl implements IReportExecutionMgr {

    /**
     * Constructor
     */
    public ReportExecutionMgrStatefulImpl() {
        super();
    }

    /**
     * Fetches the query results from the result table. In this implementation,
     * scrollable results are used to browse through the data.
     * 
     * @param queryId
     *            if of the query to look for
     * @param report
     *            report that generated these results
     * @return a browsable, stateful list of results.
     * @throws HibernateException
     *             if the database operation fails
     */
    protected IReportResultReader fetchQueryResults(Long queryId, IReport report) throws HibernateException {
        if (report == null) {
            throw new NullPointerException("Report cannot be null");
        }

        IReportResultReader result = null;
        IResultTableManager resultMgr = getResultTableMgr(report);
        IStoredQuery query = resultMgr.getStoredQuery(queryId);

        //Create an HQL query to fetch the real results from the result table
        // and return them to the caller.
        String hqlExpression = buildFetchQuery(report);

        Session s = getDataSource().getSession();
        Query q = s.createQuery(hqlExpression);
        q.setParameter("queryId", queryId);

        //Creates a reader object for the results
        result = new ReportResultReaderStatefulImpl(s, q, getDataSource(), query);
        return result;
    }
}