/*
 * Created on Dec 6, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.security.IKeyManager;
import com.bluejungle.framework.security.KeyManagerImpl;
import com.bluejungle.framework.sharedcontext.DestinySharedContextLocatorImpl;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.version.IVersion;
import com.bluejungle.versionexception.InvalidVersionException;
import com.bluejungle.versionfactory.VersionFactory;

/**
 * The default container implementation is used by most of the DCC components.
 * In this implementation, the DCC container does not do anything really special
 * for the DCC component. It simply registers the component with DMS, and upon
 * successful registration, instantiates the DCC component container and passes
 * the DCC configuration that was returned by DMS. If the DMS registration
 * fails, the DCC component is not instanciated.
 * 
 * The name and class name of the DCC components are configurable as well
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/DefaultContainerImpl.java#1 $:
 */

public class DefaultContainerImpl implements IDCCContainer, IInitializable, IConfigurable, ILogEnabled, IDisposable, IManagerEnabled {

    private static final String TRUSTSTORE_PROP_NAME = "nextlabs.javax.net.ssl.trustStore";
    private static final String TRUSTSTORE_PASS_PROP_NAME = "nextlabs.javax.net.ssl.trustStorePassword";
    private static final String KEYSTORE_PROP_NAME = "nextlabs.javax.net.ssl.keyStore";
    private static final String KEYSTORE_PASS_PROP_NAME = "nextlabs.javax.net.ssl.keyStorePassword";

