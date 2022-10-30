/*
 * Created on Nov 24, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.framework.configuration;

import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;

/**
 * This interface represents storage of configuration data - both Destiny
 * configuration and server (e.g. Tomcat) configuration
 * 
 * @author ihanen
 */
public interface IDestinyConfigurationStore {

    public static final String COMP_NAME = "DestinyConfigurationStore";

    public static final String DESTINY_CONFIG_FILE_PARAM = "DestinyConfigurationFile";

    /**
     * Returns the configuration for the authentication module
     * 
     * @return the configuration for the authentication module
     */
    public IApplicationUserConfigurationDO retrieveAuthConfig();

    /**
     * Saves the configuration for the authentication module
     * 
     * @param config
     */
    public void cacheAuthConfig(IApplicationUserConfigurationDO config);
    
    /**
     * Returns the configuration for message handlers
     * 
     * @return the configuration for message handlers
     */
    public IMessageHandlersConfigurationDO retrieveMessageHandlersConfig();
    
    /**
     * Saves the message handlers configuration
     * 
     * @param configs
     */
    public void cacheMessageHandlersConfig(IMessageHandlersConfigurationDO newConfig);
    
    /**
     * Returns the configuration for action list
     * 
     * @return the configuration for action list
     */
    public IActionListConfigDO retrieveActionListConfig();
    
    /**
     * Saves the action list configuration
     * 
     * @param configs
     */
    public void cacheActionListConfig(IActionListConfigDO newConfig);

    /**
     * Returns the obligation configuration
     *
     * @return the obligation configuration
     */
    public ICustomObligationsConfigurationDO retrieveCustomObligationsConfig();

    /**
     * Saves the custom obligation cofiguration
     *
     * @param config
     *
     */
    public void cacheCustomObligationsConfig(ICustomObligationsConfigurationDO config);

    /**
     * Returns the configuration for the provided DCC component type
     * 
     * @param namtype
     *            the type of the DCC component
     * @return the DCC Configuration for that name
     */
    public IDCCComponentConfigurationDO retrieveComponentConfiguration(String type);

    /**
     * Saves the provided configuration for the provided DCC component type
     * 
     * @param type
     * @param config
     */
    public void cacheComponentConfiguration(String type, IDCCComponentConfigurationDO config);

    /**
     * Returns the data sources configuration
     * 
     * @return configuration list
     */
    public RepositoryConfigurationList retrieveRepositoryConfigurations();

    /**
     * Saves the data sources configuration
     * 
     * @param configs
     */
    public void cacheRepositoryConfigurations(RepositoryConfigurationList configs);

    /**
     * Returns the value of a configuration parameter
     * 
     * @param name
     *            parameter name
     * @return the value of the configuration parameter
     */
    public String getConfigParam(String name);
}
