package com.nextlabs.destiny.container.dkms;

import static com.nextlabs.destiny.container.dkms.DKMSConstants.*;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.sf.hibernate.cfg.Environment;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.WebRelativeFolders;
import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.PropertyList;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.datastore.hibernate.AbstractConfigurableHibernateRepositoryImpl;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.PoolBackedHibernateRepositoryImpl;
import com.bluejungle.framework.environment.IResourceLocator;
import com.bluejungle.framework.sharedcontext.DestinySharedContextLocatorImpl;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.framework.utils.PropertiesUtils;
import com.bluejungle.framework.utils.PropertiesUtils.AbstractKeyMatcher;

public class KeyManagementRepository {
    
    public static final String CONNECTION_POOL_NAME = "keymanagement.connection.pool";
    
    public static final ComponentInfo<PoolBackedHibernateRepositoryImpl> COMP_INFO = 
        new ComponentInfo<PoolBackedHibernateRepositoryImpl>(
                "keymanagement.repository"
              , PoolBackedHibernateRepositoryImpl.class
              , IHibernateRepository.class
              , LifestyleType.SINGLETON_TYPE
    );
    
    /*
     * PKEY mean property key in configuration.xml
     */
    /*
     * the following PKEY are required 
     */
    private static final String PKEY_DIALECT  = DATABASE_HIBERNATE_CONFIG_PROERTY_PREFIX + "dialect";
    private static final String PKEY_USERNAME = DATABASE_CONFIG_PROPERTY_PREFIX + "username";
    private static final String PKEY_PASSWORD = DATABASE_CONFIG_PROPERTY_PREFIX + "password";
    private static final String PKEY_URL      = DATABASE_CONFIG_PROPERTY_PREFIX + "url";
    private static final String PKEY_DRIVER   = DATABASE_CONFIG_PROPERTY_PREFIX + "driver";
    
    /*
     * the following PKEY are optional 
     */
    private static final String PKEY_MAX_POOL_SIZE   = DATABASE_CONFIG_PROPERTY_PREFIX + "maxPoolSize";
    private static final int DEFAULT_MAX_POOL_SIZE   = 10;
    
    
    private static final Set<String> PREDEFINIED_PKEYS;
    static {
        PREDEFINIED_PKEYS = new HashSet<String>();
        PREDEFINIED_PKEYS.add(PKEY_DIALECT);
        PREDEFINIED_PKEYS.add(PKEY_USERNAME);
        PREDEFINIED_PKEYS.add(PKEY_PASSWORD);
        PREDEFINIED_PKEYS.add(PKEY_URL);
        PREDEFINIED_PKEYS.add(PKEY_DRIVER);
        PREDEFINIED_PKEYS.add(PKEY_MAX_POOL_SIZE);
    }
    
    /**
     * @param compMgr
     * @return
     */
    public static IHibernateRepository create(IComponentManager compMgr,
            Properties componentConfig) {
        createDestinySharedContextLocator(compMgr);
        
        HashMapConfiguration dsConfig = new HashMapConfiguration();
        
        @SuppressWarnings("deprecation")
        IResourceLocator webAppFileLocator = (IResourceLocator) compMgr.getComponent(
                DCCResourceLocators.WEB_APP_RESOURCE_LOCATOR);
       
        String dataSrcConfigFile_common = WebRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(
                DestinyRepository.COMMON_REPOSITORY.getConfigFileName());
        dsConfig.setProperty(AbstractConfigurableHibernateRepositoryImpl.COMMON_CONFIG, 
                webAppFileLocator.getResourceAsStream(dataSrcConfigFile_common));
        
        String dataSrcConfigFile = WebRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(
                COMP_INFO.getName() + ".xml");
        dsConfig.setProperty(AbstractConfigurableHibernateRepositoryImpl.REPOSITORY_CONFIG, 
                webAppFileLocator.getResourceAsStream(dataSrcConfigFile));
        
        
        RepositoryConfigurationDO ds = new RepositoryConfigurationDO();
        ds.setName(COMP_INFO.getName());
        ds.setConnectionPoolName(CONNECTION_POOL_NAME);
        ds.setConnectionPoolConfiguration(createPoolConfig(componentConfig));
        
        Properties extraProperties = createExtraRepositoryConfig(componentConfig);
        extraProperties.put(Environment.HBM2DDL_AUTO, "update");
        ds.setProperties(new PropertyList(extraProperties));
        
        dsConfig.setProperty(PoolBackedHibernateRepositoryImpl.REPOSITORY_DEFINITION, ds);
        
        return compMgr.getComponent(COMP_INFO, dsConfig);
    }
    
    private static IDestinySharedContextLocator createDestinySharedContextLocator(IComponentManager compMgr) {
        ComponentInfo<IDestinySharedContextLocator> locatorInfo =
                new ComponentInfo<IDestinySharedContextLocator>
                    (IDestinySharedContextLocator.COMP_NAME,
                    DestinySharedContextLocatorImpl.class,
                    IDestinySharedContextLocator.class, 
                    LifestyleType.TRANSIENT_TYPE);
       return compMgr.getComponent(locatorInfo);
    }
    
    private static String get(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null) {
            throw new NullPointerException(key + " is required");
        }
        return v;
    }
    
    private static IConnectionPoolConfigurationDO createPoolConfig(Properties componentConfig) {
        ConnectionPoolConfigurationDO connectionPoolConfig = new ConnectionPoolConfigurationDO();
        connectionPoolConfig.setDriverClassName(get(componentConfig, PKEY_DRIVER));
        connectionPoolConfig.setIsPasswordDecoded(true);
        connectionPoolConfig.setJDBCConnectString(get(componentConfig, PKEY_URL));
        connectionPoolConfig.setMaxPoolSize(
                PropertiesUtils.get(componentConfig, PKEY_MAX_POOL_SIZE, DEFAULT_MAX_POOL_SIZE));
        connectionPoolConfig.setName(CONNECTION_POOL_NAME);
        
        String encodedPassword = get(componentConfig, PKEY_PASSWORD);
        ReversibleEncryptor decoder = new ReversibleEncryptor();
        connectionPoolConfig.setPassword(decoder.decrypt(encodedPassword));
        connectionPoolConfig.setUserName(get(componentConfig, PKEY_USERNAME));
        
        Properties connectionPoolExtraProperties = PropertiesUtils.filter(componentConfig, new AbstractKeyMatcher() {
            @Override
            public boolean isMatch(Object key) {
                String skey = (String)key;
                
                if(PREDEFINIED_PKEYS.contains(skey)){
                    return false;
                }
                
                return skey.startsWith(DATABASE_CONFIG_PROPERTY_PREFIX) 
                    && !skey.startsWith(DATABASE_HIBERNATE_CONFIG_PROERTY_PREFIX);
            }
            
        });
        
        connectionPoolConfig.setProperties(new PropertyList(connectionPoolExtraProperties));
        return connectionPoolConfig;
    }
    
    private static Properties createExtraRepositoryConfig(Properties componentConfig){
        Properties extraProperties = PropertiesUtils.filter(componentConfig, new AbstractKeyMatcher() {
            private final int prefixIndex = DATABASE_CONFIG_PROPERTY_PREFIX.length();
            
            @Override
            public boolean isMatch(Object key) {
                String skey = (String)key;
                return skey.startsWith(DATABASE_HIBERNATE_CONFIG_PROERTY_PREFIX);
            }
            
            @Override
            public Object filterKey(Object key) {
                String skey = (String)key;
                return skey.substring(prefixIndex);
            }
        });
        return extraProperties;
    }
    
}
