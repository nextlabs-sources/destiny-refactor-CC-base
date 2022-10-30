/*
 * Created on Nov 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;

/**
 * This is a test representation of the Destiny configuration. Currently, the
 * production destiny configuration manager is located within DMS. It's
 * desirable to keep here to denote that's it's DMS reponsibility to read and
 * manage configuration. However, for teting purposes, it's useful to have
 * access to destiny configuration information. Therefore, implementations of
 * this interface will provide it
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/test/com/bluejungle/destiny/container/dcc/ITestDestinyConfiguration.java#1 $
 */

public interface ITestDestinyConfiguration {
    ComponentInfo<TestDestinyConfigurationImpl> COMP_INFO = 
    	new ComponentInfo<TestDestinyConfigurationImpl>(
    		"TestDestinyConfiguration", 
    		TestDestinyConfigurationImpl.class, 
    		LifestyleType.SINGLETON_TYPE);

    public IApplicationUserConfigurationDO getApplicationUserConfiguration();

    public IRepositoryConfigurationDO getRepositoryConfiguration(DestinyRepository repository);
    
    // the remaining configuration is not yet implemented
}
