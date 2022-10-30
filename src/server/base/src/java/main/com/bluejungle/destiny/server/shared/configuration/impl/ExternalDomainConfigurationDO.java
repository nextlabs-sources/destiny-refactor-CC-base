/*
 * Created on Jul 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IAuthenticatorConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IUserAccessConfigurationDO;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/ExternalDomainConfigurationDO.java#1 $
 */

public class ExternalDomainConfigurationDO implements IExternalDomainConfigurationDO {

    private String domainName;
    private IAuthenticatorConfigurationDO authenticatorConfiguration;
    private IUserAccessConfigurationDO userAccessConfiguration;

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IExternalDomainConfigurationDO#getDomainName()
     */
    public String getDomainName() {
        return this.domainName;
    }

    /**
     * Sets the domain name
     * 
     * @param name
     */
    public void setDomainName(String name) {
        this.domainName = name;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IExternalDomainConfigurationDO#getAuthenticatorConfiguration()
     */
    public IAuthenticatorConfigurationDO getAuthenticatorConfiguration() {
        return this.authenticatorConfiguration;
    }

    /**
     * Sets the authenticator configuration
     * 
     * @param authConfig
     */
    public void setAuthenticatorConfiguration(IAuthenticatorConfigurationDO authConfig) {
        this.authenticatorConfiguration = authConfig;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IExternalDomainConfigurationDO#getUserAccessConfiguration()
     */
    public IUserAccessConfigurationDO getUserAccessConfiguration() {
        return this.userAccessConfiguration;
    }

    /**
     * Sets the user access configuration
     * 
     * @param userAccessConfig
     */
    public void setUserAccessConfiguration(IUserAccessConfigurationDO userAccessConfig) {
        this.userAccessConfiguration = userAccessConfig;
    }
}