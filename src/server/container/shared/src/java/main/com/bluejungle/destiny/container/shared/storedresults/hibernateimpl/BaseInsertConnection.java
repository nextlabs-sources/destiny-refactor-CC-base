/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;

/**
 * This is the base insert connection implementation class. The base insert
 * connection class transforms a select statement into an "insert select"
 * statement. The list of columns to query on are provided by the child classes.
 * 
 * Here are an example of query by id
 *
 * POSTGRES:
 * insert into STORED_QUERY_BY_ID_RESULTS (id, stored_query_id, resultId)
 * (select nextval ('stored_results_sequence'), '16', x0_0_ from (select
 * policyacti0_.id as x0_0_ from POLICY_ACTIVITY_LOG policyacti0_ order by
 * policyacti0_.time DESC) as foo)
 *
 * ORACLE:
 * insert into STORED_QUERY_BY_ID_RESULTS (id, stored_query_id, resultId)
 * (select stored_results_sequence.nextval, '16', x0_0_ from (select
 * policyacti0_.id as x0_0_ from POLICY_ACTIVITY_LOG policyacti0_ order by
 * policyacti0_.time DESC))
 * 
 * Here are an example of query by summary
 * 
 * POSTGRES:
 * insert into STORED_QUERY_SUMMARY_RESULTS (id, stored_query_id, value, count)
 * (select nextval ('stored_results_sequence'), '23', x0_0_, x1_0_ from (select
 * policydo0_.name as x0_0_, count(policydo0_.name) as x1_0_ from CACHED_POLICY
 * policydo0_, POLICY_ACTIVITY_LOG policyacti1_ where
 * (policyacti1_.policy_id=policydo0_.id ) group by policydo0_.name order by
 * count(policydo0_.name)DESC) as foo)
 *
 * ORACLE:
 * insert into STORED_QUERY_SUMMARY_RESULTS (id, stored_query_id, value, count)
 * (select * from (select  stored_results_sequence.nextval, '23', x0_0_, x1_0_ from (select
 * policydo0_.name as x0_0_, count(policydo0_.name) as x1_0_ from CACHED_POLICY
 * policydo0_, POLICY_ACTIVITY_LOG policyacti1_ where
 * (policyacti1_.policy_id=policydo0_.id ) group by policydo0_.name order by
 * count(policydo0_.name)DESC)) where rowcount < ?)
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/BaseInsertConnection.java#1 $
 */

abstract class BaseInsertConnection implements Connection, IInsertConnection {

    private static final String OPEN = "(";
    private static final String CLOSE = ")";
    private static final String COMMA = ",";
    private static final String FROM = "from";
    private static final String AS_KEYWORD = "as";
    private static final String CHAR = "char";
    private static final String TRIM = "trim";
    private static final String VALUE_COLUMN = "value";
    private static final String INSERT_INTO_KEYWORD = "insert into";
    private static final String SELECT_KEYWORD = "select";
    private static final String SPACE = " ";
    private static final String ROW_DESIGNATOR = "row_";
    private Connection realConnection;
    private String insertSQL;
    private String resultTableName;
    private Long queryId;
    private String sequenceExpression;
    private PreparedStatement preparedStatement;
    private boolean needQueryAlias = false;
    private boolean needTopSelect = false;
    private boolean needsCast = false;
    private boolean skipRowNumber = false;

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
    public BaseInsertConnection(Connection realConnection, String resultTableName, Long storedQueryId, String sequenceExpression) {
        this.realConnection = realConnection;
        this.resultTableName = resultTableName;
        this.queryId = storedQueryId;
        if ( sequenceExpression != null ) {
            String lowerSeq = sequenceExpression.toLowerCase();
            int selectIndex = lowerSeq.indexOf( SELECT_KEYWORD );
            int fromIndex = lowerSeq.indexOf( FROM );
            if ( selectIndex != -1 && fromIndex != -1 && selectIndex < fromIndex ) {
                sequenceExpression = sequenceExpression.substring( selectIndex + SELECT_KEYWORD.length(), fromIndex );
            } else if ( selectIndex != -1 ) {
                sequenceExpression = sequenceExpression.substring( selectIndex + SELECT_KEYWORD.length() );
            }
            this.sequenceExpression = sequenceExpression;
        }
        if (this.realConnection == null) {
            throw new NullPointerException("Real connection cannot be null.");
        }
        if (this.resultTableName == null) {
            throw new NullPointerException("The result table name cannot be null.");
        }
        if (this.queryId == null) {
            throw new NullPointerException("The stored query Id cannot be null.");
        }
        try {
            DatabaseMetaData metadata = realConnection.getMetaData();
            needQueryAlias = DatabaseMetadataUtilities.isPostgres( metadata )
                          || DatabaseMetadataUtilities.isSqlServer( metadata );
            needTopSelect = DatabaseMetadataUtilities.isSqlServer( metadata );
            needsCast = DatabaseMetadataUtilities.isDb2(metadata);
            skipRowNumber = DatabaseMetadataUtilities.isDb2(metadata);
        } catch ( SQLException ignored ) {
        }
    }

