/*
 * Created on Aug 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

import java.util.Properties;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/IConnectionPoolConfigurationDO.java#1 $
 */

public interface IConnectionPoolConfigurationDO {

    /**
     * Returns the unique name of this connection pool
     * 
     * @return unique name
     */
    public String getName();

    /**
     * Returns the username for this connection pool
     * 
     * @return username
     */
    public String getUserName();

    /**
     * Returns the user password for this connection pool
     * 
     * @return user password
     */
    public String getPassword();

    /**
     * Returns the JDBC Connect String for the backing database
     * 
     * @return JDBC connect string
     */
    public String getJDBCConnectString();

    /**
     * Returns the class name of the JDBC driver for this connection pool
     * 
     * @return JDBC driver class name
     */
    public String getDriverClassName();

    /**
     * Returns the max pool size for this connection pool
     * 
     * @return max pool size
     */
    public int getMaxPoolSize();

    /**
     * Returns the list of implementation-specific property overrides, if any,
     * for this connection pool
     * 
     * @return properties
     */
    public Properties getProperties();
}