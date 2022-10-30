/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.sql.Connection;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.datastore.hibernate.HibernateUtils;

/**
 * This is the result table manager implementation for query by id.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/ResultTableManagerQueryByIdImpl.java#1 $
 */

public class ResultTableManagerQueryByIdImpl extends BaseResultTableMgrImpl {

    /**
     * name of the class to use to fetch results from the results table.
     */
    private static final String RESULT_OBJECT_NAME = StoredQueryByIdResultDO.class.getName();

    /**
     * Query to perform to build the query statistics
     */
    private static final String STATS_HQL_QUERY = "select count(result.id) from StoredQueryByIdResultDO result where result.query.id = :queryId";

    /**
     * Constants used for building the statistics
     */
    private static final Long ONE = new Long(1);
    private static final Long ZERO = new Long(0);

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.BaseResultTableMgrImpl#buildQueryStatistics(net.sf.hibernate.Session,
     *      com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.StoredQueryDO)
     */
    protected void buildQueryStatistics(Session s, StoredQueryDO storedQuery, int totalNbOfResults) throws HibernateException {
        Long queryId = storedQuery.getId();
        Query q = s.createQuery(STATS_HQL_QUERY);
        q.setLong("queryId", queryId.longValue());
        Integer result = (Integer) q.uniqueResult();
        Transaction t = null;
        try {
            t = s.beginTransaction();
            StoredQueryResultsStatisticsDO stats = new StoredQueryResultsStatisticsDO(storedQuery.getStatistics());
            stats.setAvailableRowCount(new Long(result.longValue()));
            stats.setMinValue(ZERO);
            stats.setMaxValue(ONE);
            stats.setSumValue(new Long(result.longValue()));
            storedQuery.setStatistics(stats);
            s.update(storedQuery);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw e;
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.BaseResultTableMgrImpl#getNewInsertConnection(java.sql.Connection,
     *      java.lang.String, java.lang.Long)
     */
    protected Connection getNewInsertConnection(Connection realCon, String tableName, Long queryId) {

        return new InsertConnectionQueryByIdImpl(realCon, tableName, queryId, getSequenceExpression());
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.BaseResultTableMgrImpl#getResultObjectName()
     */
    protected String getResultObjectName() {
        return RESULT_OBJECT_NAME;
    }
}