/*
 * Created on Aug 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository.c3p0impl;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.server.shared.repository.ConnectionPoolConfigurationException;
import com.bluejungle.destiny.server.shared.repository.ConnectionPoolInitializationFailedException;
import com.bluejungle.destiny.server.shared.repository.IConnectionPool;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/repository/c3p0impl/C3P0ConnectionPoolWrapper.java#1 $
 */

public class C3P0ConnectionPoolWrapper implements IConnectionPool {

	private static final double MIN_PERCENT_OF_THE_MAX = 10.0;
	/*
     * Default connection pool properties:
     */
    private static final int DEFAULT_ACQUIRE_INCREMENT = 3;
    private static final int DEFAULT_MAX_IDLE_TIME = 900;
    private static final int DEFAULT_IDLE_CONNECTION_TEST_PERIOD = 300;
    private static final int DEFAULT_MAX_STATEMENTS = 0;
    private static final int DEFAULT_CHECKOUT_TIMEOUT = 300000;
    private static final String DEFAULT_AUTOMATIC_TEST_TABLE = "connection_test_table";

    private static final Log LOG = LogFactory.getLog(C3P0ConnectionPoolWrapper.class.getName());

    private ComboPooledDataSource c3p0ConnectionPool = null;
    private String name;
    private boolean closed = true;

    /**
     * Constructor
     *  
     */
    public C3P0ConnectionPoolWrapper(String name) {
        this.name = name;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#initialize(com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration)
     */
    @Override
    public void initialize(IConnectionPoolConfiguration configuration) throws ConnectionPoolConfigurationException, ConnectionPoolInitializationFailedException {
        this.c3p0ConnectionPool = new ComboPooledDataSource();
        
        if (configuration.getJDBCConnectString() == null) {
            throw new NullPointerException("jdbc connect string is null");
        }
        if (configuration.getPassword() == null) {
            throw new NullPointerException("password is null");
        }
        if (configuration.getUserName() == null) {
            throw new NullPointerException("username is null");
        }
        if (configuration.getDriverClassName() == null) {
            throw new NullPointerException("driver class name is null");
        }

        try {
            this.c3p0ConnectionPool.setDriverClass(configuration.getDriverClassName());
        } catch (PropertyVetoException e) {
            throw new ConnectionPoolInitializationFailedException(e);
        }
        this.c3p0ConnectionPool.setJdbcUrl(configuration.getJDBCConnectString());
        this.c3p0ConnectionPool.setPassword(configuration.getPassword());
        String username = configuration.getUserName();
        this.c3p0ConnectionPool.setUser(username);
        this.c3p0ConnectionPool.setMaxPoolSize(configuration.getMaxPoolSize());
        // We set the worker threads to the MaxPoolsize so each pool entry can run on one.
        // We also add one additional thread for administrative tasks to run on.
        this.c3p0ConnectionPool.setNumHelperThreads(configuration.getMaxPoolSize() + 1);
        this.c3p0ConnectionPool.setDescription(configuration.getName());
        //round up
		int minPoolSize = (int) Math.ceil(configuration.getMaxPoolSize() / MIN_PERCENT_OF_THE_MAX);
		this.c3p0ConnectionPool.setMinPoolSize(minPoolSize);
        
        // Default properties:
        this.c3p0ConnectionPool.setAcquireIncrement(DEFAULT_ACQUIRE_INCREMENT);
        this.c3p0ConnectionPool.setMaxIdleTime(DEFAULT_MAX_IDLE_TIME);
        this.c3p0ConnectionPool.setIdleConnectionTestPeriod(DEFAULT_IDLE_CONNECTION_TEST_PERIOD);
        this.c3p0ConnectionPool.setMaxStatements(DEFAULT_MAX_STATEMENTS);
        this.c3p0ConnectionPool.setCheckoutTimeout(DEFAULT_CHECKOUT_TIMEOUT);
        String hashCode = Integer.toString(username.hashCode());
        String suffix = username.length() > 3 
                ? username.substring(0,3) 
                : username;
        String testTableSuffix = "_" +  suffix
                + (hashCode.length() > 5 
                        ? hashCode.substring(hashCode.length() - 5) 
                        : hashCode);
        
        this.c3p0ConnectionPool.setAutomaticTestTable(DEFAULT_AUTOMATIC_TEST_TABLE + testTableSuffix);
        try {
            this.c3p0ConnectionPool.setConnectionTesterClassName(ConnectionAlivenessTester.class.getName()); //default: com.mchange.v2.c3p0.impl.DefaultConnectionTester
        } catch (PropertyVetoException pve) {
            throw new ConnectionPoolInitializationFailedException("fail to initialize Connection Tester class", pve);
        }

        //If additional properties exist, we merge them with the C3PO
        // properties
        final Properties c3poProps = this.c3p0ConnectionPool.getProperties();
        final Properties configProps = configuration.getProperties();
        if (configProps != null) {
            Iterator it = configProps.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                c3poProps.setProperty(key, configProps.getProperty(key));
            }
            //This overwrites all properties
            this.c3p0ConnectionPool.setProperties(c3poProps);
        }
        closed = false;
        
        if (LOG.isInfoEnabled()) {
        	String poolProperties = this.c3p0ConnectionPool.toString().replaceAll(",", ",\n");
        	LOG.info("Initialized connection pool: '" + this.c3p0ConnectionPool.getDescription() + "' properties:" + poolProperties);
        }        
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to obtain a connection from the connection pool: '" + this.name + "'...");
        }
        Connection conn = this.c3p0ConnectionPool.getConnection();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Succesfully obtained connection from connection pool: '" + this.name 
                    + "'.\n[# of connections: '" + this.c3p0ConnectionPool.getNumConnectionsAllUsers() 
                    + "']\n[# of busy connections: '" + this.c3p0ConnectionPool.getNumBusyConnectionsAllUsers() 
                    + "']\n[# of idle connections: '" + this.c3p0ConnectionPool.getNumIdleConnectionsAllUsers() 
                    + "']\n[# of unused orphansed connections: '" + this.c3p0ConnectionPool.getNumUnclosedOrphanedConnectionsAllUsers() 
                    + "']\n[# of connection pools: '" + this.c3p0ConnectionPool.getNumUserPools() + "']");
        }
        return conn;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#close()
     */
    @Override
    public void close() throws SQLException {
        LOG.debug("Closing connection pool '" + this.name + "'");
        this.c3p0ConnectionPool.close();
        closed = true;
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#releaseConnection(java.sql.Connection)
     */
    public void releaseConnection(Connection c) throws SQLException {
        c.close();
    }
}
