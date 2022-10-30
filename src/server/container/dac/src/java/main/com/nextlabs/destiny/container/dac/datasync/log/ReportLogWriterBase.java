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

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.datastore.hibernate.BatchWriter;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/log/ReportLogWriterBase.java#1 $
 */

public abstract class ReportLogWriterBase<T extends ReportLog> extends BatchWriter<T> {
    private static final int ACTIVITY_LOG_SYNC_DONE_COLUMN;
    private static final int ACTIVITY_LOG_ID_COLUMN;
    protected static final String UPDATE_SOURCE_TABLE_QUERY_TEMPLATE;
    
    static{
        int i = 1;
        ACTIVITY_LOG_SYNC_DONE_COLUMN = i++;
        ACTIVITY_LOG_ID_COLUMN = i++;
        
        UPDATE_SOURCE_TABLE_QUERY_TEMPLATE = "update %s set sync_done = ? where id = ?";
    }
    
    protected final String updateSourceTableQuery;
    
    public ReportLogWriterBase(String updateSourceTableQuery) {
        super();
        this.updateSourceTableQuery = updateSourceTableQuery;
    }
    
    protected void setUpdateSourceTableValue(PreparedStatement statement, T entry)
            throws SQLException {
        statement.setBoolean(ACTIVITY_LOG_SYNC_DONE_COLUMN, true);
        statement.setLong(   ACTIVITY_LOG_ID_COLUMN,        entry.id);
        statement.addBatch();
    }

    @Override
    protected void insertIndividuallyFailed(T row, Session session,
            SQLException se) throws DataSourceException {
        // throw an exception if it is duplicated log
        super.insertIndividuallyFailed(row, session, se);
        
        try {
            Connection conn = session.connection();
            Transaction t = session.beginTransaction();
            PreparedStatement statement = null;
            try {
                statement = conn.prepareStatement(updateSourceTableQuery);
                statement.setBoolean(ACTIVITY_LOG_SYNC_DONE_COLUMN, false);
                statement.setLong(ACTIVITY_LOG_ID_COLUMN, row.id);
                statement.executeUpdate();
                t.commit();
            } catch(SQLException e){
                t.rollback();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        getLog().warn("Can't close update statement", e);
                    }
                }
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
    }
}
