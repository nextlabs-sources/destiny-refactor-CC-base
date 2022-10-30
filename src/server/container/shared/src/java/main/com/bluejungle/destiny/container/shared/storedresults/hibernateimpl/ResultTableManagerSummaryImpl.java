/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.sql.Connection;

import com.bluejungle.framework.datastore.hibernate.HibernateUtils;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

/**
 * This is the result table manager implementation for summary query. In this
 * implementation, the actual query results are stored in the summary result
 * table. When fetching the result, the caller can query the table content
 * directly and does not need to join against other tables.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/ResultTableManagerSummaryImpl.java#1 $
 */

public class ResultTableManagerSummaryImpl extends BaseResultTableMgrImpl {

    private static final Long ZERO = new Long(0);

    /**
     * name of the class to use to fetch results from the results table.
     */
    private static final String RESULT_OBJECT_NAME = StoredQuerySummaryResultDO.class.getName();

    /**
     * Query to perform to build the query statistics
     */
    private static final String STATS_HQL_QUERY = "select count(result.count), min(result.count), max(result.count), sum(result.count) from StoredQuerySummaryResultDO result where result.query.id = :queryId";

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.BaseResultTableMgrImpl#buildQueryStatistics(net.sf.hibernate.Session,
     *      com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.StoredQueryDO)
     */
    protected void buildQueryStatistics(Session s, StoredQueryDO storedQuery, int totalNbOfResults) throws HibernateException {
        Long queryId = storedQuery.getId();
        Query q = s.createQuery(STATS_HQL_QUERY);
        q.setLong("queryId", queryId.longValue());
        Object[] result = (Object[]) q.uniqueResult();
        Integer count = (Integer) result[0];
        Long minValue = (Long) result[1];
        if (minValue == null) {
            minValue = ZERO;
        }
        Long maxValue = (Long) result[2];
        if (maxValue == null) {
            maxValue = ZERO;
        }

        Long sumValue = (Long) result[3];
        if (sumValue == null) {
            sumValue = ZERO;
        }
        Transaction t = null;
        try {
            t = s.beginTransaction();
            StoredQueryResultsStatisticsDO stats = new StoredQueryResultsStatisticsDO();
            stats.setAvailableRowCount(new Long(count.longValue()));
            stats.setMinValue(minValue);
            stats.setMaxValue(maxValue);
            stats.setSumValue(new Long(totalNbOfResults));
            stats.setTotalRowCount(stats.getAvailableRowCount());
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
        return new InsertConnectionQuerySummaryImpl(realCon, tableName, queryId, getSequenceExpression());
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.BaseResultTableMgrImpl#getResultObjectName()
     */
    protected String getResultObjectName() {
        return RESULT_OBJECT_NAME;
    }
}