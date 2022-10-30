/*
 * Created on Sep 22, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.concurrent.Executor;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/test/com/bluejungle/destiny/server/shared/repository/ConnectionTrackingConnectionPoolWrapperTest.java#1 $
 */

public class ConnectionTrackingConnectionPoolWrapperTest extends TestCase {

    private static final Long STALE_CONNECTION_TIMEOUT = new Long(10000); // 10 seconds

    private static final String EXPECTED_LOG_MESSAGE_WITHOUT_STACK = "Connection leak detected.\nTo enable output of the connection acquisition stack trace, set the connection pool property, \"com.bluejungle.destiny.connection.tracker.includestack\", to \"true\".";    
    private static final String EXPECTED_LOG_MESSAGE_WITH_STACK = "Connection leak detected." + 
    "\nConnection acquisition stack trace:" + 
    "\n\tat com.bluejungle.destiny.server.shared.repository.ConnectionTrackingConnectionPoolWrapper.getConnection(ConnectionTrackingConnectionPoolWrapper.java:94)" + 
    "\n\tat com.bluejungle.destiny.server.shared.repository.ConnectionTrackingConnectionPoolWrapperTest.testGetReleaseConnection(ConnectionTrackingConnectionPoolWrapperTest.java:";
    
    private ConnectionTrackingConnectionPoolWrapper poolToTest;
    private DummyConnectionPool wrappedPool;
    
