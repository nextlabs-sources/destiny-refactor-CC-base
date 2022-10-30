/*
 * Created on Sep 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IUserRepositoryConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockUserRepositoryProviderConfigurationImpl.java#1 $
 */

public class MockUserRepositoryProviderConfigurationImpl implements IUserRepositoryConfigurationDO {

    private String providerClassName;
    private Properties properties;

    /**
     * Constructor
     *  
     */
    public MockUserRepositoryProviderConfigurationImpl(String providerClassName, Properties properties) {
        super();
        this.properties = properties;
        this.providerClassName = providerClassName;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IUserRepositoryConfigurationDO#getProviderClassName()
     */
    public String getProviderClassName() {
        return this.providerClassName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepositoryProviderConfiguration#getProperties()
     */
    public Properties getProperties() {
        return this.properties;
    }
}