    /**
     * @see java.sql.Connection#getHoldability()
     */
    @Override
    public int getHoldability() throws SQLException {
        return this.realConnection.getHoldability();
    }

    /**
     * @see java.sql.Connection#getTransactionIsolation()
     */
    @Override
    public int getTransactionIsolation() throws SQLException {
        return this.realConnection.getTransactionIsolation();
    }

    /**
     * @see java.sql.Connection#clearWarnings()
     */
    @Override
    public void clearWarnings() throws SQLException {
        this.realConnection.clearWarnings();
    }

    /**
     * @see java.sql.Connection#close()
     */
    @Override
    public void close() throws SQLException {
        this.realConnection.close();
    }

    /**
     * @see java.sql.Connection#commit()
     */
    @Override
    public void commit() throws SQLException {
        this.realConnection.commit();
    }

    /**
     * Returns the insertion SQL that should be executed to insert results in
     * the result table.
     *
     * Hack alert: This method looks for a field called 'value'
     * in the table into which we are inserting data.
     * If we are running against a DB2 database, the code will
     * add a trim and a cast for all data being inserted
     * into that column. 
     *
     * @param selectSQL
     *            original SQL generated by the caller
     * @return modified SQL query redirecting the query output to the result
     *         table.
     */
    protected String createInsertionSQL( String selectSQL ) {
        String result = INSERT_INTO_KEYWORD + SPACE + getResultTableName() + SPACE + OPEN;
        boolean isFirst = true;
        int valueIndex = -1;
        int i = -1;
        for (String columnName : getResultTableColumnNames()) {
            i++;
            if (columnName.equalsIgnoreCase(VALUE_COLUMN)) {
                valueIndex = i;
            }
            if (!isFirst) {
                result += COMMA + SPACE;
            } else {
                isFirst = false;
            }
            result += columnName;
        }
        result += CLOSE + SPACE + OPEN + SELECT_KEYWORD + SPACE;
        
        if (getSequenceExpression() != null) {
            result += getSequenceExpression() + COMMA + SPACE;
        }
        result += getQueryId();
        StringTokenizer st = new StringTokenizer( selectSQL );
        boolean seenSelect = false;
        boolean seenAs = false;
        boolean seenArguments = false;
        // Parse the select list of the original query,
        // and add comma-separated aliases to the select list
        // of the outer query.
        i = 1;
        while ( st.hasMoreTokens() ) {
            String tok = st.nextToken();
            if ( SELECT_KEYWORD.equalsIgnoreCase( tok ) ) {
                seenSelect = true;
            } else if ( seenSelect && FROM.equalsIgnoreCase( tok ) ) {
                if ( seenArguments ) {
                    break;
                }
            } else if ( seenSelect && AS_KEYWORD.equalsIgnoreCase( tok ) ) {
                seenAs = true;
            } else if ( seenAs ) {
                seenAs = false;
                tok = tok.replace( ',', ' ' ).trim();
                if (skipRowNumber && ROW_DESIGNATOR.equalsIgnoreCase(tok)) {
                    i++;
                    continue;
                }
                result += COMMA + SPACE;
                if (needsCast && i++ == valueIndex) {
                    result += TRIM + OPEN + CHAR + OPEN + tok + CLOSE + CLOSE;
                } else {
                    result += tok;
                }
                seenArguments = true;
            }
        }
        if (needTopSelect && selectSQL.indexOf("select top") == -1) {
            selectSQL = selectSQL.replaceAll("^select", "select top 2000000000");
        }
        result += SPACE + FROM + OPEN + selectSQL + CLOSE;
        if ( needQueryAlias ) {
            result += " OriginalQuery";
        }
        result += CLOSE;
        return result;
    }

