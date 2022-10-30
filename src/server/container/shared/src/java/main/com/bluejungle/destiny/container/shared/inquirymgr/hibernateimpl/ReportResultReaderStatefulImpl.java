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
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the stateful implementation of the report result reader. In this
 * implementation, it is assumed that the caller is in process, or will maintain
 * a reference to the reader as long as it needs the data. This class decorates
 * Hibernate scrollable results.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportResultReaderStatefulImpl.java#1 $
 */

class ReportResultReaderStatefulImpl extends BaseReportResultReader implements IReportResultReader {

    private ScrollableResults scrollResults;
    private boolean triedNext = false;

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
     * @throws HibernateException
     *             if database issue occurs
     */
    public ReportResultReaderStatefulImpl(Session s, Query q, IHibernateRepository ds, IStoredQuery storedQuery) throws HibernateException {
        super(s, q, ds, storedQuery);
        this.scrollResults = q.scroll();

        //Position the result before the first result
        goToFirstResult();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#close()
     */
    public void close() {
        try {
            getScrollResults().close();
            deleteStoredQueryResults();
        } catch (HibernateException e) {
            getLog().error("Error when closing report result reader", e);
        } finally {
            HibernateUtils.closeSession(getSession(), getLog());
        }
    }

    /**
     * Deletes the stored query results associated with this reader. Since this
     * reader is stateful, it is not useful to keep the result records in the
     * table after the reader gets closed.
     * 
     * @throws HibernateException
     *             if data operation fails.
     */
    protected void deleteStoredQueryResults() throws HibernateException {
        Session s = getDataSource().getSession();
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete(getStoredQuery());
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw e;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * Returns the scrollable results
     * 
     * @return the scrollable results
     */
    protected ScrollableResults getScrollResults() {
        return this.scrollResults;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#hasNextResult()
     */
    public boolean hasNextResult() {
        if ( triedNext ) {
            return true;
        }
        try {
            triedNext = getScrollResults().next();
        } catch (HibernateException e) {
            //Not much to do here
            triedNext = false;
        }
        return triedNext;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#nextResult()
     */
    public IResultData nextResult() {
        IResultData result = null;
        try {
            if ( triedNext || getScrollResults().next() ) {
                Object[] resultArray = getScrollResults().get();
                if (resultArray.length > 0) {
                    result = (IResultData) resultArray[0];
                }
            }
        } catch (HibernateException e) {
            result = null;
        }
        triedNext = false;
        return result;
    }

    /**
     * Position the cursor on the first result
     */
    protected void goToFirstResult() {
        try {
            getScrollResults().beforeFirst();
        } catch (HibernateException e) {
            //Not much to do here...
            getLog().error("Error when positioning the result reader before the first result", e);
        }
    }
}