/*
 * Created on Jun 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;

/**
 * Implementation class for an external domain
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/defaultimpl/ExternalDomainImpl.java#1 $
 */

public class ExternalDomainImpl implements IExternalDomain {

    private String domainName;
    private IAuthenticator authenticator;
    private IUserAccessProvider userAccessProvider;

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain#initialize(com.bluejungle.framework.applicationusers.IExternalDomainConfiguration)
     */
    public void initialize(IExternalDomainConfigurationDO configuration) throws InvalidConfigurationException, InitializationException {
        this.domainName = configuration.getDomainName();
        try {
            String jaasModuleClassName = configuration.getAuthenticatorConfiguration().getAuthenticatorClassName();
            String userAccessProviderClassName = configuration.getUserAccessConfiguration().getProviderClassName();
            this.authenticator = (IAuthenticator) Class.forName(jaasModuleClassName).newInstance();
            this.authenticator.initialize(configuration.getAuthenticatorConfiguration().getProperties());
            this.userAccessProvider = (IUserAccessProvider) Class.forName(userAccessProviderClassName).newInstance();
            this.userAccessProvider.initialize(this.domainName, configuration.getUserAccessConfiguration().getProperties());
        } catch (IllegalAccessException e) {
            throw new InitializationException(e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomain#getName()
     */
    public String getName() {
        return this.domainName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain#getAuthenticator()
     */
    public IAuthenticator getAuthenticator() {
        return this.authenticator;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain#getUserAccessProvider()
     */
    public IUserAccessProvider getUserAccessProvider() {
        return this.userAccessProvider;
    }
}