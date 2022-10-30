/*
 * Created on Jul 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IUserRepositoryConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/UserRepositoryConfigurationDO.java#1 $
 */

public class UserRepositoryConfigurationDO implements IUserRepositoryConfigurationDO {

    private String providerClassName;
    private Properties properties;

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IUserRepositoryConfigurationDO#getProviderClassName()
     */
    public String getProviderClassName() {
        return this.providerClassName;
    }

    /**
     * Sets the provider class name
     * 
     * @param className
     */
    public void setProviderClassName(String className) {
        this.providerClassName = className;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IUserRepositoryConfigurationDO#getProperties()
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Sets the properties
     * 
     * @param propertyList
     */
    public void setProperties(PropertyList propertyList) {
        if (propertyList != null) {
            this.properties = propertyList.getProperties();
        }
    }
}