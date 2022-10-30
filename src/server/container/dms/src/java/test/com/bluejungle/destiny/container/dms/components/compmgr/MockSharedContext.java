/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DMSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.exceptions.FactoryInitException;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;
import com.bluejungle.destiny.server.shared.repository.c3p0impl.C3P0ConnectionPoolWrapperFactory;
import com.bluejungle.destiny.services.management.types.ApplicationUserConfiguration;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.ConfigurationInitException;

/**
 * A Mock Shared Context
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/test/MockSharedContext.java#2 $
 */

public class MockSharedContext implements IDestinySharedContext {

    private IDestinyConfigurationStore configStore;
    private IDestinyRegistrationManager registrationMgr;
    private IDestinyEventManager eventMgr;
    private IConnectionPoolFactory connectionPoolFactory = new C3P0ConnectionPoolWrapperFactory();

    /**
     * Constructor
     * 
     * @throws ConfigurationInitException
     */
    public MockSharedContext() throws ConfigurationInitException {
        super();

        this.configStore = new MockDestinyConfigurationStore();
        this.registrationMgr = new MockRegistrationManager();
        this.eventMgr = new MockEventManager();
    }

    /**
     * @return the event manager
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getEventManager()
     */
    public IDestinyEventManager getEventManager() {
        return this.eventMgr;
    }

    /**
     * @return the configuration manager
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getConfigurationStore()
     */
    public IDestinyConfigurationStore getConfigurationStore() {
        return configStore;
    }

    /**
     * @return the registration manager
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getRegistrationManager()
     */
    public IDestinyRegistrationManager getRegistrationManager() {
        return registrationMgr;
    }

    /**
     * Initializes the shared context
     * 
     * @param confMgr
     *            configuration manager class name
     * @param eventMgr
     *            event manager class name
     * @param logMgr
     *            log manager class name
     * @param regMgr
     *            registration manager class name
     * @throws FactoryInitException
     *             if init fails
     */
    public void init(String confMgr, String eventMgr, String logMgr, String regMgr) throws FactoryInitException {
    }

    /**
     * Mock configuration manager for adding properties which are normally
     * retreieved through a JNDI context in the DMS web app
     * 
     * @author sgoldstein
     */
    private class MockDestinyConfigurationStore extends DestinyConfigurationStoreImpl {

        /**
         * Constructor
         *  
         */
        public MockDestinyConfigurationStore() {
            super();
        }

        /**
         * @see com.bluejungle.destiny.server.shared.configuration.IDestinyConfigurationStore#cacheAuthConfig(com.bluejungle.destiny.services.management.types.ApplicationUserConfiguration)
         */
        public synchronized void cacheAuthConfig(ApplicationUserConfiguration config) {
        }

        /**
         * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#cacheComponentConfiguration(java.lang.String,
         *      com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO)
         */
        public synchronized void cacheComponentConfiguration(String type, IDCCComponentConfigurationDO config) {
            super.cacheComponentConfiguration(type, config);
        }

        /**
         * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#cacheRepositoryConfigurations(com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList)
         */
        public synchronized void cacheRepositoryConfigurations(RepositoryConfigurationList configs) {
            super.cacheRepositoryConfigurations(configs);
        }

        /**
         * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#retrieveAuthConfig()
         */
        public synchronized IApplicationUserConfigurationDO retrieveAuthConfig() {
            return new ApplicationUserConfigurationDO();
        }

        /**
         * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#retrieveComponentConfiguration(java.lang.String)
         */
        public synchronized IDCCComponentConfigurationDO retrieveComponentConfiguration(String type) {
            return new DMSComponentConfigurationDO();
        }

        /**
         * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#retrieveRepositoryConfigurations()
         */
        public synchronized RepositoryConfigurationList retrieveRepositoryConfigurations() {
            return new RepositoryConfigurationList();
        }
    }

    /**
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getConnectionPoolFactory()
     */
    public IConnectionPoolFactory getConnectionPoolFactory() {
        return this.connectionPoolFactory;
    }
}