    private IComponentManager manager;
    private Log log;
    private IConfiguration config;
    private IDestinySharedContext sharedContext;
    private ServerComponentType compType;
    private String compName;
    private String compTypeDisplayName;
    private Class<? extends IRegisteredDCCComponent> compClassName;
    private URL compLocation;
    private IDCCComponentStarter componentStarter;
    private IVersion componentVersion;
    private Set<String> applicationResources;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.config = null;
        this.compName = null;
        this.compTypeDisplayName = null;
        this.compType = null;
        this.log = null;
        this.sharedContext = null;
        this.componentStarter = null;
        this.componentVersion = null;
        if (applicationResources != null) {
            applicationResources.clear();
        }
        applicationResources = null;
    }

    /**
     * This class is used to allow subclasses to add extra initialization steps.
     * For this class it is a no-op.
     */
    protected void doInit() {
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Returns the component starter
     * 
     * @return the component starter
     */
    protected IDCCComponentStarter getComponentStarter() {
        return this.componentStarter;
    }

    /**
     * Returns the class name to be used for the heartbeat manager.
     * 
     * @return the class name to be used for the heartbeat manager.
     */
    protected Class<? extends IHeartbeatMgr> getHeartbeatMgrClassName() {
        return HeartbeatMgrImpl.class;
    }

    /**
     * Specifies if this container requires the dcc keystore/truststore initialization
     */
    protected boolean requiresKeyManager() {
        return true;
    }
    
    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * Returns the shared context
     * 
     * @return the shared context object
     */
    protected IDestinySharedContext getSharedContext() {
        return this.sharedContext;
    }
    
    private void nullCheck(Object object, String objectName) {
        if(object == null){
            throw initException("The '" + objectName + "' cannot be null for a '" 
                    + (compName != null ? compName: "DCC") 
                    + "' component.");
        }
    }

    private final IllegalArgumentException initException(String message) {
        getLog().fatal(message);
        return new IllegalArgumentException(message);
    }

    /**
     * Initialization method. This method prepares the component registration
     * and initializes various member variables.
     * 
     * @throws
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public final void init() {
        System.setProperty("axis.socketSecureFactory", "com.nextlabs.axis.JSSESocketFactoryWrapper");
        
        initSharedContext();

        /*
         * Intialize the KeyManager. Need to do this in the container, as the
         * keys may be needed before the DCC Component instance is created
         */
        if (requiresKeyManager()) {
            initKeyManager();
        }

        // figures out the name of the DCC component
        compName = config.get(IDCCContainer.COMPONENT_NAME_CONFIG_PARAM);
        nullCheck(compName, "Component Name");

        // figures out the type of the DCC component. The type is passed an as
        // config parameter
        compType = config.get(IDCCContainer.COMPONENT_TYPE_CONFIG_PARAM);
        nullCheck(compType, "Component Type");
        
        compTypeDisplayName = config.get(IDCCContainer.COMPONENT_TYPE_DISPLAY_NAME_CONFIG_PARAM);
        nullCheck(compTypeDisplayName, "Component Type Display Name");

        compClassName = config.get(IDCCContainer.COMPONENT_CLASS_CONFIG_PARAM);
        nullCheck(compType, "Component Classname");

        String location = this.config.get(IDCCContainer.COMPONENT_LOCATION_CONFIG_PARAM);

        try {
            compLocation = new URL(location == null ? "https://localhost/unknown" : location);
        } catch (MalformedURLException e) {
            throw initException("Malformed URL for component: " + this.compName);
        }
        nullCheck(compLocation, "Component Location");

        try {
            this.componentVersion = (new VersionFactory()).getVersion();
        } catch (IOException e) {
            getLog().fatal("Version file read error in component " + this.compName);
            throw new RuntimeException(e);
        } catch (InvalidVersionException e) {
            getLog().fatal("Invalid version in component " + this.compName);
            throw new RuntimeException(e);
        }
        
        this.applicationResources = config.get(IDCCContainer.COMPONENT_RESOUCES_CONFIG_PARAM);

        doInit();
        final Object lockObject = new Object();
        synchronized (lockObject) {
            // If initComponentStarter fails, there is no risk to block on wait
            // since wait did not start yet
            initComponentStarter(lockObject);
            try {
                lockObject.wait();
            } catch (InterruptedException e1) {
                getLog().error("Stuck on the lock object");
            }
            registerWithDMS();
        }
    }

    /**
     * Initializes the shared context member variable.
     */
    protected void initSharedContext() {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();

        // Mostly for JUNIT. In theory nobody should mention this config
        // parameter
        Class<? extends IDestinySharedContextLocator> locatorClassName = this.config.get(SHARED_CTX_LOCATOR_CLASSNAME_CONFIG_PARAM);
        if (locatorClassName == null) {
            locatorClassName = DestinySharedContextLocatorImpl.class;
        }

        // Stores the shared context locator as a singleton. This way, everybody
        // else can retrieve the locator (hence the context) down the road
        // through the locator object
        ComponentInfo<IDestinySharedContextLocator> info = 
            new ComponentInfo<IDestinySharedContextLocator>(
                IDestinySharedContextLocator.COMP_NAME, 
                locatorClassName, 
                IDestinySharedContextLocator.class, 
                LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = manager.getComponent(info);
        this.sharedContext = locator.getSharedContext();
    }

    /**
     * Initializes the component starter thread.
     * 
     * @param lock
     *            object that should be notified once the component started is
     *            ready to wait
     * 
     */
    protected void initComponentStarter(Object lock) {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration compStarterConfig = new HashMapConfiguration();
        compStarterConfig.setProperty(IDCCComponentStarter.DCC_COMPONENT_CLASSNAME_CONFIG_PARAM, this.compClassName);
        compStarterConfig.setProperty(IDCCComponentStarter.DCC_COMPONENT_NAME_CONFIG_PARAM, this.compName);
        compStarterConfig.setProperty(IDCCComponentStarter.HEARTBEAT_MGR_CLASSNAME_CONFIG_PARAM, getHeartbeatMgrClassName());
        compStarterConfig.setProperty(IDCCComponentStarter.DCC_COMPONENT_TYPE_CONFIG_PARAM, this.compType);
        compStarterConfig.setProperty(IDCCComponentStarter.LOCK_OBJECT_CONFIG_PARAM, lock);
        // Pass on the configuration for the component itself
        compStarterConfig.setProperty(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME, 
                getConfiguration().get(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME));
        ComponentInfo<IDCCComponentStarter> compStarterInfo = 
            new ComponentInfo<IDCCComponentStarter>(
                IDCCComponentStarter.COMP_NAME, 
                DCCComponentStarterImpl.class, 
                IDCCComponentStarter.class, 
                LifestyleType.SINGLETON_TYPE, 
                compStarterConfig);
        this.componentStarter = manager.getComponent(compStarterInfo);
    }

    /**
     * This function performs the DMS registration for the DCC component. It may
     * take some time for the registration to be performed, so the registration
     * requests is passed to the registration manager (in the shared context),
     * along with a callback to the component starter that is used when the
     * registration completes (successfully or not).
     */
    protected void registerWithDMS() {
        // Creates the registration information
        IDCCRegistrationInfo registerInfo = this.sharedContext.getRegistrationManager().getDefaultRegistrationInfo();
        registerInfo.setComponentName(this.compName);
        registerInfo.setComponentType(this.compType);
        registerInfo.setComponentTypeDisplayName(this.compTypeDisplayName);
        registerInfo.setComponentURL(this.compLocation);
        registerInfo.setComponentVersion(this.componentVersion);
        registerInfo.setApplicationResources(applicationResources);
        
        if (log.isDebugEnabled()) {
            log.debug("Passing registration info for component " + this.compName + " of type " + this.compType);
        }

        // Pass on the registration info to the registration manager
        this.sharedContext.getRegistrationManager().registerWithDMS(registerInfo, this.componentStarter);
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration configuration) {
        this.config = configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        this.manager = newMgr;
    }

    /**
     * Intialize the KeyManager.
     * 
     */
    private void initKeyManager() {
        // Initialize the KeyManager
        HashMapConfiguration keyManagerConfiguration = new HashMapConfiguration();
        Set<KeyManagerImpl.KeystoreFileInfo> keyStoreFileInfo = new HashSet<KeyManagerImpl.KeystoreFileInfo>();

        IConfiguration contextConfiguration = config.get(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME);

        // On JBoss the keystore/truststore information might be set via a separate config file and won't be a
        // system property. Check for this and make it a system property if it isn't one already
        String keystoreLocation = System.getProperty(KEYSTORE_PROP_NAME);
        if (keystoreLocation == null) {
            keystoreLocation = (String)contextConfiguration.get(KEYSTORE_PROP_NAME);
            System.setProperty(KEYSTORE_PROP_NAME, keystoreLocation);
        }

        String keystorePassword = System.getProperty(KEYSTORE_PASS_PROP_NAME);
        if (keystorePassword == null) {
            keystorePassword = (String)contextConfiguration.get(KEYSTORE_PASS_PROP_NAME);
            System.setProperty(KEYSTORE_PASS_PROP_NAME, keystorePassword);
        }

        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("keystore", keystoreLocation, "jks", keystorePassword));

        String truststoreLocation = System.getProperty(TRUSTSTORE_PROP_NAME);
        if (truststoreLocation == null) {
            truststoreLocation = (String)contextConfiguration.get(TRUSTSTORE_PROP_NAME);
            System.setProperty(TRUSTSTORE_PROP_NAME, truststoreLocation);
        }

        String truststorePassword = System.getProperty(TRUSTSTORE_PASS_PROP_NAME);
        if (truststorePassword == null) {
            truststorePassword = (String)contextConfiguration.get(TRUSTSTORE_PASS_PROP_NAME);
            System.setProperty(TRUSTSTORE_PASS_PROP_NAME, truststorePassword);
        }

        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("truststore", truststoreLocation, "jks", truststorePassword));
        keyManagerConfiguration.setProperty(KeyManagerImpl.KEYSTORE_FILE_INFO_PROPERTY_NAME, keyStoreFileInfo);
        ComponentInfo<IKeyManager> keyManagerComponentInfo = 
            new ComponentInfo<IKeyManager>(
                IKeyManager.COMPONENT_NAME, 
                KeyManagerImpl.class, 
                IKeyManager.class, 
                LifestyleType.SINGLETON_TYPE, 
                keyManagerConfiguration);

        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        manager.registerComponent(keyManagerComponentInfo, true);
    }
}
