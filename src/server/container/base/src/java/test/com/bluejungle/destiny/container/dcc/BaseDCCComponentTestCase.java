/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.datastore.hibernate.test.HibernateTestUtils;
import com.bluejungle.framework.datastore.hibernate.test.TestOrientedHibernateRepositoryImpl;
import com.bluejungle.framework.test.BaseDestinyTestCase;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.cfg.Environment;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A Base JUnit Test Case for DCC components. Currently, it merely sets up
 * config file locator component and data sources
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/test/BaseDCCComponentTestCase.java#1 $
 */
public abstract class BaseDCCComponentTestCase extends BaseDestinyTestCase {

    private static final String DESTINY_INSTALL_PATH_PROPERTY_NAME = "build.root.dir";
    private static final String DEFAULT_DESTINY_INSTALL_PATH = "C:/builds/destiny";
    private static final String DEFAULT_COMPONENT_RELATIVE_HOME_PATH = "/server/container/";
    private static final String DEFAULT_COMPONENT_RELATIVE_CONFIG_PATH = "/WEB-INF/conf/";

    private final boolean reinitBetweenTests;

    private ReversibleEncryptor decryptor = new ReversibleEncryptor();
    
    /*
     * FIX ME - Needs to be moved into product
     */
    public static final String CONFIG_RESOURCE_LOCATOR = "ConfigResourceLocator";

    private File installPath;

    /**
     * Constructor
     */
    public BaseDCCComponentTestCase() {
        this(true);
    }

    /**
     * Constructor
     */
    public BaseDCCComponentTestCase(boolean reinitBetweenTests) {
        this.reinitBetweenTests = reinitBetweenTests;
        initializeIfRequired();
    }

    /**
     * Constructor for BaseDCCComponentTestCase.
     * 
     * @param testName -
     *            the name of the test
     */
    public BaseDCCComponentTestCase(String testName) {
        this(testName, true);
    }

    /**
     * Constructor for BaseDCCComponentTestCase.
     * 
     * @param testName -
     *            the name of the test
     */
    public BaseDCCComponentTestCase(String testName, boolean reinitBetweenTests) {
        super(testName);
        this.reinitBetweenTests = reinitBetweenTests;
        initializeIfRequired();
    }

    private void initializeIfRequired() {
        // If we're not reinitializing between tests,
        // we should initialize in the constructor.
        if (!reinitBetweenTests) {
            ComponentManagerFactory.getComponentManager().shutdown();
            setupResourceLocator();
            setupConfigResourceLocator();
            readConfigurationFile();
            setupDataSources();
        }
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        if (reinitBetweenTests) {
            super.setUp();

            setupResourceLocator();
            setupConfigResourceLocator();
            readConfigurationFile();
            setupDataSources();
        }
    }

    protected void tearDown() throws Exception {
        if (reinitBetweenTests) {
            super.tearDown();
        }
    }

    /**
     * Set up the resource locator
     */
    protected void setupResourceLocator() {
        /*
         * Set up component resource locator. Use the WebAppResourceLocator
         * constant, because this is what is generally used. Should it be
         * renamed to Component_Resource_Locator? - FIX ME
         */
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        String installPath = getInstallPath().getAbsolutePath();
        HashMapConfiguration serverLocatorConfig = new HashMapConfiguration();
        serverLocatorConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, installPath);
        ComponentInfo<FileSystemResourceLocatorImpl> serverLocatorInfo = 
        	new ComponentInfo<FileSystemResourceLocatorImpl>(
        		DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR, 
        		FileSystemResourceLocatorImpl.class, 
        		INamedResourceLocator.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		serverLocatorConfig);
        compMgr.registerComponent(serverLocatorInfo, false);
        
