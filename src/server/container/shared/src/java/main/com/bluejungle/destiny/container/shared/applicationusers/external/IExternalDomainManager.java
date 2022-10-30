/*
 * Created on Jun 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external;

import java.util.Set;

import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;

/**
 * This interface represents an object that manages external domains, as
 * "importable" user domains as well as authentication domains.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/IExternalDomainManager.java#1 $
 */

public interface IExternalDomainManager {

    public void initialize(Set<IExternalDomainConfigurationDO> domainConfigurations) throws InvalidConfigurationException, InitializationException;

    public Set<String> getAllDomainNames();

    public IExternalDomain getExternalDomain(String domainName) throws DomainNotFoundException;
}