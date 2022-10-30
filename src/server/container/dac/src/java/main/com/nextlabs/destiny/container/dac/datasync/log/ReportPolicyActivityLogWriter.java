/*
 * Created on Jun 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/log/ReportPolicyActivityLogWriter.java#1 $
 */

public class ReportPolicyActivityLogWriter extends ReportLogWriterBase<ReportPolicyActivityLog>{
    
	//they need to be public, the result of <code>log</code> depends on them.    
    public static final int INSERT_LOG_QUERY_INDEX          = 0;
    public static final int INSERT_CUSTOM_ATTR_QUERY_INDEX  = 1;
    public static final int UPDATE_SOURCE_TABLE_QUERY_INDEX = 2;
    
    private final String insertLogQuery;
    private final String insertCustomAttrQuery;

    @Override
    protected PreparedStatement[] createPerpareStatements(Connection conn) throws SQLException {
        PreparedStatement[] statements = new PreparedStatement[3];
        statements[INSERT_LOG_QUERY_INDEX]          = conn.prepareStatement(insertLogQuery);
        statements[INSERT_CUSTOM_ATTR_QUERY_INDEX]  = conn.prepareStatement(insertCustomAttrQuery);
        statements[UPDATE_SOURCE_TABLE_QUERY_INDEX] = conn.prepareStatement(updateSourceTableQuery);
        return statements;
    }
    
    @Override
    protected void setValues(PreparedStatement statement, ReportPolicyActivityLog row,
            int indexOfStatement) throws SQLException {
        switch (indexOfStatement) {
        case INSERT_LOG_QUERY_INDEX:
            row.setValue(statement);
            break;
        case INSERT_CUSTOM_ATTR_QUERY_INDEX:
            row.setCustomAttributesValue(statement);
            break;
        case UPDATE_SOURCE_TABLE_QUERY_INDEX:
            setUpdateSourceTableValue(statement, row);
            break;
        default:
            throw new IllegalArgumentException("unknown index: " + indexOfStatement);
        }
    }
	
	public ReportPolicyActivityLogWriter(String insertLogQuery) {
		super(String.format(UPDATE_SOURCE_TABLE_QUERY_TEMPLATE,
                        SharedLib.PA_TABLE));
		this.insertLogQuery = insertLogQuery;
		this.insertCustomAttrQuery = ReportPolicyActivityLog.INSERT_CUSTOM_ATTR_QUERY;
    }
}
