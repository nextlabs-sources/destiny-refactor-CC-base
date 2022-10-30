/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/DataSourceConfigurationDO.java#1 $
 */

public class RepositoryConfigurationDO implements IRepositoryConfigurationDO {

    /*
     * Variables:
     */
    private PropertyList propertyOverrides;
    private String name;
    private String connectionPoolName;
    private IConnectionPoolConfigurationDO connectionPoolConfig;

    /**
     * Constructor
     *  
     */
    public RepositoryConfigurationDO() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IRepositoryConfigurationDO#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IRepositoryConfigurationDO#getProperties()
     */
    public Properties getProperties() {
        Properties result = null;
        if (this.propertyOverrides != null) {
            result = this.propertyOverrides.getProperties();
        }
        return result;
    }

    /**
     * Sets the list of properties for this data source
     * 
     * @param propertyOverrides
     */
    public void setProperties(PropertyList listOfProperties) {
        this.propertyOverrides = listOfProperties;
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

    public void setConnectionPoolName(String name) {
        this.connectionPoolName = name;
    }

    public String getConnectionPoolName() {
        return this.connectionPoolName;
    }

    public void setConnectionPoolConfiguration(IConnectionPoolConfigurationDO config) {
        this.connectionPoolConfig = config;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IRepositoryConfigurationDO#getConnectionPoolConfiguration()
     */
    public IConnectionPoolConfigurationDO getConnectionPoolConfiguration() {
        return this.connectionPoolConfig;
    }
}