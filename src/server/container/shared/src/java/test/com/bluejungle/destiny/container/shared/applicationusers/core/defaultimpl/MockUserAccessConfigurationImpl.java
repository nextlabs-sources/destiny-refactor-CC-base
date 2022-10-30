/*
 * Created on Sep 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IUserAccessConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockUserAccessConfigurationImpl.java#1 $
 */

public class MockUserAccessConfigurationImpl implements IUserAccessConfigurationDO {

    private String userAccessClassName;
    private Properties properties;

    /**
     * Constructor
     *  
     */
    public MockUserAccessConfigurationImpl(String userAccessClassName, Properties properties) {
        super();
        this.userAccessClassName = userAccessClassName;
        this.properties = properties;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IUserAccessConfigurationDO#getProviderClassName()
     */
    public String getProviderClassName() {
        return this.userAccessClassName;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IUserAccessConfigurationDO#getProperties()
     */
    public Properties getProperties() {
        return this.properties;
    }

}