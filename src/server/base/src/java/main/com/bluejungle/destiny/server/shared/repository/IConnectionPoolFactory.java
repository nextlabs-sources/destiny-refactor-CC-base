/*
 * Created on Aug 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/repository/IConnectionPoolFactory.java#1 $
 */

public interface IConnectionPoolFactory {

    /**
     * Returns a connection pool corresponding to the provided configuration's
     * name attribute. If one doesn't exist already for this configuration, it
     * will be created lazily. The "name" attribute of the configuration should
     * be unique for each connection pool.
     * 
     * @param configuration
     * @throws ConnectionPoolInitializationFailedException
     * @throws ConnectionPoolConfigurationException
     * @return connection pool
     */
    public IConnectionPool getConnectionPool(IConnectionPoolConfiguration configuration) throws ConnectionPoolInitializationFailedException, ConnectionPoolConfigurationException;

    /**
     * Returns the connection pool with the provided name, if one exists.
     * Returns null otherwise.
     * 
     * @param connectionPoolName
     * @return connection pool instance
     */
    public IConnectionPool getConnectionPoolByName(String connectionPoolName);
}