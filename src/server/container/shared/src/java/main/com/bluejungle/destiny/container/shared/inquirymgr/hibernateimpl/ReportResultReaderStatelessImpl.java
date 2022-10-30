/*
 * Created on Mar 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionResult;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the stateless implementation of the report result reader. In this
 * implementation, the caller is typically a web service. The caller typically
 * wants only a subset of the data and comes back later to fetch other subset is
 * necessary. In this implementation, the results remain stored in the result
 * table, until programatically deleted by the caller, or by some other task
 * (based on the stored query creation time). The stateless reader needs to
 * query the result table and fetch only a maximum number records, which record
 * id are greater (or lower) than a certain value
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportResultReaderStatefulImpl.java#1 $
 */

class ReportResultReaderStatelessImpl extends ReportResultReaderStatefulImpl implements IStatelessReportExecutionResult {

    private IReportResultState cachedResultState;

    /**
     * Constructor
     * 
     * @param s
     *            session object associated with the scrollable results. If the
     *            session is null, then it is assumed that the session
     *            management is done by the caller, and this class does not have
     *            to worry about it.
     * 
     * @param q
     *            query object. The caller is supposed to have fully setup the
     *            query object before passing it to the result reader.
     * @param storedQuery
     *            stored query corresponding to these results. When the reader
     *            gets closed, the query results can be deleted.
     * @param ds
     *            data source used. This is useful to deleted the stored results
     *            query
     * @param currentState
     *            current state of the query, in order to initialize the reader
     * @throws HibernateException
     *             if database issue occurs
     */
    public ReportResultReaderStatelessImpl(Session s, Query q, IHibernateRepository ds, IStoredQuery storedQuery, IReportResultState currentState) throws HibernateException {
        super(s, q, ds, storedQuery);
        this.cachedResultState = buildCurrentState(getScrollResults(), currentState);
        goToFirstResult();
    }

    /**
     * Builds the current state of the reader and returns it to the caller.
     * 
     * @param results
     *            scrollable results to use in order to build the new state
     * @param oldState
     *            Current state before the query executed
     * @return
     */
    protected IReportResultState buildCurrentState(ScrollableResults results, IReportResultState oldState) {
        ReportResultStateImpl newState = new ReportResultStateImpl(oldState);
        try {
            results.beforeFirst();
            //Pick the first record in the scrollable results
            boolean firstOk = getScrollResults().first();
            if (firstOk) {
                Object[] resultArray = getScrollResults().get();
                if (resultArray.length > 0) {
                    IResultData result = (IResultData) resultArray[0];
                    //We know for sure what type of object we are dealing with
                    // here
                    if (result instanceof BaseReportActivityResultDO) {
                        newState.setFirstRowSequenceId(((BaseReportActivityResultDO) result).getStoredResultId());
                    } else if (result instanceof ReportSummaryResultDO) {
                        newState.setFirstRowSequenceId(((ReportSummaryResultDO) result).getId());
                    }
                }
            }

            //Pick the last record in the scrollable results
            boolean lastOk = results.last();
            if (lastOk) {
                Object[] resultArray = results.get();
                if (resultArray.length > 0) {
                    IResultData result = (IResultData) resultArray[0];
                    if (result instanceof BaseReportActivityResultDO) {
                        newState.setLastRowSequenceId(((BaseReportActivityResultDO) result).getStoredResultId());
                    } else if (result instanceof ReportSummaryResultDO) {
                        newState.setLastRowSequenceId(((ReportSummaryResultDO) result).getId());
                    }
                }
            }
        } catch (HibernateException e) {
            getLog().error("Error when building the current state", e);
            //Error occured, keep the same state around
        }
        return newState;
    }

    /**
     * In this implementation, the close function does not destroy the results
     * from the result table. The current set of results and other stateful
     * objects are recycled, but the query results stay in the database. This
     * will allow subsequent calls to resume fetching the data.
     */
    public void close() {
        try {
            getScrollResults().close();
        } catch (HibernateException e) {
            getLog().error("Error when closing scrollable results", e);
        } finally {
            HibernateUtils.closeSession(getSession(), getLog());
        }
        this.cachedResultState = null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionResult#getResultState()
     */
    public IReportResultState getResultState() {
        return this.cachedResultState;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Current state: " + this.cachedResultState;
    }
}
