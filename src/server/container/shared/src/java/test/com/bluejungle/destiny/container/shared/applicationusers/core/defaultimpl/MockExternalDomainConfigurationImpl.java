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
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IUserAccessConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockExternalDomainConfigurationImpl.java#1 $
 */

public class MockExternalDomainConfigurationImpl implements IExternalDomainConfigurationDO {

    private String domainName;
    private String accessProvider;
    private Properties accessProviderProperties;
    private String authenticator;
    private Properties authenticatorProperties;

    /**
     * Constructor
     *  
     */
    public MockExternalDomainConfigurationImpl(String domainName, String accessProvider, Properties accessProviderProperties, String authenticator, Properties authenticatorProperties) {
        super();
        this.domainName = domainName;
        this.accessProvider = accessProvider;
        this.accessProviderProperties = accessProviderProperties;
        this.authenticator = authenticator;
        this.authenticatorProperties = authenticatorProperties;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainConfiguration#getDomainName()
     */
    public String getDomainName() {
        return this.domainName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainConfiguration#getAuthenticatorConfiguration()
     */
    public IAuthenticatorConfigurationDO getAuthenticatorConfiguration() {
        return new MockAuthenticatorConfigurationImpl(this.authenticator, this.authenticatorProperties);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainConfiguration#getUserAccessConfiguration()
     */
    public IUserAccessConfigurationDO getUserAccessConfiguration() {
        return new MockUserAccessConfigurationImpl(this.accessProvider, this.accessProviderProperties);
    }
}