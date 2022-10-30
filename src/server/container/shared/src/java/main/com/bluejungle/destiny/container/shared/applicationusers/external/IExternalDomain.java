/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.IDomain;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/applicationusers/IExternalDomain.java#1 $
 */

public interface IExternalDomain extends IDomain {

    /**
     * Initialize
     * 
     * @param configuration
     * @throws InvalidConfigurationException
     * @throws InitializationException
     */
    public void initialize(IExternalDomainConfigurationDO configuration) throws InitializationException, InvalidConfigurationException;

    /**
     * Returns the external user access provider class
     * 
     * @return external user provider
     */
    public IUserAccessProvider getUserAccessProvider();

    /**
     * Returns the authenticator for this external domain - this authenticator
     * instance should NOT have a dependency on the external domain instance. It
     * must be able to exist as a separate entity with a separate lifecycle.
     * 
     * @return
     */
    public IAuthenticator getAuthenticator();
}