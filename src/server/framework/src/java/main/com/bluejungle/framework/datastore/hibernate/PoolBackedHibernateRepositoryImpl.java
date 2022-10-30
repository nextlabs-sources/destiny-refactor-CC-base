/*
 * Created on Dec 11, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import net.sf.hibernate.cfg.Environment;

import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.repository.ConnectionPoolConfigurationException;
import com.bluejungle.destiny.server.shared.repository.ConnectionPoolInitializationFailedException;
import com.bluejungle.destiny.server.shared.repository.ConnectionTrackingConnectionPoolWrapper;
import com.bluejungle.destiny.server.shared.repository.IConnectionPool;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.connectionprovider.PooledConnectionProviderImpl;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This implementation allows for connection pooling and also supports property
 * overrides from the configuration
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class PoolBackedHibernateRepositoryImpl extends AbstractConfigurableHibernateRepositoryImpl {

    public static final PropertyKey<IRepositoryConfigurationDO> REPOSITORY_DEFINITION = 
        new PropertyKey<IRepositoryConfigurationDO>(PoolBackedHibernateRepositoryImpl.class, "RepositoryDefinition");

    /**
     * Initializes the factory object with the provided configuration data.
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void doCustomConfiguration(HibernateConfiguration hibernateCfg) throws ConnectionPoolInitializationFailedException, ConnectionPoolConfigurationException {
        // Obtain the relevant repository definition from the configuration:
        IRepositoryConfigurationDO dataSourceDefinition = (IRepositoryConfigurationDO) getConfiguration().get(PoolBackedHibernateRepositoryImpl.REPOSITORY_DEFINITION);

        // 1. Set the property-overrides as obtained from destiny configuration:
        Properties props = dataSourceDefinition.getProperties();
        for(Map.Entry e : props.entrySet()){
            String currentKey = (String)e.getKey();
            String currentValue = (String)e.getValue();
            hibernateCfg.setProperty(currentKey, currentValue);
        }
        
        // 2. Piggy-back connection pool configuration, if applicable:
        // This is applicable if either the customer has chosen no connection
        // provider, or if the configuration is explicitly set to our custom
        // connection provider:
        if ((hibernateCfg.getProperty(Environment.CONNECTION_PROVIDER) == null) || (hibernateCfg.getProperty(Environment.CONNECTION_PROVIDER).equals(PooledConnectionProviderImpl.class.getName()))) {
            IConnectionPoolConfiguration connectionPoolConfiguration = new ConnectionPoolConfigurationImpl(dataSourceDefinition.getConnectionPoolConfiguration());
            PooledConnectionProviderImpl.preConfigure(connectionPoolConfiguration, hibernateCfg);

            // In case the provider class name was specified as null, we should
            // set it to our own custom provider so that hibernate instantiates
            // the right provider internally:
            hibernateCfg.setProperty(Environment.CONNECTION_PROVIDER, PooledConnectionProviderImpl.class.getName());
        }
    }
    
    @Override
    protected void doPostCustomConfiguration(HibernateConfiguration hibernateCfg)
            throws Exception {
        assert hibernateCfg.getProperty(Environment.CONNECTION_PROVIDER).equals(PooledConnectionProviderImpl.class.getName());
        
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);
        IDestinySharedContext sharedCtx = locator.getSharedContext();
        
        IRepositoryConfigurationDO dataSourceDefinition = (IRepositoryConfigurationDO) getConfiguration().get(PoolBackedHibernateRepositoryImpl.REPOSITORY_DEFINITION);
        IConnectionPoolConfiguration connectionPoolConfiguration = new ConnectionPoolConfigurationImpl(dataSourceDefinition.getConnectionPoolConfiguration());
        IConnectionPool connectionPool = sharedCtx.getConnectionPoolFactory().getConnectionPool(connectionPoolConfiguration);
        assert !connectionPool.isClosed();
        
        // there is a issue with connection pool tracker
        // when the connection is closed, the pool doesn't know
        // look net.sf.hibernate.tool.hbm2ddl.SchemaUpdate.execute 
        if(connectionPool instanceof ConnectionTrackingConnectionPoolWrapper){
            ((ConnectionTrackingConnectionPoolWrapper)connectionPool).reset();
        }
    }

    /**
     * Class to wrap a connection pool WS object into the connection pool
     * interface expected by the connection pool factory
     * 
     * @author safdar
     */
    private class ConnectionPoolConfigurationImpl implements IConnectionPoolConfiguration {

        private String jdbcConnectString;
        private int maxPoolSize;
        private String connectionPoolName;
        private String username;
        private String password;
        private Properties properties;
        private String driverClassName;

        /**
         * Constructor
         *  
         */
        public ConnectionPoolConfigurationImpl(IConnectionPoolConfigurationDO configuration) {
            // Read the strongly typed properties:
            this.jdbcConnectString = configuration.getJDBCConnectString().toString();
            this.maxPoolSize = configuration.getMaxPoolSize();
            this.connectionPoolName = configuration.getName();
            this.username = configuration.getUserName();
            this.password = configuration.getPassword();
            this.driverClassName = configuration.getDriverClassName();
            this.properties = configuration.getProperties();
            if (this.properties == null) {
                this.properties = new Properties();
            }
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getJDBCConnectString()
         */
        public String getJDBCConnectString() {
            return this.jdbcConnectString;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getMaxPoolSize()
         */
        public int getMaxPoolSize() {
            return this.maxPoolSize;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getName()
         */
        public String getName() {
            return this.connectionPoolName;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getPassword()
         */
        public String getPassword() {
            return this.password;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getProperties()
         */
        public Properties getProperties() {
            return this.properties;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getUserName()
         */
        public String getUserName() {
            return this.username;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration#getDriverClassName()
         */
        public String getDriverClassName() {
            return this.driverClassName;
        }
    }
}