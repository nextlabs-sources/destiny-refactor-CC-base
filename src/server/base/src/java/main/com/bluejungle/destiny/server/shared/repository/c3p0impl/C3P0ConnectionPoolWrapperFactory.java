/*
 * Created on Aug 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository.c3p0impl;

import com.bluejungle.destiny.server.shared.repository.ConnectionPoolConfigurationException;
import com.bluejungle.destiny.server.shared.repository.ConnectionPoolInitializationFailedException;
import com.bluejungle.destiny.server.shared.repository.ConnectionTrackingConnectionPoolWrapper;
import com.bluejungle.destiny.server.shared.repository.IConnectionPool;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/repository/c3p0impl/C3P0ConnectionPoolWrapperFactory.java#1 $
 */

public class C3P0ConnectionPoolWrapperFactory implements IConnectionPoolFactory {

    private Map<String, IConnectionPool> connectionPoolsByName = new HashMap<String, IConnectionPool>();

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory#getConnectionPool(com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration)
     */
    public synchronized IConnectionPool getConnectionPool(IConnectionPoolConfiguration configuration)
            throws ConnectionPoolConfigurationException, ConnectionPoolInitializationFailedException {
        IConnectionPool connectionPoolToReturn = null;
        if (configuration.getName() == null) {
            throw new NullPointerException("connection pool name is null");
        }

        connectionPoolToReturn = this.connectionPoolsByName.get(configuration.getName());
        
        if (connectionPoolToReturn == null) {
            connectionPoolToReturn = new ConnectionTrackingConnectionPoolWrapper(
                    new C3P0ConnectionPoolWrapper(configuration.getName()));
            connectionPoolToReturn.initialize(configuration);
            this.connectionPoolsByName.put(configuration.getName(), connectionPoolToReturn);
        } else if(connectionPoolToReturn.isClosed()) {
            connectionPoolToReturn.initialize(configuration);
        }
        return connectionPoolToReturn;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory#getConnectionPoolByName(java.lang.String)
     */
    public synchronized IConnectionPool getConnectionPoolByName(String connectionPoolName) {
        return this.connectionPoolsByName.get(connectionPoolName);
    }
}
