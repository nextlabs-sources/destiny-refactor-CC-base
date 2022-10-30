/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the insert connection for summary queries. This implementation
 * differs because of the SQL statement that gets created for the insert.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/InsertConnectionQuerySummaryImpl.java#1 $
 */

public class InsertConnectionQuerySummaryImpl extends BaseInsertConnection {

    protected static final String ID_COLUMN_NAME = "id";
    protected static final String STORED_QUERY_ID_COLUMN_NAME = "stored_query_id";
    protected static final String VALUE_COLUMN_NAME = "value";
    protected static final String COUNT_COLUMN_NAME = "count";

    /**
     * Constructor
     * 
     * @param realConnection
     *            real SQL connection
     * @param resultTableName
     *            name of the table where result rows should be stored
     * @param storedQueryId
     *            id of the stored query
     * @param sequenceExpression
     *            DB specific expression used to retrieve the sequence number
     */
    public InsertConnectionQuerySummaryImpl(Connection realConnection, String resultTableName, Long storedQueryId, String sequenceExpression) {
        super(realConnection, resultTableName, storedQueryId, sequenceExpression);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.BaseInsertConnection#getResultTableColumnNames()
     */
    protected List<String> getResultTableColumnNames() {
        List<String> list = new ArrayList<String>();
        if (isSequenceEnabled()) {
            list.add(ID_COLUMN_NAME);
        }
        list.add(STORED_QUERY_ID_COLUMN_NAME);
        list.add(VALUE_COLUMN_NAME);
        list.add(COUNT_COLUMN_NAME);
        return list;
    }

}