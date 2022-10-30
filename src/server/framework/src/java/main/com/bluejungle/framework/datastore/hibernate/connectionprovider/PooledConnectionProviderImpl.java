/*
 * Created on Aug 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.connectionprovider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.connection.ConnectionProvider;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.repository.ConnectionPoolConfigurationException;
import com.bluejungle.destiny.server.shared.repository.ConnectionPoolInitializationFailedException;
import com.bluejungle.destiny.server.shared.repository.IConnectionPool;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.datastore.hibernate.HibernateConfiguration;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This class serves as a proxy for the actual connection pool stored in teh
 * shared context. It is connection-pool-implementation agnostic, and assumes
 * that the connection pool adheres to the IConnectionPool interface. The class
 * provides static initializer methods to initialize connection pools in teh
 * shared context - these methods are to be used exclusively by the
 * PoolBackedHibernateRepositoryImpl class to pass connection pool configuration
 * to the connection pool factory.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/connectionprovider/PooledConnectionProviderImpl.java#1 $
 */

public class PooledConnectionProviderImpl implements ConnectionProvider {

    private static final String CONNECTION_POOL_NAME_PARAM = "destiny.connectionpool.name";

    /*
     * Member variables:
     */
    private IConnectionPool connectionPool;
    
    /**
     * This method initializes a connection pool via the shared context. It then
     * piggy-backs the name of the connection pool onto the 'hibernateCfg'
     * parameter so that when the connection provider is actually instantiated
     * and configured with these properties, it knows what connection pool to
     * obtain from the shared context.
     * 
     * @param configuration
     * @param HibernateConfiguration
     *            instance to configure
     * @throws SQLException
     */
    public static void preConfigure(IConnectionPoolConfiguration configuration, HibernateConfiguration hibernateCfgToTag) throws ConnectionPoolInitializationFailedException, ConnectionPoolConfigurationException {
        IDestinySharedContext sharedCtx = getSharedContext();
        IConnectionPool connectionPool = sharedCtx.getConnectionPoolFactory().getConnectionPool(configuration);
        hibernateCfgToTag.setProperty(CONNECTION_POOL_NAME_PARAM, connectionPool.getName());
    }

    /**
     * Returns the shared context
     * 
     * @return shared context
     */
    protected static IDestinySharedContext getSharedContext() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);
        IDestinySharedContext sharedCtx = locator.getSharedContext();
        return sharedCtx;
    }

    /**
     * This method looks for the name of the connection pool - which was
     * appended by the appendConnectionPoolConfiguration() method. It then
     * obtains the connection pool with that name from the shared context.
     * 
     * @see net.sf.hibernate.connection.ConnectionProvider#configure(java.util.Properties)
     */
    public void configure(Properties props) throws HibernateException {
        IDestinySharedContext sharedCtx = getSharedContext();
        this.connectionPool = sharedCtx.getConnectionPoolFactory().getConnectionPoolByName(props.getProperty(CONNECTION_POOL_NAME_PARAM));
    }

    /**
     * @see net.sf.hibernate.connection.ConnectionProvider#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return this.connectionPool.getConnection();
    }

    /**
     * @see net.sf.hibernate.connection.ConnectionProvider#closeConnection(java.sql.Connection)
     */
    public void closeConnection(Connection conn) throws SQLException {
        this.connectionPool.releaseConnection(conn);
    }

    /**
     * @see net.sf.hibernate.connection.ConnectionProvider#close()
     */
    public void close() throws HibernateException {
        try {
            this.connectionPool.close();
        } catch (SQLException e) {
            throw new HibernateException(e);
        }
    }
    
}