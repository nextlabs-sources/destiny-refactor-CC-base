/*
 * Created on Oct 20, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dcc;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.plugin.DCCServerPluginManager;
import com.bluejungle.destiny.container.dcc.plugin.IDCCServerPluginManager;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlerConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.AbstractConfigurableHibernateRepositoryImpl;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.PoolBackedHibernateRepositoryImpl;
import com.bluejungle.framework.environment.IResourceLocator;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.framework.messaging.IMessageHandler;
import com.nextlabs.framework.messaging.IMessageHandlerManager;
import com.nextlabs.framework.messaging.MessagingException;
import com.nextlabs.framework.messaging.impl.MapMessageHandlerConfig;
import com.nextlabs.framework.messaging.impl.MessageHandlerManagerImpl;

/**
 * This is the base DCC component class. Its main purpose is to contain utility
 * functions that other DCC components can reuse. It is not mandatory for a DCC
 * component to inherit from this class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/BaseDCCComponentImpl.java#3 $
 */
public abstract class BaseDCCComponentImpl implements IRegisteredDCCComponent, IInitializable,
                                                      IConfigurable, ILogEnabled, IDisposable, IManagerEnabled {
    
    private String componentName;
    private ServerComponentType componentType;
    private IComponentManager manager;
    protected Log log;
    private IConfiguration config;
    protected IDestinySharedContext sharedContext;
    private IDCCServerPluginManager serverPluginManager;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.config = null;
        this.sharedContext = null;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return this.componentType;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentName()
     */
    public String getComponentName() {
        return this.componentName;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {

        //Stores the shared context
        IComponentManager compMgr = getManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);
        this.sharedContext = locator.getSharedContext();

        //Sets the DCC container name
        setComponentName(getConfiguration().get(IDCCContainer.COMPONENT_NAME_CONFIG_PARAM));

        //Shares the main component configuration for all other components in
        // the system
        ComponentInfo<HashMapConfiguration> mainCompInfo = 
            new ComponentInfo<HashMapConfiguration>(
        	IDCCContainer.MAIN_COMPONENT_CONFIG_COMP_NAME, 
        	HashMapConfiguration.class, 
        	IConfiguration.class, 
        	LifestyleType.SINGLETON_TYPE);
        HashMapConfiguration mainCompConfig = compMgr.getComponent(mainCompInfo);
        IConfiguration config = getConfiguration();
        for (String propName : config.propertySet()) {
            mainCompConfig.setProperty(propName, config.get(propName));
	}

        // Obtain a handle to the server-root-resource locator:
        INamedResourceLocator serverResourceLocator = (INamedResourceLocator) compMgr.getComponent(
            DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);

        // Initialize the certificates-dir resource locator:
        String securityDirRelativePath = ServerRelativeFolders.CERTIFICATES_FOLDER.getPath();
        String securityDirFullPath = serverResourceLocator.getFullyQualifiedName(securityDirRelativePath);
        HashMapConfiguration securityLocatorConfig = new HashMapConfiguration();
        securityLocatorConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, securityDirFullPath);
        ComponentInfo<FileSystemResourceLocatorImpl> securityLocatorInfo = 
            new ComponentInfo<FileSystemResourceLocatorImpl>(
        	DCCResourceLocators.SECURITY_RESOURCE_LOCATOR, 
        	FileSystemResourceLocatorImpl.class, 
        	INamedResourceLocator.class, 
        	LifestyleType.SINGLETON_TYPE, 
        	securityLocatorConfig);
        compMgr.getComponent(securityLocatorInfo);

        // Initializes all the data sources:
        IResourceLocator webAppFileLocator = (IResourceLocator) compMgr.getComponent(
            DCCResourceLocators.WEB_APP_RESOURCE_LOCATOR);
	IDCCRegistrationStatus regStatus = (IDCCRegistrationStatus) this.getConfiguration().get(
            IRegisteredDCCComponent.DMS_REGISTRATION_STATUS_CONFIG_PARAM);

        RepositoryConfigurationList repositoryList = regStatus == null ? null : regStatus.getRepositoryConfigurations();
        if (repositoryList != null) {
            Set<? extends IRepositoryConfigurationDO> repositories = repositoryList.getRepositoriesAsSet();
            String dataSrcConfigFile_common = WebRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(DestinyRepository.COMMON_REPOSITORY.getConfigFileName());
            for (IRepositoryConfigurationDO ds : repositories) {
                DestinyRepository repository = DestinyRepository.getByName(ds.getName());
                String dataSrcConfigFile = WebRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(repository.getConfigFileName());

                // Create the HibernateRepositoryImpl object only if the
                // configs
                // exist:
                if (webAppFileLocator.exists(dataSrcConfigFile_common) && webAppFileLocator.exists(dataSrcConfigFile)) {
                    getLog().debug("initalizing repository " + repository.getName());
                    HashMapConfiguration dsConfig = new HashMapConfiguration();
                    dsConfig.setProperty(AbstractConfigurableHibernateRepositoryImpl.COMMON_CONFIG, 
                                         webAppFileLocator.getResourceAsStream(dataSrcConfigFile_common));
                    dsConfig.setProperty(AbstractConfigurableHibernateRepositoryImpl.REPOSITORY_CONFIG, 
                                         webAppFileLocator.getResourceAsStream(dataSrcConfigFile));
                    dsConfig.setProperty(PoolBackedHibernateRepositoryImpl.REPOSITORY_DEFINITION, ds);
                    ComponentInfo<PoolBackedHibernateRepositoryImpl> dsInfo = 
                        new ComponentInfo<PoolBackedHibernateRepositoryImpl>(
                            repository.getName(), 
                            PoolBackedHibernateRepositoryImpl.class, 
                            IHibernateRepository.class, 
                            LifestyleType.SINGLETON_TYPE, 
                            dsConfig);
                    IHibernateRepository factory = compMgr.getComponent(dsInfo);
                } else {
                    getLog().debug("webAppFileLocator can't find either " + dataSrcConfigFile_common + " or " + dataSrcConfigFile);
                }
            }
        } else {
            getLog().debug("repositoryList is null");
        }

        serverPluginManager = (IDCCServerPluginManager)compMgr.getComponent(DCCServerPluginManager.COMP_INFO);
        serverPluginManager.initializePlugins(this);

        initMessageHandlerManager();
    }
    

    protected void initMessageHandlerManager(){
        final IDestinyConfigurationStore configMgr = getManager().getComponent(
            DestinyConfigurationStoreImpl.COMP_INFO);
        
        IComponentManager compMgr = getManager();
        
        IMessageHandlerManager messageHandlerManager =
            compMgr.getComponent(MessageHandlerManagerImpl.class);
        
        IMessageHandlersConfigurationDO handlersConfig = configMgr.retrieveMessageHandlersConfig();
        if (handlersConfig != null) {
            List<IMessageHandlerConfigurationDO> handlerConfigs = handlersConfig.getHandlerConfigs();
            if (handlerConfigs != null) {
                for (IMessageHandlerConfigurationDO handlerConfig : handlerConfigs) {
                    String name = handlerConfig.getName();
                    
                    if(name == null || name.trim().length() == 0){
                        getLog().error("The Message Handler's name is required.");
                        continue;
                    }
                    
                    String className = handlerConfig.getClassName();
                    Properties properties = handlerConfig.getProperties();

                    Class senderClass;
                    try {
                        senderClass = Class.forName(className);
                    } catch (Throwable e) {
                        getLog().error("The Message Handler, " + name + " can't be loaded.", e);
                        continue;
                    } 
                    
                    if (!IMessageHandler.class.isAssignableFrom(senderClass)) {
                        getLog().error("The Message Handler, " + name + " can't be loaded. " 
                                       + senderClass + " is not a supported type.");
                        continue;
                    }
                    
                    MapMessageHandlerConfig config = new MapMessageHandlerConfig(properties);
                    
                    try {
                        messageHandlerManager.defineMessageHandler(name, senderClass, config);
                        getLog().debug("MessageHandler, " + name + ", " + senderClass + ", is loaded");
                    } catch (MessagingException e) {
                        getLog().error("The Message Handler, " + name + " can't be initialized.", e);
                    }
                }
            }
        }
    }
    
    /**
     * Sets the container name
     * 
     * @param containerName
     *            The container name to set.
     */
    protected final void setComponentName(String containerName) {
        this.componentName = containerName;
    }

    /**
     * Sets the container type
     * 
     * @param compType
     *            The containerType to set.
     */
    protected final void setComponentType(ServerComponentType compType) {
        this.componentType = compType;
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
     * Sets the component manager
     * 
     * @param newMgr
     *            component manager to set
     */
    public void setManager(IComponentManager newMgr) {
        this.manager = newMgr;
    }
}