        String componentRootDir = installPath + getComponentRelativeHomePath() + getComponentName();
        HashMapConfiguration resourceLocatorConfig = new HashMapConfiguration();
        resourceLocatorConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, componentRootDir);
        ComponentInfo<FileSystemResourceLocatorImpl> resourceLocatorInfo = 
        	new ComponentInfo<FileSystemResourceLocatorImpl>(
        		DCCResourceLocators.WEB_APP_RESOURCE_LOCATOR, 
        		FileSystemResourceLocatorImpl.class, 
        		INamedResourceLocator.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		resourceLocatorConfig);
        compMgr.getComponent(resourceLocatorInfo);
    }

    /**
     * Set up the config resource locator
     */
    protected void setupConfigResourceLocator() {
        /*
         * Set up config file locator
         */
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration configLocatorConfig = new HashMapConfiguration();
        configLocatorConfig.setProperty(
				TestDCCComponentConfigLocatorImpl.CONFIG_FOLDER_RELATIVE_PATH_PROPERTY_NAME,
				getComponentConfigRelativePath());
        ComponentInfo<TestDCCComponentConfigLocatorImpl> configLocatorInfo = 
        	new ComponentInfo<TestDCCComponentConfigLocatorImpl>(
        		CONFIG_RESOURCE_LOCATOR, 
        		TestDCCComponentConfigLocatorImpl.class, 
        		IDCCComponentConfigResourceLocator.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		configLocatorConfig);
        compMgr.getComponent(configLocatorInfo);
    }

    protected void readConfigurationFile() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        compMgr.getComponent(ITestDestinyConfiguration.COMP_INFO);
    }
    
    /**
     * Set up the Hibernate Data Sources
     */
    protected void setupDataSources() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ITestDestinyConfiguration destinyConfiguration =
				compMgr.getComponent(ITestDestinyConfiguration.COMP_INFO);
		IDCCComponentConfigResourceLocator configResourceLocator =
				(IDCCComponentConfigResourceLocator) compMgr.getComponent(CONFIG_RESOURCE_LOCATOR);

        // Setup the Hibernate Data Sources
        Set<DestinyRepository> repositories = getDataRepositories();
		if (repositories != null) {
			for (DestinyRepository currentRepository : repositories) {
                IRepositoryConfigurationDO repositoryConfig =
						destinyConfiguration.getRepositoryConfiguration(currentRepository);
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
						TestOrientedHibernateRepositoryImpl.PASSWORD_PARAM, this.decryptor
								.decrypt(connectionPoolConfiguration.getPassword()));
				currentRepositoryConfiguration.setProperty(
						TestOrientedHibernateRepositoryImpl.CONNECT_STRING_PARAM,
						connectionPoolConfiguration.getJDBCConnectString());
				currentRepositoryConfiguration.setProperty(Environment.DIALECT, repositoryConfig
						.getProperties().getProperty(Environment.DIALECT));
				currentRepositoryConfiguration.setProperty(Environment.DRIVER,
						connectionPoolConfiguration.getDriverClassName());
                
                // Create the HibernateDataSource object only if the configs
                InputStream commonConfigFileStream =
						configResourceLocator.getConfigResourceAsStream(
								DestinyRepository.COMMON_REPOSITORY.getConfigFileName());
				InputStream repositoryConfigFileStream =
						configResourceLocator.getConfigResourceAsStream(currentRepository.getConfigFileName());
				List<Class<?>> dynamicMappings = setupDataSourceDynamicMappings();
				HibernateTestUtils.createDataSource(
						currentRepository,				// DestinyRepository repositoryEnum, 
						commonConfigFileStream,			// InputStream commonConfigStream, 
						repositoryConfigFileStream,		// InputStream repositoryConfigStream,
						currentRepositoryConfiguration,	// IConfiguration partialRepositoryConfiguration,
						dynamicMappings);				// List dynamicMappings
			}
        }
    }

    /**
     * Returns the list of dynamic mappings. By default, there are none.
     * 
     * @return a list of class objects to be added in the mapping of the session
     *         factory.
     */
    protected List<Class<?>> setupDataSourceDynamicMappings() {
        return null;
    }

    /**
     * Retrieve the name of the component. Used to locate the component home
     * path in the Destiny install
     * 
     * @return the name of the component
     */
    protected abstract String getComponentName();

    /**
     * Retrieve the relative path to DCC components from the Desinty root
     * folder. By default, this is "/server/container/"
     * 
     * @return the relative path to DCC components from the Desinty root folder
     */
    protected String getComponentRelativeHomePath() {
        return DEFAULT_COMPONENT_RELATIVE_HOME_PATH;
    }

    /**
     * Retrieve the component config path relative to the component home path.
     * By default, this is "/WEB-INF/conf/"
     * 
     * @return
     */
    protected String getComponentConfigRelativePath() {
        return DEFAULT_COMPONENT_RELATIVE_CONFIG_PATH;
    }

    /**
     * Retrieve the Destiny install path. By default, this is
     * "c:\builds\destiny". However, it can be configured through the JVM system
     * property, "destiny.install.path"
     * 
     * @return the Destiny install path
     */
    private File getInstallPath() {
        if (installPath == null) {
			String destinyInstallPath = System.getProperty(DESTINY_INSTALL_PATH_PROPERTY_NAME, DEFAULT_DESTINY_INSTALL_PATH);

            this.installPath = new File(destinyInstallPath);
            if (!installPath.exists()) {
                throw new IllegalArgumentException("Install path specified by system property, \"" + DESTINY_INSTALL_PATH_PROPERTY_NAME + "\", does not exists.");
            }
        }

        return installPath;
    }

    /**
     * Retrieve the Set of DestinyRepositories needed for this DCC component.
     * 
     * @return the Set of DestinyRepositories needed for this DCC component.
     */
    protected abstract Set<DestinyRepository> getDataRepositories();

    protected static <T> TestSuite makeSingleInitializationSuite(Class<T> testClass) {
        if (testClass == null) {
            return new TestSuite("<null>");
        }

        TestSuite res = new TestSuite(testClass.getName());

        // Find the constructor that we are going to use for the tests
        Constructor<? extends T>[] allConstructors = (Constructor<? extends T>[])testClass.getConstructors();
        Constructor<? extends T> theConstructor = null;
        boolean needsName = false;
        for (int i = 0; theConstructor == null && i != allConstructors.length; i++) {
            if (Modifier.isPublic(allConstructors[i].getModifiers())) {
                Class<?>[] params = allConstructors[i].getParameterTypes();
                if (params.length == 0) {
                    theConstructor = allConstructors[i];
                } else if (params.length == 1 && params[0] == String.class) {
                    theConstructor = allConstructors[i];
                    needsName = true;
                }
            }
        }
        if (theConstructor == null) {
            return new TestSuite("<no constructor>: " + testClass.getName());
        }

        final Object[] EMPTY = new Object[0];
        // This class is used to defer throwing of exceptions in constructors
        class DeferredThrow extends TestCase {

            private Exception ex;

            public DeferredThrow(String name, Exception ex) {
                super(name);
                this.ex = ex;
            }

            public void runTest() throws Exception {
                Throwable cause = ex.getCause();
                if (cause instanceof Exception) {
                    throw (Exception) cause;
                } else {
                    throw ex;
                }
            }
        }

        Method setUp = null;
        Method tearDown = null;
        try {
            setUp = testClass.getDeclaredMethod("setUp");
            setUp.setAccessible(true);
        } catch (Exception ignore) {
        }
        try {
            tearDown = testClass.getDeclaredMethod("tearDown");
            tearDown.setAccessible(true);
        } catch (Exception ignore) {
        }

        // Build the suite
        try {
            final Object theInstance = theConstructor.newInstance(needsName ? new Object[] { testClass.getName() } : EMPTY);
            final Method theSetUp = setUp;
            final Method thetearDown = tearDown;
            // Find the methods that we are going to add as tests
            Method[] allMethods = testClass.getMethods();
            for (int i = 0; i != allMethods.length; i++) {
                if (Modifier.isPublic(allMethods[i].getModifiers()) && allMethods[i].getParameterTypes().length == 0 && allMethods[i].getName().startsWith("test")) {
                    final Method toCall = allMethods[i];
                    res.addTest(new TestCase(toCall.getName()) {

                        public void setUp() throws Exception {
                            if (theSetUp != null) {
                                theSetUp.invoke(theInstance);
                            }
                        }

                        public void runTest() throws Exception {
                            toCall.invoke(theInstance);
                        }

                        public void tearDown() throws Exception {
                            if (thetearDown != null) {
                                thetearDown.invoke(theInstance);
                            }
                        }
                    });
                }
            }
        } catch (final IllegalArgumentException e) {
            res.addTest(new DeferredThrow("<IllegalArgumentException> " + testClass.getName(), e));
        } catch (final InstantiationException e) {
            res.addTest(new DeferredThrow("<InstantiationException> " + testClass.getName(), e));
        } catch (final IllegalAccessException e) {
            res.addTest(new DeferredThrow("<IllegalAccessException> " + testClass.getName(), e));
        } catch (final InvocationTargetException e) {
            res.addTest(new DeferredThrow("<InvocationTargetException> " + testClass.getName(), e));
        }
        return res;
    }
}