    /**
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        System.setProperty(LogFactory.FACTORY_PROPERTY, TestLogFactory.class.getName());
        LogFactory.releaseAll();
        
        this.wrappedPool = new DummyConnectionPool();
        this.poolToTest = new ConnectionTrackingConnectionPoolWrapper(this.wrappedPool);
        
        Properties configProperties = new Properties();
        configProperties.setProperty(ConnectionTrackingConnectionPoolWrapper.CONNECTION_TRACKER_STALE_CONNECTION_TIME_PROPERTY_NAME, STALE_CONNECTION_TIMEOUT.toString());
        this.poolToTest.initialize(new TestConnectionPoolConfiguration(configProperties));
    }

    /**
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        this.poolToTest.close();
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.server.shared.repository.ConnectionTrackingConnectionPoolWrapper#ConnectionTrackingConnectionPoolWrapper(com.bluejungle.destiny.server.shared.repository.c3p0impl.C3P0ConnectionPoolWrapper)}.
     */
    public void testConnectionTrackingConnectionPoolWrapper() {
        try {
            new ConnectionTrackingConnectionPoolWrapper(null);
            fail("Should throw NPE for null argument to constructor");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.server.shared.repository.ConnectionTrackingConnectionPoolWrapper#close()}.
     * @throws SQLException 
     */
    public void testClose() throws SQLException {
        this.poolToTest.close();
        assertTrue("testClose - Ensure close was called on wrapped pool", this.wrappedPool.wasClosedCalled());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.server.shared.repository.ConnectionTrackingConnectionPoolWrapper#getConnection()}.
     * @throws SQLException 
     * @throws InterruptedException 
     * @throws ConnectionPoolInitializationFailedException 
     * @throws ConnectionPoolConfigurationException 
     */
    public void testGetReleaseConnection() throws SQLException, InterruptedException, ConnectionPoolConfigurationException, ConnectionPoolInitializationFailedException {
        TestLog poolLog = (TestLog) LogFactory.getLog(ConnectionTrackingConnectionPoolWrapper.class.getName());
        
        Connection connection = this.poolToTest.getConnection();
        assertNotNull("testGetReleaseConnection - Ensure initial connection retrieved is not null", connection);
        this.poolToTest.releaseConnection(connection);
        if (!poolLog.isEmpty()) {
            StringWriter errorMessageWriter = new StringWriter();            
            PrintWriter errorMessagePrintWriter = new PrintWriter(errorMessageWriter);
            errorMessagePrintWriter.println("testGetReleaseConnection - Failure.  Log found for timely release of connection:\n");
            while (!poolLog.isEmpty()) {
                TestLogMessage logMessage = poolLog.popMessage();
                errorMessagePrintWriter.println(logMessage.getMessage().toString());
                Throwable logMessageThrowable  = logMessage.getThrowable();
                if (logMessageThrowable != null) {
                    logMessageThrowable.printStackTrace(errorMessagePrintWriter);
                }
            }
            fail(errorMessageWriter.toString());
        }        
        
        // Try with timout
        connection = this.poolToTest.getConnection();
        assertNotNull("testGetReleaseConnection - Ensure initial connection retrieved is not null in timeout test", connection);
        Thread.sleep(STALE_CONNECTION_TIMEOUT.longValue() * 2 + 1000);
        Thread.yield();  
        TestLogMessage loggedMessage = poolLog.popMessage();
        assertEquals("testGetReleaseConnection - ", EXPECTED_LOG_MESSAGE_WITHOUT_STACK, loggedMessage.getMessage());
        
        // Now try with the stack
        this.poolToTest.close();
        Properties configProperties = new Properties();
        configProperties.setProperty(ConnectionTrackingConnectionPoolWrapper.CONNECTION_TRACKER_STALE_CONNECTION_TIME_PROPERTY_NAME, STALE_CONNECTION_TIMEOUT.toString());
        configProperties.setProperty(ConnectionTrackingConnectionPoolWrapper.CONNECTION_TRACKER_OUTPUT_STACK_TRACE_PROPERTY_NAME, "true");
        this.poolToTest = new ConnectionTrackingConnectionPoolWrapper(this.wrappedPool);
        this.poolToTest.initialize(new TestConnectionPoolConfiguration(configProperties));
   
        connection = this.poolToTest.getConnection();
        assertNotNull("testGetReleaseConnection - Ensure initial connection retrieved is not null in timeout test", connection);
        Thread.sleep(STALE_CONNECTION_TIMEOUT.longValue() * 2 + 1000);
        Thread.yield(); 
        loggedMessage = poolLog.popMessage();
        assertTrue("testGetReleaseConnection - Ensure log message with stack as expected", loggedMessage.getMessage().toString().startsWith(EXPECTED_LOG_MESSAGE_WITH_STACK));
    }

    private class DummyConnectionPool implements IConnectionPool {

        boolean closedCalled;
        boolean getConnectionCalled;
        boolean releaseConnectionCalled;
        boolean initializeCalled;

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#close()
         */
        public void close() throws SQLException {
            this.closedCalled = true;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#getName()
         */
        public String getName() {
            return "DummyConnectionPool";
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#initialize(com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration)
         */
        public void initialize(IConnectionPoolConfiguration configuration) throws ConnectionPoolConfigurationException, ConnectionPoolInitializationFailedException {
            this.initializeCalled = true;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#releaseConnection(java.sql.Connection)
         */
        public void releaseConnection(Connection c) throws SQLException {
            this.releaseConnectionCalled = true;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#getConnection()
         */
        public Connection getConnection() throws SQLException {
            this.getConnectionCalled = true;
            return new Connection() {

                /**
                 * @see java.sql.Connection#clearWarnings()
                 */
                public void clearWarnings() throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#close()
                 */
                public void close() throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#commit()
                 */
                public void commit() throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#createStatement()
                 */
                public Statement createStatement() throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#createStatement(int, int, int)
                 */
                public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#createStatement(int, int)
                 */
                public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#getAutoCommit()
                 */
                public boolean getAutoCommit() throws SQLException {
                    // TODO Auto-generated method stub
                    return false;
                }

                /**
                 * @see java.sql.Connection#getCatalog()
                 */
                public String getCatalog() throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#getHoldability()
                 */
                public int getHoldability() throws SQLException {
                    // TODO Auto-generated method stub
                    return 0;
                }

                /**
                 * @see java.sql.Connection#getMetaData()
                 */
                public DatabaseMetaData getMetaData() throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#getTransactionIsolation()
                 */
                public int getTransactionIsolation() throws SQLException {
                    // TODO Auto-generated method stub
                    return 0;
                }

                /**
                 * @see java.sql.Connection#getTypeMap()
                 */
                public Map getTypeMap() throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#getWarnings()
                 */
                public SQLWarning getWarnings() throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#isClosed()
                 */
                public boolean isClosed() throws SQLException {
                    // TODO Auto-generated method stub
                    return false;
                }

                /**
                 * @see java.sql.Connection#isReadOnly()
                 */
                public boolean isReadOnly() throws SQLException {
                    // TODO Auto-generated method stub
                    return false;
                }

                /**
                 * @see java.sql.Connection#nativeSQL(java.lang.String)
                 */
                public String nativeSQL(String sql) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
                 */
                public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
                 */
                public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareCall(java.lang.String)
                 */
                public CallableStatement prepareCall(String sql) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
                 */
                public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
                 */
                public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
                 */
                public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
                 */
                public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
                 */
                public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#prepareStatement(java.lang.String)
                 */
                public PreparedStatement prepareStatement(String sql) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
                 */
                public void releaseSavepoint(Savepoint savepoint) throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#rollback()
                 */
                public void rollback() throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#rollback(java.sql.Savepoint)
                 */
                public void rollback(Savepoint savepoint) throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#setAutoCommit(boolean)
                 */
                public void setAutoCommit(boolean autoCommit) throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#setCatalog(java.lang.String)
                 */
                public void setCatalog(String catalog) throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#setHoldability(int)
                 */
                public void setHoldability(int holdability) throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#setReadOnly(boolean)
                 */
                public void setReadOnly(boolean readOnly) throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#setSavepoint()
                 */
                public Savepoint setSavepoint() throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#setSavepoint(java.lang.String)
                 */
                public Savepoint setSavepoint(String name) throws SQLException {
                    // TODO Auto-generated method stub
                    return null;
                }

                /**
                 * @see java.sql.Connection#setTransactionIsolation(int)
                 */
                public void setTransactionIsolation(int level) throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                /**
                 * @see java.sql.Connection#setTypeMap(java.util.Map)
                 */
                public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
                    // TODO Auto-generated method stub
                    
                }

                @Override
                public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
                    return null;
                }

                @Override
                public Blob createBlob() throws SQLException {
                    return null;
                }

                @Override
                public Clob createClob() throws SQLException {
                    return null;
                }

                @Override
                public NClob createNClob() throws SQLException {
                    return null;
                }

                @Override
                public SQLXML createSQLXML() throws SQLException {
                    return null;
                }

                @Override
                public Struct createStruct(String typeName, Object[] attributes)
                        throws SQLException {
                    return null;
                }

                @Override
                public Properties getClientInfo() throws SQLException {
                    return null;
                }

                @Override
                public String getClientInfo(String name) throws SQLException {
                    return null;
                }

                @Override
                public boolean isValid(int timeout) throws SQLException {
                    return false;
                }

                @Override
                public void setClientInfo(Properties properties) throws SQLClientInfoException {
                }

                @Override
                public void setClientInfo(String name, String value) throws SQLClientInfoException {
                }

                @Override
                public boolean isWrapperFor(Class<?> iface) throws SQLException {
                    return false;
                }

                @Override
                public <T> T unwrap(Class<T> iface) throws SQLException {
                    return null;
                }

                // @Override
                public int getNetworkTimeout() throws SQLException {
                    return 0;
                }
                /**
                 * @see java.sql.Connection#setNetworkTimeout(Executor, int)
                 */
                // @Override
                public void setNetworkTimeout(Executor e, int timeout) throws SQLException {
                }
                
                /**
                 * @see java.sql.Connection#abort(Executor)
                 */
                // @Override
                public void abort(Executor e) throws SQLException {
                }
                
                // @Override
                public String getSchema() throws SQLException {
                    return null;
                }
                
                // @Override
                public void setSchema(String schema) throws SQLException {
                }
            };
        }

        /**
         * Retrieve the closedCalled.
         * 
         * @return the closedCalled.
         */
        private boolean wasClosedCalled() {
            return this.closedCalled;
        }

        /**
         * Reset closeCalled flag
         */
        private void resetClosedCalled() {
            this.closedCalled = false;
        }

        /**
         * Retrieve the getConnectionCalled.
         * 
         * @return the getConnectionCalled.
         */
        private boolean wasGetConnectionCalled() {
            return this.getConnectionCalled;
        }

        /**
         * Reset getConnectionCalled flag
         */
        private void resetGetConnectionCalled() {
            this.closedCalled = false;
        }

        /**
         * Retrieve the initializeCalled.
         * 
         * @return the initializeCalled.
         */
        private boolean wasInitializeCalled() {
            return this.initializeCalled;
        }

        /**
         * Reset initializedCalled flag
         */
        private void resetInitializedCalled() {
            this.closedCalled = false;
        }

        /**
         * Retrieve the releaseConnectionCalled.
         * 
         * @return the releaseConnectionCalled.
         */
        private boolean wasReleaseConnectionCalled() {
            return this.releaseConnectionCalled;
        }

        /**
         * Reset releaseConnectionCalled flag
         */
        private void releaseConnectionCalled() {
            this.closedCalled = false;
        }

        @Override
        public boolean isClosed() {
            // TODO Auto-generated method stub
            return false;
        }
    }
    
    public static class TestLogFactory extends LogFactory {
        private Map logsPerClassName = new HashMap();
        
        /**
         * @see org.apache.commons.logging.LogFactory#getAttribute(java.lang.String)
         */
        public Object getAttribute(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.apache.commons.logging.LogFactory#getAttributeNames()
         */
        public String[] getAttributeNames() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.apache.commons.logging.LogFactory#getInstance(java.lang.Class)
         */
        public Log getInstance(Class clazz) throws LogConfigurationException {
            return getInstance(clazz.getName());
        }

        /**
         * @see org.apache.commons.logging.LogFactory#getInstance(java.lang.String)
         */
        public Log getInstance(String name) throws LogConfigurationException {
            Log logToReturn = null; 
            synchronized (this.logsPerClassName) {
                logToReturn = (Log) this.logsPerClassName.get(name);
                if (logToReturn == null) {
                    logToReturn = new TestLog();
                    this.logsPerClassName.put(name, logToReturn);
                }
            } 
            
            return logToReturn;
        }

        /**
         * @see org.apache.commons.logging.LogFactory#release()
         */
        public void release() {
            // TODO Auto-generated method stub
            
        }

        /**
         * @see org.apache.commons.logging.LogFactory#removeAttribute(java.lang.String)
         */
        public void removeAttribute(String name) {
            // TODO Auto-generated method stub
            
        }

        /**
         * @see org.apache.commons.logging.LogFactory#setAttribute(java.lang.String, java.lang.Object)
         */
        public void setAttribute(String name, Object value) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    private static class TestLog implements Log {
        private final List messages = new LinkedList();

        /**
         * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
         */
        public void debug(Object message, Throwable t) {
            this.messages.add(new TestLogMessage(message, t, Level.FINE));
        }

        /**
         * @see org.apache.commons.logging.Log#debug(java.lang.Object)
         */
        public void debug(Object message) {
            this.debug(message, null);
        }

        /**
         * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
         */
        public void error(Object message, Throwable t) {
            this.messages.add(new TestLogMessage(message, t, Level.SEVERE));            
        }

        /**
         * @see org.apache.commons.logging.Log#error(java.lang.Object)
         */
        public void error(Object message) {
            this.error(message, null);
        }

        /**
         * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
         */
        public void fatal(Object message, Throwable t) {
            this.messages.add(new TestLogMessage(message, t, Level.SEVERE));  
        }

        /**
         * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
         */
        public void fatal(Object message) {
            this.fatal(message, null);
        }

        /**
         * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
         */
        public void info(Object message, Throwable t) {
            this.messages.add(new TestLogMessage(message, t, Level.INFO));  
        }

        /**
         * @see org.apache.commons.logging.Log#info(java.lang.Object)
         */
        public void info(Object message) {
            this.info(message, null);
        }

        /**
         * @see org.apache.commons.logging.Log#isDebugEnabled()
         */
        public boolean isDebugEnabled() {
            // TODO Auto-generated method stub
            return true;
        }

        /**
         * @see org.apache.commons.logging.Log#isErrorEnabled()
         */
        public boolean isErrorEnabled() {
            // TODO Auto-generated method stub
            return true;
        }

        /**
         * @see org.apache.commons.logging.Log#isFatalEnabled()
         */
        public boolean isFatalEnabled() {
            // TODO Auto-generated method stub
            return true;
        }

        /**
         * @see org.apache.commons.logging.Log#isInfoEnabled()
         */
        public boolean isInfoEnabled() {
            // TODO Auto-generated method stub
            return true;
        }

        /**
         * @see org.apache.commons.logging.Log#isTraceEnabled()
         */
        public boolean isTraceEnabled() {
            // TODO Auto-generated method stub
            return true;
        }

        /**
         * @see org.apache.commons.logging.Log#isWarnEnabled()
         */
        public boolean isWarnEnabled() {
            // TODO Auto-generated method stub
            return true;
        }

        /**
         * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
         */
        public void trace(Object message, Throwable t) {
            this.messages.add(new TestLogMessage(message, t, Level.FINEST));  
        }

        /**
         * @see org.apache.commons.logging.Log#trace(java.lang.Object)
         */
        public void trace(Object message) {
            this.trace(message, null);            
        }

        /**
         * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
         */
        public void warn(Object message, Throwable t) {
            this.messages.add(new TestLogMessage(message, t, Level.WARNING));  
        }

        /**
         * @see org.apache.commons.logging.Log#warn(java.lang.Object)
         */
        public void warn(Object message) {
            this.warn(message, null);            
        }      
        
        private TestLogMessage popMessage() {
            return (TestLogMessage) this.messages.remove(0);
        }
        
        private boolean isEmpty() {
            return this.messages.isEmpty();
        }
    }
    
    private static class TestLogMessage {        
        private Object message;
        private Throwable throwable;
        private Level logLevel;
        
        /**
         * Create an instance of TestLogMessage
         * @param message
         * @param throwable
         */
        public TestLogMessage(Object message, Throwable throwable, Level logLevel) {
            super();
            this.message = message;
            this.throwable = throwable;
            this.logLevel = logLevel;
        }

        /**
         * Retrieve the message.
         * @return the message.
         */
        private Object getMessage() {
            return this.message;
        }
        
        /**
         * Retrieve the throwable.
         * @return the throwable.
         */
        private Throwable getThrowable() {
            return this.throwable;
        }
        
        /**
         * Retrieve the logLevel.
         * @return the logLevel.
         */
        private Level getLogLevel() {
            return this.logLevel;
        }                
    }
    
    private class TestConnectionPoolConfiguration implements IConnectionPoolConfiguration {
        private Properties configProperties;
        
        
        /**
         * Create an instance of TestConnectionPoolConfiguration
         * @param configProperties
         */
        public TestConnectionPoolConfiguration(Properties configProperties) {
            super();
            this.configProperties = configProperties;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getDriverClassName()
         */
        public String getDriverClassName() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getJDBCConnectString()
         */
        public String getJDBCConnectString() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getMaxPoolSize()
         */
        public int getMaxPoolSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getName()
         */
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getPassword()
         */
        public String getPassword() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getProperties()
         */
        public Properties getProperties() {
            return this.configProperties;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getUserName()
         */
        public String getUserName() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
