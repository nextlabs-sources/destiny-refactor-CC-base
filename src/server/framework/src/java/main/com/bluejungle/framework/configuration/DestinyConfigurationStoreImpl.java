/*
 * Created on Nov 24, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.framework.configuration;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * This is the configuration manager implementation class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/configuration/DestinyConfigurationStoreImpl.java#7 $:
 */
public class DestinyConfigurationStoreImpl implements IDestinyConfigurationStore {

    /**
     * Component initialization
     */
    public static final ComponentInfo<DestinyConfigurationStoreImpl> COMP_INFO = 
    	new ComponentInfo<DestinyConfigurationStoreImpl>(
    			IDestinyConfigurationStore.COMP_NAME, 
    			DestinyConfigurationStoreImpl.class, 
    			LifestyleType.SINGLETON_TYPE);

    /**
     * Context lookup
     */
    private static final String INIT_JAVA_ENV_NAME = "java:/comp/env/";

    /*
     * Configuration cache variables:
     */
    private IApplicationUserConfigurationDO applicationUserConfig;
    private IMessageHandlersConfigurationDO messageHandleresConfig;
    private ICustomObligationsConfigurationDO customObligationsConfig;
    private IActionListConfigDO actionListConfig;
    private Map<String, IDCCComponentConfigurationDO> dccConfigsByType;
    private RepositoryConfigurationList repositories;
    
    

    /**
     * Constructor
     */
    public DestinyConfigurationStoreImpl() {
        this.dccConfigsByType = new HashMap<String, IDCCComponentConfigurationDO>();
    }

    /**
     * Returns the value of a configuration parameter
     * 
     * @param name
     *            parameter name
     * @return the value of the configuration parameter
     */
    public String getConfigParam(String name) {
        //If the configuration is already cached, return the cached value,
        //otherwise retrieve the default value.
        String paramValue = null;
        boolean cached = false;
        if (cached) {
            //TODO retrive config param from cache
        } else {
            paramValue = getDefaultConfigParam(name);
        }
        return (paramValue);
    }

    /**
     * Retrieves the default value of the configuration parameter (from the
     * server.xml default environment variable value)
     * 
     * @param name
     *            parameter name
     * @return the value of the configuration parameter
     */
    private String getDefaultConfigParam(String name) {
        String result = null;
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        String resourceName = INIT_JAVA_ENV_NAME + name;
        try {
            Context initCtx = new InitialContext();
            result = (String) initCtx.lookupLink(resourceName);
        } catch (NamingException e) {
            // Do nothing. This is not an error case since it is not necessary
            // that all parameters exist in the initial context.
            result = null;
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#retrieveActionListConfig()
     */
    public synchronized IActionListConfigDO retrieveActionListConfig() {
        return this.actionListConfig;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IDestinyConfigurationStore#cacheAuthConfig(com.bluejungle.destiny.services.management.types.ApplicationUserConfiguration)
     */
    public synchronized void cacheAuthConfig(IApplicationUserConfigurationDO config) {
        if (this.applicationUserConfig == null) {
            this.applicationUserConfig = config;
        }
    }
    
    public synchronized IMessageHandlersConfigurationDO retrieveMessageHandlersConfig(){
        return messageHandleresConfig;
    }
    
    public synchronized void cacheMessageHandlersConfig(IMessageHandlersConfigurationDO newConfig) {
        if (messageHandleresConfig == null) {
            this.messageHandleresConfig = newConfig;
        }
    }

    /**
     * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#retrieveAuthConfig()
     */
    public synchronized IApplicationUserConfigurationDO retrieveAuthConfig() {
        return this.applicationUserConfig;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IDestinyConfigurationStore#cacheActionListConfig(com.bluejungle.destiny.services.management.types.ActionListConfigDO)
     */
    public synchronized void cacheActionListConfig(IActionListConfigDO newConfig) {
        if (this.actionListConfig == null) {
            this.actionListConfig = newConfig;
        }
    }

    /**
     * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#retrieveCustomObligationsConfig()
     */
    public synchronized ICustomObligationsConfigurationDO retrieveCustomObligationsConfig() {
        return this.customObligationsConfig;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IDestinyConfigurationStore#cacheCustomObligationsConfig(com.bluejungle.destiny.services.management.types.CustomObligationsConfiguration)
     */
    public synchronized void cacheCustomObligationsConfig(ICustomObligationsConfigurationDO config) {
        if (this.customObligationsConfig == null) {
            this.customObligationsConfig = config;
        }
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IDestinyConfigurationStore#retrieveComponentConfiguration(java.lang.String)
     */
    public synchronized IDCCComponentConfigurationDO retrieveComponentConfiguration(String type) {
        return this.dccConfigsByType.get(type);
    }

    /**
     * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#cacheComponentConfiguration(java.lang.String,
     *      com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO)
     */
    public synchronized void cacheComponentConfiguration(String type,
            IDCCComponentConfigurationDO config) {
        if (this.dccConfigsByType.get(type) == null) {
            this.dccConfigsByType.put(type, config);
        }
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IDestinyConfigurationStore#retrieveRepositoryConfigurations()
     */
    public synchronized RepositoryConfigurationList retrieveRepositoryConfigurations() {
        return this.repositories;
    }

    /**
     * @see com.bluejungle.framework.configuration.IDestinyConfigurationStore#cacheRepositoryConfigurations(com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList)
     */
    public synchronized void cacheRepositoryConfigurations(RepositoryConfigurationList configs) {
        if (this.repositories == null) {
            this.repositories = configs;
        }
    }
}
