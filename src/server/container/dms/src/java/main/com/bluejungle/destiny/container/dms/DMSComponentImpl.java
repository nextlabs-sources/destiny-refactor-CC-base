/*
 * Created on Oct 20, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dms;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.FileSystemResourceLocatorImpl;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentMgr;
import com.bluejungle.destiny.container.dms.components.discovery.AutoDiscoveryServerImpl;
import com.bluejungle.destiny.container.dms.components.discovery.IAutoDiscoveryServer;
import com.bluejungle.destiny.container.dms.components.licenseauditor.ILicenseAuditor;
import com.bluejungle.destiny.container.dms.components.sharedfolder.defaultimpl.DMSSharedFolderInformationSourceImpl;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.nextlabs.destiny.container.shared.customapps.CustomAppDataManager;
import com.nextlabs.destiny.container.shared.customapps.ExternalApplicationLoader;
import com.nextlabs.destiny.container.shared.utils.DCCComponentHelper;

/**
 * This is the DMS component class. This component creates the various
 * sub-component running within the DMS process.
 * 
 * @author ihanen
 */
public class DMSComponentImpl extends BaseDCCComponentImpl implements IDestinyEventListener {


    /**
     * Container destruction method
     */
    public void dispose() {
        super.dispose();
    }

    /**
     * Process a Destiny event
     * 
     * @param event
     *            event to be fired
     */
    public void onDestinyEvent(IDCCServerEvent event) {
        System.out.println("DMS received event " + event.getName());
    }

    /**
     * DMS container initialization. Components running inside the DMS container
     * are initialized here.
     */
    public void init() {

        setComponentType(ServerComponentType.DMS);
        super.init();

        IComponentManager compMgr = getManager();
        compMgr.registerComponent(ServerSpecManager.COMP_INFO, true);

        // Ensure that the required data source(s) exist(s). These should have
        // been initialized by the BaseDCCComponentImpl based on the
        // configuration file for this component.
        IHibernateRepository mgmtDataSrc = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        if (mgmtDataSrc == null) {
            throw new RuntimeException("Data source " + DestinyRepository.MANAGEMENT_REPOSITORY.getName() + " is not correctly setup for the DMS component.");
        }

        // Initialize the destiny configuration-files resource locator:
        INamedResourceLocator serverResourceLocator = (INamedResourceLocator) compMgr.getComponent(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);
        String configDirRelativePath = ServerRelativeFolders.CONFIGURATION_FOLDER.getPath();
        String configDirFullPath = serverResourceLocator.getFullyQualifiedName(configDirRelativePath);
        HashMapConfiguration configLocatorConfig = new HashMapConfiguration();
        configLocatorConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, configDirFullPath);
        ComponentInfo<INamedResourceLocator> configLocatorInfo = 
            new ComponentInfo<INamedResourceLocator>(
                DMSResourceLocators.CONFIGURATION_RESOURCE_LOCATOR, 
                FileSystemResourceLocatorImpl.class, 
                INamedResourceLocator.class, 
                LifestyleType.SINGLETON_TYPE, 
                configLocatorConfig);
        compMgr.getComponent(configLocatorInfo);


        DCCComponentHelper.initSecurityComponents(getManager(), getLog());

        //Prepares the aut-discovery component
        final IProfileManager profileMgr = (IProfileManager) compMgr.getComponent(IProfileManager.COMP_NAME);
        IAgentManager agentMgr = (IAgentManager) compMgr.getComponent(IAgentManager.COMP_NAME);
        HashMapConfiguration discoveryServerConfig = new HashMapConfiguration();
        IDCCComponentMgr dccCompMgr = (IDCCComponentMgr) compMgr.getComponent(IDCCComponentMgr.COMP_NAME);
        discoveryServerConfig.setProperty(IAutoDiscoveryServer.CERT_LOCATION_CONFIG_PARAM, serverResourceLocator.getFullyQualifiedName(ServerRelativeFolders.CERTIFICATES_FOLDER.getPath()));
        discoveryServerConfig.setProperty(IAutoDiscoveryServer.DCC_COMP_MGR_CONFIG_PARAM, dccCompMgr);
        discoveryServerConfig.setProperty(IAutoDiscoveryServer.PROFILE_MGR_CONFIG_PARAM, profileMgr);
        discoveryServerConfig.setProperty(IAutoDiscoveryServer.AGENT_MGR_CONFIG_PARAM, agentMgr);
        ComponentInfo<IAutoDiscoveryServer> discoveryServerCompInfo = 
            new ComponentInfo<IAutoDiscoveryServer>(
                IAutoDiscoveryServer.COMP_NAME, 
                AutoDiscoveryServerImpl.class, 
                IAutoDiscoveryServer.class, 
                LifestyleType.SINGLETON_TYPE, 
                discoveryServerConfig);
        compMgr.getComponent(discoveryServerCompInfo);

        // Initialize the SharedFolderInformation relay for DMS:
        ComponentInfo<ISharedFolderInformationSource> sharedFolderInfoSourceCompInfo = 
            new ComponentInfo<ISharedFolderInformationSource>(
                    ISharedFolderInformationSource.COMP_NAME, 
                    DMSSharedFolderInformationSourceImpl.class, 
                    ISharedFolderInformationSource.class, 
                    LifestyleType.SINGLETON_TYPE);
        ISharedFolderInformationSource sharedFolderInfoSource = compMgr.getComponent(sharedFolderInfoSourceCompInfo);

        ILicenseAuditor licenseAuditor = (ILicenseAuditor) compMgr.getComponent(ILicenseAuditor.COMP_NAME);
        licenseAuditor.startPeriodicAudit();
        
        initCustomApp();
    }
    
    /**
     * This function initializes the components related to custom application.
     */
    protected void initCustomApp(){
        IComponentManager compMgr = getManager();
        
        ComponentInfo<CustomAppDataManager> customAppDataManagerCompInfo = 
            new ComponentInfo<CustomAppDataManager>(
                    CustomAppDataManager.class, 
                    LifestyleType.SINGLETON_TYPE);
        CustomAppDataManager customAppDataManager = compMgr.getComponent(customAppDataManagerCompInfo);
        
        
        ComponentInfo<ExternalApplicationLoader> externalApplicationLoaderCompInfo =
            new ComponentInfo<ExternalApplicationLoader>(
                    ExternalApplicationLoader.class,
                    LifestyleType.TRANSIENT_TYPE);
        ExternalApplicationLoader externalApplicationLoader = compMgr.getComponent(
                externalApplicationLoaderCompInfo);
        
        try {
            externalApplicationLoader.load();
        } catch (Exception e) {
            throw new RuntimeException("fail to load custom apps", e);
        }
    }
}
