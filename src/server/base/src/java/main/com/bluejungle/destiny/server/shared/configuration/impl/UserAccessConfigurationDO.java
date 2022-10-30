/*
 * Created on Jul 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IUserAccessConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/UserAccessConfigurationDO.java#1 $
 */

public class UserAccessConfigurationDO implements IUserAccessConfigurationDO {

    private String providerClassName;
    private Properties properties;

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IUserAccessConfigurationDO#getProviderClassName()
     */
    public String getProviderClassName() {
        return this.providerClassName;
    }

    /**
     * Sets the provider class name
     * 
     * @param name
     */
    public void setProviderClassName(String name) {
        this.providerClassName = name;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IUserAccessConfigurationDO#getProperties()
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