    /**
     * Returns the an ordered list of column names from the result tables. The
     * column names should be returned in the same order than the SQL query.
     * 
     * @return an ordered list of the result table column name for insertion.
     */
    protected abstract List<String> getResultTableColumnNames();

    /**
     * @see java.sql.Connection#rollback()
     */
    @Override
    public void rollback() throws SQLException {
        this.realConnection.rollback();
    }

    /**
     * @see java.sql.Connection#getAutoCommit()
     */
    @Override
    public boolean getAutoCommit() throws SQLException {
        return this.realConnection.getAutoCommit();
    }

    /**
     * @see java.sql.Connection#isClosed()
     */
    @Override
    public boolean isClosed() throws SQLException {
        return this.realConnection.isClosed();
    }

    /**
     * @see java.sql.Connection#isReadOnly()
     */
    @Override
    public boolean isReadOnly() throws SQLException {
        return this.realConnection.isReadOnly();
    }

    /**
     * Returns whether the current database is sequence enabled
     * 
     * @return whether the current database is sequence enabled
     */
    protected boolean isSequenceEnabled() {
        return getSequenceExpression() != null;
    }

    /**
     * @see java.sql.Connection#setHoldability(int)
     */
    @Override
    public void setHoldability(int holdability) throws SQLException {
        this.realConnection.setHoldability(holdability);
    }

