/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCSFComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.DestinyConfigurationManagerImpl;

/**
 * @author safdar
 * @version $Id:
 *          //depot/branch/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/compmgr/test/MockConfigurationManagerImpl.java#1 $
 */

public class MockConfigurationManagerImpl extends DestinyConfigurationManagerImpl {

    /**
     * Constructor
     *  
     */
    public MockConfigurationManagerImpl() {
        super();
    }

    /**
     * Constructor
     * 
     * @param schema
     * @param data
     * @param digesterRules
     */
    public MockConfigurationManagerImpl(String schema, String data, String digesterRules) {
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDestinyConfigurationManager#getApplicationUserConfiguration()
     */
    public IApplicationUserConfigurationDO getApplicationUserConfiguration() {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDestinyConfigurationManager#getRepositories()
     */
    public Set getRepositories() {
        return new HashSet();
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDestinyConfigurationManager#getDCCConfiguration(java.lang.String)
     */
    @Override
    public IDCCComponentConfigurationDO getDCCConfiguration(ServerComponentType type) {
        return new MockDCSFComponentConfigurationImpl();
    }
    
    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }

    /**
     * Mock class to represent a DCC component configuration
     * 
     * @author safdar
     */
    private class MockDCSFComponentConfigurationImpl implements IDCSFComponentConfigurationDO {

        /**
         * @see com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO#getHeartbeatInterval()
         */
        public int getHeartbeatInterval() {
            return 5;
        }

        @Override
        public Properties getProperties() {
            return null;
        }
    }
}