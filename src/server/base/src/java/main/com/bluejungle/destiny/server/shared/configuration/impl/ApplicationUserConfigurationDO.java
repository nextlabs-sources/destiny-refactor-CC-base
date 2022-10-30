/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IUserRepositoryConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/AuthConfigurationDO.java#1 $
 */

public class ApplicationUserConfigurationDO implements IApplicationUserConfigurationDO {

    private IExternalDomainConfigurationDO externalDomainConfiguration;
    private IUserRepositoryConfigurationDO userRepositoryConfiguration;
    private String authenticationMode;

    public ApplicationUserConfigurationDO() {
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IApplicationUserConfigurationDO#getExternalDomainConfiguration()
     */
    public IExternalDomainConfigurationDO getExternalDomainConfiguration() {
        return this.externalDomainConfiguration;
    }

    /**
     * Sets the external domain config
     * 
     * @param externalConfig
     */
    public void setExternalDomainConfiguration(IExternalDomainConfigurationDO externalConfig) {
        this.externalDomainConfiguration = externalConfig;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IApplicationUserConfigurationDO#getUserRepositoryConfiguration()
     */
    public IUserRepositoryConfigurationDO getUserRepositoryConfiguration() {
        return this.userRepositoryConfiguration;
    }

    /**
     * Sets the user repository config
     * 
     * @param userConfig
     */
    public void setUserRepositoryConfiguration(IUserRepositoryConfigurationDO userConfig) {
        this.userRepositoryConfiguration = userConfig;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO#getAuthenticationMode()
     */
    public String getAuthenticationMode() {
        return this.authenticationMode;
    }

    /**
     * Sets the authenticationMode
     * 
     * @param authenticationMode
     *            The authenticationMode to set.
     */
    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }
}