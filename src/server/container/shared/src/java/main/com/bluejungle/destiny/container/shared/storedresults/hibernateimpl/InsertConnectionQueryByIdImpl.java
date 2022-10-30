/*
 * Created on Mar 1, 2005
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
 * This is the insert connection class implementation. This class decorates a
 * real connection, and traps the prepared statements that should be executed on
 * the database. Later on, the caller can "replay" the prepared statement and
 * insert the results in a different table. For now, this connection traps only
 * prepared statement calls, since Hibernate uses mostly prepared statements.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/InsertConnection.java#1 $
 */

class InsertConnectionQueryByIdImpl extends BaseInsertConnection {

    protected static final String ID_COLUMN_NAME = "id";
    protected static final String STORED_QUERY_ID_COLUMN_NAME = "stored_query_id";
    protected static final String RESULT_ID_COLUMN_NAME = "resultId";

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
    public InsertConnectionQueryByIdImpl(Connection realConnection, String resultTableName, Long storedQueryId, String sequenceExpression) {
        super(realConnection, resultTableName, storedQueryId, sequenceExpression);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.BaseInsertConnection#getResultTableColumnNames()
     */
    protected List getResultTableColumnNames() {
        ArrayList list = new ArrayList();
        if (isSequenceEnabled()) {
            list.add(ID_COLUMN_NAME);
        }
        list.add(STORED_QUERY_ID_COLUMN_NAME);
        list.add(RESULT_ID_COLUMN_NAME);
        return list;
    }
}