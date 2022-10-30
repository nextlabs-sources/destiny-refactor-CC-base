package com.nextlabs.destiny.container.shared.utils;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager;
import com.bluejungle.destiny.container.shared.securesession.hibernate.HibernateSecureSessionManager;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

public final class DCCComponentHelper {

    private DCCComponentHelper() {
    }
    
    public static void initSecurityComponents(IComponentManager compMgr, Log log) {
        initApplicationUserManager(compMgr, log);
        initSecureSessionManager(compMgr, log);
    }
    
    public static void initApplicationUserManager(IComponentManager compMgr, Log log) {
        log.debug("Initializing the authentication manager");
        IDestinyConfigurationStore confStore = compMgr.getComponent(
                DestinyConfigurationStoreImpl.COMP_INFO);

        // Get the management repository:
        IHibernateRepository mgmtRep = (IHibernateRepository) compMgr.getComponent(
                DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        IApplicationUserConfigurationDO appUserConfig = confStore.retrieveAuthConfig();
        HashMapConfiguration applicationUserManagerFactoryConfig = new HashMapConfiguration();
        applicationUserManagerFactoryConfig.setProperty(
                IApplicationUserManagerFactory.APPLICATION_USER_CONFIGURATION, appUserConfig);
        applicationUserManagerFactoryConfig.setProperty(
                IApplicationUserManagerFactory.MANAGEMENT_REPOSITORY, mgmtRep);
        IApplicationUserManagerFactory appUserManager = compMgr.getComponent(
                ApplicationUserManagerFactoryImpl.class, 
                applicationUserManagerFactoryConfig
        );
        log.debug("Initialized the authentication manager");
    }
    
    public static void initSecureSessionManager(IComponentManager compMgr, Log log) {
      //Initializes the secure session manager
        log.debug("Initializing the secure session manager");
        HashMapConfiguration secureSessionConfig = new HashMapConfiguration();
        secureSessionConfig.setProperty(
                HibernateSecureSessionManager.SESSION_TIMEOUT_CONFIG_PROPERTY_NAME,
                HibernateSecureSessionManager.DEFAULT_SESSION_TIMEOUT);
        secureSessionConfig.setProperty(
                HibernateSecureSessionManager.SESSION_CLEANUP_TIME_INTERVAL_PROPERTY_NAME,
                HibernateSecureSessionManager.DEFAULT_SESSION_CLEANUP_TIME_INTERVAL);
        ComponentInfo<HibernateSecureSessionManager> compInfo = 
            new ComponentInfo<HibernateSecureSessionManager>(
                ISecureSessionManager.COMPONENT_NAME, 
                HibernateSecureSessionManager.class, 
                ISecureSessionManager.class, 
                LifestyleType.SINGLETON_TYPE, 
                secureSessionConfig);
        compMgr.registerComponent(compInfo, true);
        log.debug("Initialized the secure session manager");
    }
}
