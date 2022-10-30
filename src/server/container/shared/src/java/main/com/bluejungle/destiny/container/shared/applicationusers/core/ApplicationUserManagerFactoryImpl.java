/*
 * Created on Jul 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.ApplicationUserManagerImpl;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/ApplicationUserManagerFactoryImpl.java#1 $
 */

public class ApplicationUserManagerFactoryImpl implements IApplicationUserManagerFactory, IInitializable, IConfigurable, IDisposable, IManagerEnabled, ILogEnabled, IHasComponentInfo<ApplicationUserManagerFactoryImpl> {

    private static final ComponentInfo<ApplicationUserManagerFactoryImpl> COMPONENT_INFO = new ComponentInfo<ApplicationUserManagerFactoryImpl>(COMP_NAME, ApplicationUserManagerFactoryImpl.class.getName(), LifestyleType.SINGLETON_TYPE);
    private static final Log LOG = LogFactory.getLog(ApplicationUserManagerFactoryImpl.class);
    
    private IConfiguration configuration;
    private Log logger;
    private IComponentManager manager;
    private IApplicationUserManager singleton;
    private IApplicationUserConfigurationDO applicationUserManagerConfiguration;
    private IHibernateRepository dataSource;
    private IDestinyEventManager eventManager;

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory#getSingleton()
     */
    public IApplicationUserManager getSingleton() {
        return this.singleton;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        LOG.info("Initializing ApplicationUserManagerFactoryImpl ...");
        if (this.configuration.get(APPLICATION_USER_CONFIGURATION) == null) {
            throw new NullPointerException("application user configuration was not provided");
        }
        if (this.configuration.get(MANAGEMENT_REPOSITORY) == null) {
            throw new NullPointerException("management repository was not provided");
        }
        this.applicationUserManagerConfiguration = this.configuration.get(APPLICATION_USER_CONFIGURATION);
        this.dataSource = this.configuration.get(MANAGEMENT_REPOSITORY);
        this.eventManager = ((IDestinySharedContextLocator) getManager().getComponent(IDestinySharedContextLocator.COMP_NAME)).getSharedContext().getEventManager();
        this.singleton = new ApplicationUserManagerImpl(this.dataSource, this.eventManager);
        try {
            this.singleton.initialize(this.applicationUserManagerConfiguration);
        } catch (InitializationException e) {
            LOG.error("Initialization of application user manager factory failed", e);
            throw new RuntimeException("initialization of application user manager factory failed", e);
        } catch (InvalidConfigurationException e) {
            LOG.error("Initialization of application user manager factory failed", e);
            throw new RuntimeException("initialization of application user manager factory failed", e);
        }
        LOG.info("Succesfully initialized ApplicationUserManagerFactoryImpl...");
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.logger;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.logger = log;
    }

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo getComponentInfo() {
        return COMPONENT_INFO;
    }
}
