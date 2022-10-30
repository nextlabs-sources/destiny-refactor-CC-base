/*
 * Created on Aug 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/ConnectionPoolConfigurationDO.java#1 $
 */

public class ConnectionPoolConfigurationDO implements IConnectionPoolConfigurationDO {

    private String name;
    private String userName;
    private String password;
    private String jdbcConnectString;
    private int maxPoolSize;
    private Properties properties;
    private String driverClassName;
    private boolean isPasswordDecoded = false;

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IConnectionPoolConfigurationDO#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IConnectionPoolConfigurationDO#getUserName()
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IConnectionPoolConfigurationDO#getPassword()
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IConnectionPoolConfigurationDO#getJDBCConnectString()
     */
    public String getJDBCConnectString() {
        return this.jdbcConnectString;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IConnectionPoolConfigurationDO#getMaxPoolSize()
     */
    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IConnectionPoolConfigurationDO#getProperties()
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Sets the property overrides
     * 
     * @param properites
     *            The properties to set.
     */
    public void setProperties(PropertyList props) {
        if (props != null) {
            this.properties = props.getProperties();
        }
    }

    /**
     * Sets the jDBCConnectString
     * 
     * @param connectString
     *            The jDBCConnectString to set.
     */
    public void setJDBCConnectString(String connectString) {
        this.jdbcConnectString = connectString;
    }

    /**
     * Sets the maxPoolSize
     * 
     * @param maxPoolSize
     *            The maxPoolSize to set.
     */
    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize.intValue();
    }

    /**
     * Sets the name
     * 
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the password
     * 
     * @param password
     *            The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the userName
     * 
     * @param userName
     *            The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IConnectionPoolConfigurationDO#getDriverClassName()
     */
    public String getDriverClassName() {
        return this.driverClassName;
    }

    /**
     * Sets the driverClassName
     * 
     * @param driverClassName
     *            The driverClassName to set.
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * Set password decoded flag, to prevent decode twice 
     */
    public void setIsPasswordDecoded(boolean decoded) {
        this.isPasswordDecoded = decoded;
    }

    /**
     * Get password decode flag, to prevent decode twice
     */
    public boolean getIsPasswordDecoded() {
        return this.isPasswordDecoded;
    }

}
