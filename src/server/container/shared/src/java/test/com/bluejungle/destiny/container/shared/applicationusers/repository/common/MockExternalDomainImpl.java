/*
 * Created on Sep 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.common;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain;
import com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/openldapimpl/MockExternalDomainImpl.java#1 $
 */

public class MockExternalDomainImpl implements IExternalDomain {

    private String name;
    private MockUserAccessProviderImpl userAccessProvider;

    /**
     * Constructor
     *  
     */
    public MockExternalDomainImpl(String name) {
        super();
        this.name = name;
        this.userAccessProvider = new MockUserAccessProviderImpl();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain#initialize(com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO)
     */
    public void initialize(IExternalDomainConfigurationDO configuration) throws InvalidConfigurationException, InitializationException {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain#getUserAccessProvider()
     */
    public IUserAccessProvider getUserAccessProvider() {
        return this.userAccessProvider;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain#getAuthenticator()
     */
    public IAuthenticator getAuthenticator() {
        throw new UnsupportedOperationException("method not supported");
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomain#getName()
     */
    public String getName() {
        return this.name;
    }
}