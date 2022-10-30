/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

import java.util.Set;

import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepositoryProviderConfiguration;

/**
 * This interface represents the configuration for the top-level Application
 * User component.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/IApplicationUserManagerConfiguration.java#2 $
 */

public interface IApplicationUserManagerConfiguration {

    /**
     * Returns the authentication mode
     * 
     * @return
     */
    public AuthenticationModeEnumType getAuthenticationMode();

    /**
     * Returns the login name of the virtual super user
     * 
     * @return
     */
    public String getSuperUserLogin();

    /**
     * Returns the local repository configuration
     * 
     * @return
     */
    public IApplicationUserRepositoryProviderConfiguration getLocalRepositoryConfiguration();

    /**
     * Returns a set of configuration objects, one for each external domain
     * controller against which authentication/access is required. This method
     * may return an empty set if we're using local authentication.
     * 
     * @return set of external domain configurations
     */
    public Set getExternalDomainConfigurations();
}