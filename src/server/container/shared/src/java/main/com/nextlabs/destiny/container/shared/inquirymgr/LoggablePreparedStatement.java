/*
 *  All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.sql.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.utils.StringUtils;

/**
 * A wrapper class for prepared statments to allow the query content and the
 * run time to be logged.
 * 
 */
public class LoggablePreparedStatement implements PreparedStatement {

    protected Log log = LogFactory.getLog(LoggablePreparedStatement.class.getName());

    PreparedStatement stmt;
    String sql;
    String[] parameters;

    public LoggablePreparedStatement(Connection con, String sql) throws SQLException   {
        this.stmt = con.prepareStatement(sql);
        this.sql = sql;
        if (log.isDebugEnabled()) {
            parameters = new String[StringUtils.count(sql, '?')];
        }
    }

    public String toString() {
        StringBuffer buff = new StringBuffer(sql);
        if (log.isDebugEnabled()) {
            for (int i=0; i<parameters.length; i++) {
                buff.append("\nparameter[" + i + "] = ").append(parameters[i]);
            }
        }
        return buff.toString();
    }

    private void setParameter(int index, Object parameterValue) {
        if (log.isDebugEnabled()) {
            parameters[index - 1] = String.valueOf(parameterValue);
        }
    }
    
    /*  
     * Implement PreparedStatement
     */
    @Override
    public ResultSet executeQuery() throws SQLException {
        long startTime = 0;
        ResultSet result = null;
        if (log.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            log.debug("Starting to run query: " + toString());
        }
        result = stmt.executeQuery();
        if (log.isDebugEnabled()) {
            log.debug("Time taken to complete query in seconds: " + 
                    (System.currentTimeMillis() - startTime) / 1000);
        }
        return result;
    }
    
    @Override
    public boolean execute() throws SQLException {
        boolean status = false;
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            log.debug("Starting to run query: " + toString());
        }
        status  = stmt.execute();
        if (log.isDebugEnabled()) {
            log.debug("Time taken to complete query in seconds: " + 
                    (System.currentTimeMillis() - startTime) / 1000);
        }
        return status;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return stmt.executeUpdate();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setParameter(parameterIndex, null);
        stmt.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setByte(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setShort(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws 
    SQLException {
        setParameter(parameterIndex, x);
        stmt.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, java.sql.Date x) throws 
    SQLException {
        setParameter(parameterIndex, x);
        stmt.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws 
    SQLException {
        setParameter(parameterIndex, x);
        stmt.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) 
    throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int 
            length) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int 
            length) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        parameters = new String[parameters.length];
        stmt.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, 
            int scale) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setObject(parameterIndex, x, targetSqlType, scale);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) 
    throws SQLException {
        setParameter(parameterIndex, "" + x);
        stmt.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setObject(parameterIndex, x);
    }

    @Override
    public void addBatch() throws SQLException {
        stmt.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int 
            length) throws SQLException {
        parameters[parameterIndex-1] = reader.toString();
        stmt.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int i, Ref x) throws SQLException {
        parameters[i-1] = String.valueOf(x);
        stmt.setRef(i, x);
    }

    @Override
    public void setBlob(int i, Blob x) throws SQLException {
        parameters[i-1] = String.valueOf(x);
        stmt.setBlob(i, x);
    }

    @Override
    public void setClob(int i, Clob x) throws SQLException {
        parameters[i-1] = String.valueOf(x);
        stmt.setClob(i, x);
    }

    @Override
    public void setArray(int i, Array x) throws SQLException {
        parameters[i-1] = String.valueOf(x);
        stmt.setArray(i, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return stmt.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws 
    SQLException {
        setParameter(parameterIndex, x);
        stmt.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws 
    SQLException {
        setParameter(parameterIndex, x);
        stmt.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) 
    throws SQLException {
        setParameter(parameterIndex, x);
        stmt.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(int paramIndex, int sqlType, String typeName) throws 
    SQLException {
        parameters[paramIndex-1] = null;
        stmt.setNull(paramIndex, sqlType, typeName);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return stmt.executeQuery(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return stmt.executeUpdate(sql);
    }

    @Override
    public void close() throws SQLException {
        stmt.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return stmt.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        stmt.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return stmt.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        stmt.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        stmt.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return stmt.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        stmt.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        stmt.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return stmt.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        stmt.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        stmt.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return stmt.execute(sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return stmt.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return stmt.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return stmt.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        stmt.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return stmt.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        stmt.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return stmt.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return stmt.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return stmt.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        stmt.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        stmt.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return stmt.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return stmt.getConnection();
    }

    @Override
    public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
        stmt.setURL(parameterIndex, x);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return stmt.getGeneratedKeys();
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) 
    throws SQLException {
        return stmt.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int columnIndexes[]) 
    throws SQLException {
        return stmt.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String columnNames[]) 
    throws SQLException {
        return stmt.execute(sql, columnNames);
    }

    @Override
    public  int getResultSetHoldability() throws SQLException {
        return stmt.getResultSetHoldability();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return stmt.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int columnIndexes[]) throws SQLException {
        return stmt.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String columnNames[]) throws SQLException {
        return stmt.executeUpdate(sql, columnNames);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return stmt.getParameterMetaData();
    }

    @Override
    public  boolean getMoreResults(int i) throws SQLException {
        return stmt.getMoreResults(i);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return stmt.isClosed();
    }

    // @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return stmt.isPoolable();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return stmt.isWrapperFor(iface);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        stmt.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        stmt.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        stmt.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        stmt.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {
        stmt.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        stmt.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length)
            throws SQLException {
        stmt.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        stmt.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        stmt.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        stmt.setClob(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length)
            throws SQLException {
        stmt.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        stmt.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        stmt.setNClob(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        stmt.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        stmt.setNClob(parameterIndex, reader);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        stmt.setNString(parameterIndex, value);
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        stmt.setPoolable(poolable);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        stmt.setRowId(parameterIndex, x);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        stmt.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return stmt.unwrap(iface);
    }

    /**
     * @see java.sql.Statement#closeOnCompletion()
     */
    // @Override
    public void closeOnCompletion() throws SQLException {
        // stmt.closeOnCompletion();
    }
}
