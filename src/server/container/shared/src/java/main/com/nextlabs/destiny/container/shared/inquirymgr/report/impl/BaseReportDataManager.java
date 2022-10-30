/*
 * Created on Mar 31, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.report.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.nextlabs.report.datagen.IReportDataManager;
import com.nextlabs.report.datagen.ResultData;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/report/impl/BaseReportDataManager.java#1 $
 */

public abstract class BaseReportDataManager implements IReportDataManager{
    // should use the value from ICustomReportPageBean but the dependence is too much
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ssZ";
    
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
    static{
        DATE_FORMATTER.setLenient(false);
    }
    
    // we don't use timestamp with timezone in mssql
    private static final String MSSQL_DATE_FORMAT = DATE_FORMAT.replace("Z", ""); 
    
    private static final DateFormat MSSQL_DATE_FORMATTER = new SimpleDateFormat(MSSQL_DATE_FORMAT);
   
    protected Integer version;
    
    protected abstract Connection getConnection() throws Exception;
    protected abstract void closeConnection() throws Exception;
    
    @Override
    public ResultData runQuery(String query) throws Exception {
        Connection connection = getConnection();
        String[] columnName;
        List<Object[]> values;
        try{
            Statement statement = connection.createStatement();
            try{
                ResultSet rs = statement.executeQuery(query);
                try {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    columnName = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        columnName[i] = rsmd.getColumnLabel(i + 1);
                    }
                    values = new LinkedList<Object[]>();
                    while (rs.next()) {
                        Object[] value = new Object[columnCount];
                        for (int i = 0; i < columnCount; i++) {
                            value[i] = rs.getObject(i + 1);
                        }
                        values.add(value);
                    }
                } finally {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        logExcpetion(e);
                    }
                }
            } finally{
                try {
                    statement.close();
                } catch (SQLException e) {
                    logExcpetion(e);
                }
            }
        } finally {
            try {
                closeConnection();
            } catch (SQLException e) {
                logExcpetion(e);
            }
        }

        return new ResultData(columnName, values.toArray(new Object[values.size()][]));
    }
    
    @Override
    public int getDatabaseVersion() {
        if(version == null){
            Connection connection = null ;
            try {
                connection = getConnection();
                version = connection.getMetaData().getDatabaseMajorVersion();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }finally{
                if(connection != null){
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        logExcpetion(e);
                    }
                }
            }
        }
        return version.intValue();
    }
    
    protected abstract void logExcpetion(Throwable t);

    
    
    @Override
    public String convertToTimestamp(String timestampString) {
        Date date;
        try {
            date = DATE_FORMATTER.parse(timestampString);
        } catch (ParseException e) {
            throw new RuntimeException("The date doesn't match ISO 8601/SQL standard."
                    + " The current time in correct format is \"" 
                    + DATE_FORMATTER.format(new Date()) + "\"", e);
        }
        byte type = getDatabaseType();
        switch (type) {
        case DB_TYPE_POSTGRESQL:
            //postgre can just take timezone even it does nothing if the column doesn't support tz.
            return "'" + timestampString + "'";
        case DB_TYPE_ORACLE:
            return "TO_TIMESTAMP_TZ('" + timestampString + "', 'YYYY-MM-DD HH24:MI:SSTZHTZM')";
        case DB_TYPE_MS_SQL:
            return "'" + MSSQL_DATE_FORMATTER.format(date) + "'";
        default:
            throw new UnsupportedOperationException("Unknown database type: " + type);
        }
    }
}