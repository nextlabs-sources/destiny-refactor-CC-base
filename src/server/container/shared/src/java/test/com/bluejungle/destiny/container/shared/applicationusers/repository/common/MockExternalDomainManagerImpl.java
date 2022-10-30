/*
 * Created on Sep 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.MockApplicationUserManagerConfigurationImpl;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/openldapimpl/MockExternalDomainManagerImpl.java#1 $
 */

public class MockExternalDomainManagerImpl implements IExternalDomainManager {

    /*
     * Only 1 external domain for these tests:
     */
    public static final String SINGLETON_DOMAIN_NAME = MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME;

    public static final Map DOMAINS = new HashMap();
    static {
        DOMAINS.put(SINGLETON_DOMAIN_NAME, new MockExternalDomainImpl(SINGLETON_DOMAIN_NAME));
    }

    /**
     * Constructor
     *  
     */
    public MockExternalDomainManagerImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager#initialize(java.util.Set)
     */
    public void initialize(Set domainConfigurations) throws InvalidConfigurationException, InitializationException {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager#getAllDomainNames()
     */
    public Set getAllDomainNames() {
        return DOMAINS.keySet();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager#getExternalDomain(java.lang.String)
     */
    public IExternalDomain getExternalDomain(String domainName) throws DomainNotFoundException {
        if (DOMAINS.get(domainName) == null) {
            throw new DomainNotFoundException("domain: '" + domainName + "' not found");
        }
        return (IExternalDomain) DOMAINS.get(domainName);
    }
}