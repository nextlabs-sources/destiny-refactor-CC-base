/*
 * Created on Dec 7, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms;

import java.util.Iterator;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.DefaultContainerImpl;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.dcc.WebRelativeFolders;
import com.bluejungle.destiny.container.dms.components.compmgr.DCCRegistrationBrokerImpl;
import com.bluejungle.destiny.container.dms.components.compmgr.IDCCRegistrationBroker;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.AbstractConfigurableHibernateRepositoryImpl;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.PoolBackedHibernateRepositoryImpl;
import com.bluejungle.framework.environment.IResourceLocator;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.IDestinyConfigurationManager;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.DestinyConfigurationManagerImpl;

/**
 * This is the DMS container implementation class. The DMS container
 * pre-initializes a couples of sub-components running within DMS (the DCC
 * component registration manager and the event registration manager). These two
 * sub components have to be used early for the component registration process.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/DMSContainerImpl.java#15 $:
 */

public class DMSContainerImpl extends DefaultContainerImpl implements IDCCContainer {

    /**
     * Initialization function. This function pre-initializes the component
     * manager so that the registration of other DCC components can occur
     * properly. Since the component manager is a singleton, this initialization
     * is transparent for the real DMS DCC Component class.
     */
    protected void doInit() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        // Initialize the shared context locator:
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);

        // Initialize the Destiny configuration manager (that is contained only
        // by DMS):
        ComponentInfo<IDestinyConfigurationManager> configMgrCompInfo = 
            new ComponentInfo<IDestinyConfigurationManager>(
                IDestinyConfigurationManager.COMP_NAME, 
                DestinyConfigurationManagerImpl.class, 
                IDestinyConfigurationManager.class, 
                LifestyleType.SINGLETON_TYPE);
        IDestinyConfigurationManager configMgr = compMgr.getComponent(configMgrCompInfo);

        // Boot-strap the data sources. This would normally be done in the
        // BaseComponentImpl for other DCC components, but for DMS we need do
        // add some bootstrap code.

        // Initialize the data source configuration file locator:
        IResourceLocator webAppFileLocator = (IResourceLocator) compMgr.getComponent(DCCResourceLocators.WEB_APP_RESOURCE_LOCATOR);

        // Initializes all the data sources:
        Set dataSourceList = configMgr.getRepositories();
        if (dataSourceList != null) {
            String dataSrcConfigFile_common = WebRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(DestinyRepository.COMMON_REPOSITORY.getConfigFileName());
            Iterator iter = dataSourceList.iterator();
            while (iter.hasNext()) {
                IRepositoryConfigurationDO repositoryConfig = (IRepositoryConfigurationDO) iter.next();
                DestinyRepository repository = DestinyRepository.getByName(repositoryConfig.getName());
                String dataSrcConfigFile = WebRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(repository.getConfigFileName());

                // Create the HibernateDataSource object only if the configs
                // exist:
                if (webAppFileLocator.exists(dataSrcConfigFile_common) && webAppFileLocator.exists(dataSrcConfigFile)) {
                    HashMapConfiguration dsConfig = new HashMapConfiguration();
                    dsConfig.setProperty(AbstractConfigurableHibernateRepositoryImpl.COMMON_CONFIG, webAppFileLocator.getResourceAsStream(dataSrcConfigFile_common));
                    dsConfig.setProperty(AbstractConfigurableHibernateRepositoryImpl.REPOSITORY_CONFIG, webAppFileLocator.getResourceAsStream(dataSrcConfigFile));
                    dsConfig.setProperty(PoolBackedHibernateRepositoryImpl.REPOSITORY_DEFINITION, repositoryConfig);
                    ComponentInfo<IHibernateRepository> dsInfo = 
                        new ComponentInfo<IHibernateRepository>(
                            repository.getName(), 
                            PoolBackedHibernateRepositoryImpl.class, 
                            IHibernateRepository.class, 
                            LifestyleType.SINGLETON_TYPE, 
                            dsConfig);
                    IHibernateRepository factory = compMgr.getComponent(dsInfo);
                }
            }
        }

        // Intialize the profile manager.  Needed by the registration broker
        // Profile manager requires agent manager
        ComponentInfo<IAgentManager> agentMgrCompInfo = 
            new ComponentInfo<IAgentManager> (
                IAgentManager.COMP_NAME, 
                AgentManager.class, 
                IAgentManager.class, 
                LifestyleType.SINGLETON_TYPE);
        IAgentManager agentMgr = compMgr.getComponent(agentMgrCompInfo);
        
        ComponentInfo<IProfileManager> profileMgrCompInfo = 
            new ComponentInfo<IProfileManager> (
                IProfileManager.COMP_NAME, 
                HibernateProfileManager.class, 
                IProfileManager.class, 
                LifestyleType.SINGLETON_TYPE);
        compMgr.registerComponent(profileMgrCompInfo, true);
        
        //Init the facade for the component and event registration
        //This component is not used, just initialized before the real
        //DMS component comes up
        ComponentInfo<IDCCRegistrationBroker> compFacadeInfo = 
            new ComponentInfo<IDCCRegistrationBroker>(
                IDCCRegistrationBroker.COMP_NAME, 
                DCCRegistrationBrokerImpl.class, 
                IDCCRegistrationBroker.class, 
                LifestyleType.SINGLETON_TYPE);
        IDCCRegistrationBroker componentAndEventFacade = compMgr.getComponent(compFacadeInfo);
    }
}
