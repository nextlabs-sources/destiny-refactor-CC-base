/*
 * Created on Sep 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IAuthenticatorConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockAuthenticatorConfigurationImpl.java#1 $
 */

public class MockAuthenticatorConfigurationImpl implements IAuthenticatorConfigurationDO {

    private String authenticatorClassName;
    private Properties properties;

    /**
     * Constructor
     */
    public MockAuthenticatorConfigurationImpl(String authenticatorClassName, Properties properties) {
        super();
        this.authenticatorClassName = authenticatorClassName;
        this.properties = properties;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticatorConfiguration#getAuthenticatorClassName()
     */
    public String getAuthenticatorClassName() {
        return this.authenticatorClassName;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IAuthenticatorConfigurationDO#getProperties()
     */
    public Properties getProperties() {
        return this.properties;
    }
}