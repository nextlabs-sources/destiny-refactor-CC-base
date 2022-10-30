/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResultsStatistics;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the base class for the report result reader. It is extended by the
 * various readers' implementations.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseReportResultReader.java#1 $
 */

abstract class BaseReportResultReader implements IReportResultReader {

    private static Log log = LogFactory.getLog(BaseReportResultReader.class.getName());
    private IHibernateRepository dataSource;
    private Session session;
    private IReportResultStatistics stats;
    private IStoredQuery storedQuery;

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
     *            stored query corresponding to these results.
     * @param ds
     *            Data source used.
     * @throws HibernateException
     *             if database issue occurs
     */
    public BaseReportResultReader(Session s, Query q, IHibernateRepository ds, IStoredQuery storedQuery) {
        super();
        if (q == null) {
            throw new NullPointerException("Query parameter cannot be null");
        }
        //No need to check for null here
        this.dataSource = ds;
        this.session = s;
        this.storedQuery = storedQuery;
        this.stats = new ReaderStatistics(storedQuery.getStatistics());
    }

    /**
     * Internal constructor
     * 
     * @param ds
     *            dataSource
     * @param storedQuery
     *            stored query object
     */
    protected BaseReportResultReader(IHibernateRepository ds, IStoredQuery storedQuery) {
        this.dataSource = ds;
        this.storedQuery = storedQuery;
    }

    /**
     * Returns the data source
     * 
     * @return the data source
     */
    protected IHibernateRepository getDataSource() {
        return this.dataSource;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return log;
    }

    /**
     * Returns the session object
     * 
     * @return the session object
     */
    protected Session getSession() {
        return this.session;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#getStatistics()
     */
    public IReportResultStatistics getStatistics() {
        return this.stats;
    }

    /**
     * Returns the stored query object
     * 
     * @return the stored query object
     */
    protected IStoredQuery getStoredQuery() {
        return this.storedQuery;
    }

    /**
     * This inner class wraps around the stored query statistics object and
     * exposes report results statistics to the caller. This inner class could
     * because useful later if more statistics have to be calculated.
     * 
     * @author ihanen
     */
    private class ReaderStatistics implements IReportResultStatistics {

        private IStoredQueryResultsStatistics stats;

        /**
         * Constructor
         * 
         * @param storedQueryStats
         *            stored query statistics to wrap around.
         */
        private ReaderStatistics(IStoredQueryResultsStatistics storedQueryStats) {
            this.stats = storedQueryStats;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getAvailableRowCount()
         */
        public Long getAvailableRowCount() {
            return this.stats.getAvailableRowCount();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getMinValue()
         */
        public Long getMinValue() {
            return this.stats.getMinValue();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getMaxValue()
         */
        public Long getMaxValue() {
            return this.stats.getMaxValue();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getSumValue()
         */
        public Long getSumValue() {
            return this.stats.getSumValue();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getTotalRowCount()
         */
        public Long getTotalRowCount() {
            return this.stats.getTotalRowCount();
        }
    }
}