/*
 * Created on Aug 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/repository/IConnectionPool.java#1 $
 */

public interface IConnectionPool {

    public String getName();

    public void initialize(IConnectionPoolConfiguration configuration) throws ConnectionPoolConfigurationException, ConnectionPoolInitializationFailedException;

    public Connection getConnection() throws SQLException;

    public void releaseConnection(Connection c) throws SQLException;

    public void close() throws SQLException;
    
    public boolean isClosed();
    
}