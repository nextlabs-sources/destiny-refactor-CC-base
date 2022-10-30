/*
 * Created on Jun 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;

/**
 * Implementation class for managing external domains, if using remote
 * authentication.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ExternalDomainManagerImpl.java#1 $
 */

public class ExternalDomainManagerImpl implements IExternalDomainManager {

    private Map domains = new HashMap();

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager#initialize(java.util.Set)
     */
    public void initialize(Set domainConfigurations) throws InvalidConfigurationException, InitializationException {
        Iterator iter = domainConfigurations.iterator();
        while (iter.hasNext()) {
            IExternalDomainConfigurationDO config = (IExternalDomainConfigurationDO) iter.next();
            IExternalDomain domain = new ExternalDomainImpl();
            domain.initialize(config);
            this.domains.put(domain.getName(), domain);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager#getAllDomainNames()
     */
    public Set getAllDomainNames() {
        return this.domains.keySet();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager#getExternalDomain(java.lang.String)
     */
    public IExternalDomain getExternalDomain(String domainName) throws DomainNotFoundException {
        if (this.domains.get(domainName)==null) {
            throw new DomainNotFoundException("domain: '" + domainName + "' not found");
        }
        IExternalDomain domain = (IExternalDomain) this.domains.get(domainName);
        return domain;
    }
}