package com.nextlabs.destiny.container.dcc;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.cfg.Environment;

import com.bluejungle.destiny.container.dcc.FileSystemResourceLocatorImpl;
import com.bluejungle.destiny.container.dcc.IDCCComponentConfigResourceLocator;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.TestDCCComponentConfigLocatorImpl;
import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.test.HibernateTestUtils;
import com.bluejungle.framework.datastore.hibernate.test.TestOrientedHibernateRepositoryImpl;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.IDestinyConfigurationManager;

@SuppressWarnings("deprecation")

public final class BaseDCCComponentTestHelper {
    
    private BaseDCCComponentTestHelper() {
    }
    
    
    
    
    
    public static INamedResourceLocator setupResourceLocator(
            IComponentManager compMgr
           , String componentName
           , String rootPath
    ) {
        HashMapConfiguration cConfig = new HashMapConfiguration();
        cConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, rootPath);
        ComponentInfo<FileSystemResourceLocatorImpl> resourceLocatorInfo = 
            new ComponentInfo<FileSystemResourceLocatorImpl>(
                componentName, 
                FileSystemResourceLocatorImpl.class, 
                INamedResourceLocator.class, 
                LifestyleType.SINGLETON_TYPE, 
                cConfig);
        INamedResourceLocator locator = compMgr.getComponent(resourceLocatorInfo);
        assertNotNull(locator);
        return locator;
    }
    
    public static INamedResourceLocator setupResourceLocator(
            String componentName
          , String rootPath
    ) {
        return setupResourceLocator(
                ComponentManagerFactory.getComponentManager()
              , componentName
              , rootPath
        );
    }
    
    
    
    
    
    private static final String DEFAULT_COMPONENT_RELATIVE_CONFIG_PATH = "/WEB-INF/conf/";
    public static final String CONFIG_RESOURCE_LOCATOR = "ConfigResourceLocator";
    
    /**
     * Set up the config resource locator
     */
    public static IDCCComponentConfigResourceLocator setupConfigResourceLocator(
            IComponentManager compMgr) {
        HashMapConfiguration cConfig = new HashMapConfiguration();
        cConfig.setProperty(
                TestDCCComponentConfigLocatorImpl.CONFIG_FOLDER_RELATIVE_PATH_PROPERTY_NAME,
                DEFAULT_COMPONENT_RELATIVE_CONFIG_PATH);
        ComponentInfo<TestDCCComponentConfigLocatorImpl> configLocatorInfo = 
            new ComponentInfo<TestDCCComponentConfigLocatorImpl>(
                CONFIG_RESOURCE_LOCATOR, 
                TestDCCComponentConfigLocatorImpl.class, 
                IDCCComponentConfigResourceLocator.class, 
                LifestyleType.SINGLETON_TYPE, 
                cConfig);
        IDCCComponentConfigResourceLocator locator = compMgr.getComponent(configLocatorInfo);
        assertNotNull(locator);
        return locator;
    }
    
    public static IDCCComponentConfigResourceLocator setupConfigResourceLocator() {
        return setupConfigResourceLocator(ComponentManagerFactory.getComponentManager());
    }
    
    
    
    
    
    public static IHibernateRepository setupDestinyRepository(
            IDestinyConfigurationManager destinyConfiguration
          , DestinyRepository currentRepository
          , IComponentManager compMgr
          , List<Class<?>> dynamicMappings
    ) {
        IDCCComponentConfigResourceLocator configResourceLocator =
            (IDCCComponentConfigResourceLocator) compMgr.getComponent(
                    BaseDCCComponentTestHelper.CONFIG_RESOURCE_LOCATOR);
        
        assertNotNull(configResourceLocator);
        
        Set<? extends IRepositoryConfigurationDO> repositories = destinyConfiguration.getRepositories();
        
        assertNotNull(repositories);
        
        IRepositoryConfigurationDO repositoryConfig = null;
        for(IRepositoryConfigurationDO repository : repositories) {
            if (currentRepository.getName().equals(repository.getName())) {
                repositoryConfig = repository;
            }
        }
        assertNotNull("Can't find repositoryConfig for " + currentRepository, repositoryConfig);
        
        IConnectionPoolConfigurationDO connectionPoolConfiguration =
                repositoryConfig.getConnectionPoolConfiguration();
    
        HashMapConfiguration currentRepositoryConfiguration = new HashMapConfiguration();
        currentRepositoryConfiguration.setProperty(
                TestOrientedHibernateRepositoryImpl.REPOSITORY_ENUM_PARAM,
                currentRepository);
        currentRepositoryConfiguration.setProperty(
                TestOrientedHibernateRepositoryImpl.USER_NAME_PARAM,
                connectionPoolConfiguration.getUserName());
        currentRepositoryConfiguration.setProperty(
                TestOrientedHibernateRepositoryImpl.PASSWORD_PARAM, 
                new ReversibleEncryptor().decrypt(connectionPoolConfiguration.getPassword()));
        currentRepositoryConfiguration.setProperty(
                TestOrientedHibernateRepositoryImpl.CONNECT_STRING_PARAM,
                connectionPoolConfiguration.getJDBCConnectString());
        currentRepositoryConfiguration.setProperty(
                Environment.DIALECT, 
                repositoryConfig.getProperties().getProperty(Environment.DIALECT));
        currentRepositoryConfiguration.setProperty(
                Environment.DRIVER,
                connectionPoolConfiguration.getDriverClassName());
        
        // Create the HibernateDataSource object only if the configs
        InputStream commonConfigFileStream =
                configResourceLocator.getConfigResourceAsStream(
                        DestinyRepository.COMMON_REPOSITORY.getConfigFileName());
        InputStream repositoryConfigFileStream =
                configResourceLocator.getConfigResourceAsStream(currentRepository.getConfigFileName());
        return HibernateTestUtils.createDataSource(
                currentRepository,              // DestinyRepository repositoryEnum, 
                commonConfigFileStream,         // InputStream commonConfigStream, 
                repositoryConfigFileStream,     // InputStream repositoryConfigStream,
                currentRepositoryConfiguration, // IConfiguration partialRepositoryConfiguration,
                dynamicMappings);               // List dynamicMappings
    }
    
    public static IHibernateRepository setupDestinyRepository(
            IDestinyConfigurationManager destinyConfiguration
          , DestinyRepository currentRepository
          , IComponentManager compMgr
    ) {
        return setupDestinyRepository(destinyConfiguration, currentRepository, compMgr, null);
    }
    
    public static IHibernateRepository setupDestinyRepository(
            IDestinyConfigurationManager destinyConfiguration
          , DestinyRepository currentRepository
    ) {
        return setupDestinyRepository(
                destinyConfiguration
              , currentRepository
              , ComponentManagerFactory.getComponentManager()
        );
    }
    
    public static IHibernateRepository setupDestinyRepository(
            DestinyRepository currentRepository
          , IComponentManager compMgr
    ) {
        IDestinyConfigurationManager configMgr = (IDestinyConfigurationManager) compMgr
                .getComponent(IDestinyConfigurationManager.COMP_NAME);
        
        assertNotNull(configMgr);
        
        return setupDestinyRepository(
                configMgr
              , currentRepository
        );
    }
    
    public static IHibernateRepository setupDestinyRepository(
            DestinyRepository currentRepository
    ) {
        return setupDestinyRepository(
              currentRepository
            , ComponentManagerFactory.getComponentManager()
        );
    }
    
}