    /**
     * @see java.sql.Connection#setTransactionIsolation(int)
     */
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        this.realConnection.setTransactionIsolation(level);
    }

    /**
     * @see java.sql.Connection#setAutoCommit(boolean)
     */
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.realConnection.setAutoCommit(autoCommit);
    }

    /**
     * @see java.sql.Connection#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        this.realConnection.setReadOnly(readOnly);
    }

    /**
     * @see java.sql.Connection#getCatalog()
     */
    @Override
    public String getCatalog() throws SQLException {
        return this.realConnection.getCatalog();
    }

    /**
     * @see java.sql.Connection#setCatalog(java.lang.String)
     */
    @Override
    public void setCatalog(String catalog) throws SQLException {
        this.realConnection.setCatalog(catalog);
    }

    /**
     * @see java.sql.Connection#getMetaData()
     */
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.realConnection.getMetaData();
    }

    /**
     * Returns the stored query id
     * 
     * @return the query id
     */
    protected Long getQueryId() {
        return this.queryId;
    }

    /**
     * Returns the name of the result table where to insert result rows
     * 
     * @return the name of the result table where to insert result rows
     */
    protected String getResultTableName() {
        return this.resultTableName;
    }

    /**
     * Returns the DB specific expression to get the sequence number
     * 
     * @return the DB specific expression to get the sequence number
     */
    protected String getSequenceExpression() {
        return this.sequenceExpression;
    }

    /**
     * @see java.sql.Connection#getWarnings()
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.realConnection.getWarnings();
    }

    /**
     * @see java.sql.Connection#setSavepoint()
     */
    @Override
    public Savepoint setSavepoint() throws SQLException {
        return this.realConnection.setSavepoint();
    }

    /**
     * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
     */
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        this.realConnection.releaseSavepoint(savepoint);
    }

    /**
     * @see java.sql.Connection#rollback(java.sql.Savepoint)
     */
    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        this.realConnection.rollback();
    }

    /**
     * @see java.sql.Connection#createStatement()
     */
    @Override
    public Statement createStatement() throws SQLException {
        return this.realConnection.createStatement();
    }

    /**
     * @see java.sql.Connection#createStatement(int, int)
     */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.realConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    /**
     * @see java.sql.Connection#createStatement(int, int, int)
     */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return this.realConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * @see java.sql.Connection#getTypeMap()
     */
    @Override
    public Map<String,Class<?>> getTypeMap() throws SQLException {
        return this.realConnection.getTypeMap();
    }

    /**
     * @see java.sql.Connection#setTypeMap(java.util.Map)
     */
    @Override
    public void setTypeMap(Map<String,Class<?>> map) throws SQLException {
        this.realConnection.setTypeMap(map);
    }

    /**
     * @see java.sql.Connection#nativeSQL(java.lang.String)
     */
    @Override
    public String nativeSQL(String sql) throws SQLException {
        return this.realConnection.nativeSQL(sql);
    }

    /**
     * @see java.sql.Connection#prepareCall(java.lang.String)
     */
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return this.realConnection.prepareCall(sql);
    }

    /**
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
     */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.realConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
     */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return this.realConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String)
     */
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        this.insertSQL = createInsertionSQL(sql);
        this.preparedStatement = new InsertPreparedStatement();
        return this.preparedStatement;
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int)
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return this.prepareStatement(sql);
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.prepareStatement(sql);
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int,
     *      int)
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return this.prepareStatement(sql);
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return this.prepareStatement(sql);
    }

    /**
     * @see java.sql.Connection#setSavepoint(java.lang.String)
     */
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return this.realConnection.setSavepoint(name);
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String,
     *      java.lang.String[])
     */
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return this.prepareStatement(sql);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IInsertConnection#getSQLQuery()
     */
    public String getSQLQuery() {
        return this.insertSQL;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IInsertConnection#getPreparedStatement()
     */
    public PreparedStatement getPreparedStatement() {
        return this.preparedStatement;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IInsertConnection#getPreparedStatement()
     */
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return realConnection.createArrayOf(typeName, elements);
    }

    
    /**
     * @see java.sql.Connection#createBlob()
     */
    @Override
    public Blob createBlob() throws SQLException {
        return realConnection.createBlob();
    }

    /**
     * @see java.sql.Connection#createClob()
     */
    @Override
    public Clob createClob() throws SQLException {
        return realConnection.createClob();
    }

    /**
     * @see java.sql.Connection#createNClob()
     */
    @Override
    public NClob createNClob() throws SQLException {
        return realConnection.createNClob();
    }

    /**
     * @see java.sql.Connection#createSQLXML()
     */
    @Override
    public SQLXML createSQLXML() throws SQLException {
        return realConnection.createSQLXML();
    }

    /**
     * @see java.sql.Connection#createStruct()
     */
    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return realConnection.createStruct(typeName, attributes);
    }

    /**
     * @see java.sql.Connection#getClientInfo()
     */
    @Override
    public Properties getClientInfo() throws SQLException {
        return realConnection.getClientInfo();
    }

    /**
     * @see java.sql.Connection#getClientInfo(String)
     */
    @Override
    public String getClientInfo(String name) throws SQLException {
        return realConnection.getClientInfo(name);
    }

    /**
     * @see java.sql.Connection#isValid(int)
     */
    @Override
    public boolean isValid(int timeout) throws SQLException {
        return realConnection.isValid(timeout);
    }

    /**
     * @see java.sql.Connection#isWrapperFor(Class<?>)
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return realConnection.isWrapperFor(iface);
    }

    /**
     * @see java.sql.Connection#setClientInfo(Properties)
     */
    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        realConnection.setClientInfo(properties);
    }

    /**
     * @see java.sql.Connection#setClientInfo(String, String)
     */
    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        realConnection.setClientInfo(name, value);
    }

    /**
     * @see java.sql.Connection#unwrap(Class<T>)
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return realConnection.unwrap(iface);
    }

    /**
     * @see java.sql.Connection#getNetworkTimeout()
     */
    // @Override
    public int getNetworkTimeout() throws SQLException {
        // return realConnection.getNetworkTimeout();
        return 0;
    }
    
    /**
     * @see java.sql.Connection#setNetworkTimeout(Executor, int)
     */
    // @Override
    public void setNetworkTimeout(Executor e, int timeout) throws SQLException {
        // realConnection.setNetworkTimeout(e, timeout);
    }
    
    /**
     * @see java.sql.Connection#abort(Executor)
     */
    // @Override
    public void abort(Executor e) throws SQLException {
        // realConnection.abort(e);
    }

    // @Override
    public String getSchema() throws SQLException {
        // return realConnection.getSchema();
        return null;
    }

    // @Override
    public void setSchema(String schema) throws SQLException {
        // realConnection.setSchema(schema);
    }
}
