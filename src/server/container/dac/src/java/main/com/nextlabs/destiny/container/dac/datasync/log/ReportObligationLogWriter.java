/*
 * Created on Jun 15, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/log/ReportObligationLogWriter.java#1 $
 */

public class ReportObligationLogWriter extends ReportLogWriterBase<ReportObligationLog> {

    //they need to be public, the result of <code>log</code> depends on them.
    public static final int INSERT_LOG_QUERY_INDEX          = 0;
    public static final int UPDATE_SOURCE_TABLE_QUERY_INDEX = 1;
    
    private final String insertLogQuery;
    
    public ReportObligationLogWriter() {
        super(String.format(UPDATE_SOURCE_TABLE_QUERY_TEMPLATE,
                SharedLib.PA_OBLIGATION_TABLE));
        this.insertLogQuery = ReportObligationLog.INSERT_LOG_QUERY;
    }

    @Override
    protected PreparedStatement[] createPerpareStatements(Connection conn) throws SQLException,
            DataSourceException {
        PreparedStatement[] statements = new PreparedStatement[2];
        statements[INSERT_LOG_QUERY_INDEX]          = conn.prepareStatement(insertLogQuery);
        statements[UPDATE_SOURCE_TABLE_QUERY_INDEX] = conn.prepareStatement(updateSourceTableQuery);
        return statements;
    }

    @Override
    protected void setValues(PreparedStatement statement, ReportObligationLog row, int indexOfStatement)
            throws SQLException, DataSourceException {
        switch (indexOfStatement) {
        case INSERT_LOG_QUERY_INDEX:
            row.setValue(statement);
            break;
        case UPDATE_SOURCE_TABLE_QUERY_INDEX:
            setUpdateSourceTableValue(statement, row);
            break;
        default:
            throw new IllegalArgumentException("unknown index: " + indexOfStatement);
        }
    }
}
