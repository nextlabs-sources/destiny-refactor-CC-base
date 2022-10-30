/*
 * Created on Apr 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
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

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/SimplePreparedStatement.java#1 $
 */

class SimplePreparedStatement implements PreparedStatement {

    private String stringValue;

    /**
     * Constructor
     */
    public SimplePreparedStatement() {
        super();
    }

    /**
     * @see java.sql.PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.PreparedStatement#addBatch()
     */
    public void addBatch() throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#clearParameters()
     */
    public void clearParameters() throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        return false;
    }

    /**
     * Returns the last string value that was saved in the setString method
     * 
     * @return the last string value set in the setString method.
     */
    public String getStringValue() {
        return this.stringValue;
    }

    /**
     * @see java.sql.PreparedStatement#setByte(int, byte)
     */
    public void setByte(int parameterIndex, byte x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setDouble(int, double)
     */
    public void setDouble(int parameterIndex, double x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setFloat(int, float)
     */
    public void setFloat(int parameterIndex, float x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setInt(int, int)
     */
    public void setInt(int parameterIndex, int x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setNull(int, int)
     */
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setLong(int, long)
     */
    public void setLong(int parameterIndex, long x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setShort(int, short)
     */
    public void setShort(int parameterIndex, short x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setBoolean(int, boolean)
     */
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setBytes(int, byte[])
     */
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,
     *      int)
     */
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,
     *      int)
     */
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setUnicodeStream(int,
     *      java.io.InputStream, int)
     */
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,
     *      int)
     */
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
     */
    public void setObject(int parameterIndex, Object x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int,
     *      int)
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
     */
    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setString(int, java.lang.String)
     */
    public void setString(int parameterIndex, String x) throws SQLException {
        this.stringValue = x;
    }

    /**
     * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
     */
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
     */
    public void setURL(int parameterIndex, URL x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
     */
    public void setArray(int i, Array x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
     */
    public void setBlob(int i, Blob x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
     */
    public void setClob(int i, Clob x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
     */
    public void setDate(int parameterIndex, Date x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#getParameterMetaData()
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    /**
     * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
     */
    public void setRef(int i, Ref x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        return null;
    }

    /**
     * @see java.sql.PreparedStatement#getMetaData()
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    /**
     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
     */
    public void setTime(int parameterIndex, Time x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
     */
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date,
     *      java.util.Calendar)
     */
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time,
     *      java.util.Calendar)
     */
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    }

    /**
     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp,
     *      java.util.Calendar)
     */
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    }

    /**
     * @see java.sql.Statement#getFetchDirection()
     */
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#getFetchSize()
     */
    public int getFetchSize() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#getMaxFieldSize()
     */
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#getMaxRows()
     */
    public int getMaxRows() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#getQueryTimeout()
     */
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#getResultSetConcurrency()
     */
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#getResultSetHoldability()
     */
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#getResultSetType()
     */
    public int getResultSetType() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#getUpdateCount()
     */
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#cancel()
     */
    public void cancel() throws SQLException {
    }

    /**
     * @see java.sql.Statement#clearBatch()
     */
    public void clearBatch() throws SQLException {
    }

    /**
     * @see java.sql.Statement#clearWarnings()
     */
    public void clearWarnings() throws SQLException {
    }

    /**
     * @see java.sql.Statement#close()
     */
    public void close() throws SQLException {
    }

    /**
     * @see java.sql.Statement#getMoreResults()
     */
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    /**
     * @see java.sql.Statement#executeBatch()
     */
    public int[] executeBatch() throws SQLException {
        return null;
    }

    /**
     * @see java.sql.Statement#setFetchDirection(int)
     */
    public void setFetchDirection(int direction) throws SQLException {
    }

    /**
     * @see java.sql.Statement#setFetchSize(int)
     */
    public void setFetchSize(int rows) throws SQLException {
    }

    /**
     * @see java.sql.Statement#setMaxFieldSize(int)
     */
    public void setMaxFieldSize(int max) throws SQLException {
    }

    /**
     * @see java.sql.Statement#setMaxRows(int)
     */
    public void setMaxRows(int max) throws SQLException {
    }

    /**
     * @see java.sql.Statement#setQueryTimeout(int)
     */
    public void setQueryTimeout(int seconds) throws SQLException {
    }

    /**
     * @see java.sql.Statement#getMoreResults(int)
     */
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    /**
     * @see java.sql.Statement#setEscapeProcessing(boolean)
     */
    public void setEscapeProcessing(boolean enable) throws SQLException {
    }

    /**
     * @see java.sql.Statement#executeUpdate(java.lang.String)
     */
    public int executeUpdate(String sql) throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#addBatch(java.lang.String)
     */
    public void addBatch(String sql) throws SQLException {
    }

    /**
     * @see java.sql.Statement#setCursorName(java.lang.String)
     */
    public void setCursorName(String name) throws SQLException {
    }

    /**
     * @see java.sql.Statement#execute(java.lang.String)
     */
    public boolean execute(String sql) throws SQLException {
        return false;
    }

    /**
     * @see java.sql.Statement#executeUpdate(java.lang.String, int)
     */
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#execute(java.lang.String, int)
     */
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    /**
     * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
     */
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#execute(java.lang.String, int[])
     */
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    /**
     * @see java.sql.Statement#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return null;
    }

    /**
     * @see java.sql.Statement#getGeneratedKeys()
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    /**
     * @see java.sql.Statement#getResultSet()
     */
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    /**
     * @see java.sql.Statement#getWarnings()
     */
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    /**
     * @see java.sql.Statement#executeUpdate(java.lang.String,
     *      java.lang.String[])
     */
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    /**
     * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
     */
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    /**
     * @see java.sql.Statement#executeQuery(java.lang.String)
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        return null;
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length)
            throws SQLException {
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length)
            throws SQLException {
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    // True for Java 7
    // @Override 
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    /**
     * @see java.sql.Statement#closeOnCompletion()
     */
    // @Override
    public void closeOnCompletion() {
    }
}
