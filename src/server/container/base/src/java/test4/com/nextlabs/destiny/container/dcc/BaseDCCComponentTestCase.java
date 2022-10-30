package com.nextlabs.destiny.container.dcc;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.IDCCComponentConfigResourceLocator;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.sharedcontext.DestinySharedContextLocatorImpl;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.IDestinyConfigurationManager;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.DestinyConfigurationManagerImpl;
import com.nextlabs.framework.test.BaseDestinyTestCase;

public abstract class BaseDCCComponentTestCase extends BaseDestinyTestCase{

    private static final String BUILD_ROOT_PROPERTY_KEY = "build.root.dir";
    protected static final String BUILD_ROOT_DIR = System.getProperty(BUILD_ROOT_PROPERTY_KEY);

    protected static IComponentManager compMgr;
    
    
    private static final String DEFAULT_COMPONENT_RELATIVE_HOME_PATH = "/server/container/";
    
    
    protected final Log LOG = LogFactory.getLog(this.getClass());
    
    
    @BeforeClass
    public static void checkBuildParamater() {
        assertNotNull("Please define system property \"" + BUILD_ROOT_PROPERTY_KEY + "\"", BUILD_ROOT_DIR);
        
    }
    
    @BeforeClass
    public static void staticInit() {
        compMgr = ComponentManagerFactory.getComponentManager();
    }
    
    /**
     * Retrieve the name of the component. Used to locate the component home
     * path in the Destiny install
     * 
     * @return the name of the component
     */
    protected abstract String getComponentName();
    
    
    protected void initAll() {
        ComponentInfo<IDestinySharedContextLocator> locatorInfo =
            new ComponentInfo<IDestinySharedContextLocator>
                (IDestinySharedContextLocator.COMP_NAME,
                DestinySharedContextLocatorImpl.class,
                IDestinySharedContextLocator.class, 
                LifestyleType.TRANSIENT_TYPE);
        compMgr.registerComponent(locatorInfo, false);
        
        setupResourceLocator();
        setupConfigResourceLocator();
        readConfigurationFile();
        setupDataSources();
    }
    
    /**
     * Set up the resource locator
     */
    protected void setupResourceLocator() {
        String installPath = new File(BUILD_ROOT_DIR).getAbsolutePath();
        
        BaseDCCComponentTestHelper.setupResourceLocator(compMgr, 
                DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR, installPath + "/run/server");
        
        BaseDCCComponentTestHelper.setupResourceLocator(compMgr, 
                DCCResourceLocators.WEB_APP_RESOURCE_LOCATOR, installPath + DEFAULT_COMPONENT_RELATIVE_HOME_PATH + getComponentName());
    }

    /**
     * Set up the config resource locator
     */
    protected void setupConfigResourceLocator() {
        // must define the webapp resource locator first
        BaseDCCComponentTestHelper.setupConfigResourceLocator(compMgr);
    }

    protected IDestinyConfigurationManager readConfigurationFile() {
        ComponentInfo<IDestinyConfigurationManager> configMgrCompInfo = 
            new ComponentInfo<IDestinyConfigurationManager>(
                IDestinyConfigurationManager.COMP_NAME, 
                DestinyConfigurationManagerImpl.class, 
                IDestinyConfigurationManager.class, 
                LifestyleType.SINGLETON_TYPE);
        
        IDestinyConfigurationManager config = compMgr.getComponent(configMgrCompInfo);
        assertNotNull(config);
        return config;
    }
    
    /**
     * Retrieve the Set of DestinyRepositories needed for this DCC component.
     * 
     * @return the Set of DestinyRepositories needed for this DCC component.
     */
    protected abstract Set<DestinyRepository> getDataRepositories();
    
    /**
     * Returns the list of dynamic mappings. By default, there are none.
     * 
     * @return a list of class objects to be added in the mapping of the session
     *         factory.
     */
    protected List<Class<?>> getDataSourceDynamicMappings() {
        return null;
    }
    
    /**
     * Set up the Hibernate Data Sources
     */
    protected void setupDataSources() {
        // Setup the Hibernate Data Sources
        Set<DestinyRepository> repositories = getDataRepositories();
        if(repositories == null || repositories.isEmpty()){
            LOG.warn("ServerComponent '" + getComponentName() + "' doesn't define any data sources");
            return;
        }
        
        
        IDestinyConfigurationManager destinyConfiguration = readConfigurationFile();
        
        @SuppressWarnings("deprecation")
        IDCCComponentConfigResourceLocator configResourceLocator =
            (IDCCComponentConfigResourceLocator) compMgr.getComponent(
                    BaseDCCComponentTestHelper.CONFIG_RESOURCE_LOCATOR);
        
        assertNotNull(configResourceLocator);

        for (DestinyRepository currentRepository : repositories) {
            BaseDCCComponentTestHelper.setupDestinyRepository(
                    destinyConfiguration
                  , currentRepository
                  , compMgr
                  , getDataSourceDynamicMappings()
            );
        }
    }
    
    
}
