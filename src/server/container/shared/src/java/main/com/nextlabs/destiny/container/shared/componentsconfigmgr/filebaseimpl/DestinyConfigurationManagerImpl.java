/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.CustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.MessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.IDestinyConfigurationManager;

/**
 * Implementation class for the IDCCConfigurationManager interface. This class
 * reads configuration information from a configuration file.
 * 
 * @author safdar
 * @version $Id:  
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/DestinyConfigurationManagerImpl.java#2 $
 */

public class DestinyConfigurationManagerImpl implements IDestinyConfigurationManager, IManagerEnabled {
    /*
     * Component manager-related variables:
     */
    private IConfiguration config;
    private IComponentManager manager;
    private Log log;

    /*
     * Configuration-related variables:
     */
    private ApplicationUserConfigurationDO applicationUserConfig;
    private MessageHandlersConfigurationDO messageHandlersConfig;
    private CustomObligationsConfigurationDO customObligationsConfig;
    private ActionListConfigDO actionListConfig;
    private Map<ServerComponentType, IDCCComponentConfigurationDO> dccComponentConfigs;
    private RepositoryConfigurationList dataSources;
    
    /*
     * Configuration parser-related files:
     */
    private String configSchemaFileLoc;
    private String configDataFileLoc;
    private String digesterRulesFileLoc;

    /**
     * Constructor
     *  
     */
    public DestinyConfigurationManagerImpl() {
        super();
    }

    /**
     * Overloaded Constructor
     * 
     * ONLY to be used for JUnit testing. Not applicable to Destiny application.
     * 
     * Obtains the configuration-related file locations as parameters. Then
     * calls initialize() after reading in the configuration-related file
     * locations.
     * 
     * @param schema -
     *            Location of schema file
     * @param data -
     *            Location of data file
     * @param digesterRules -
     *            Location of digester rules file
     * @throws ConfigurationInitException
     */
    public DestinyConfigurationManagerImpl(String schema, String data, String digesterRules) {
        super();

        // Read locations of necessary files:
        configSchemaFileLoc = schema;
        configDataFileLoc = data;
        digesterRulesFileLoc = digesterRules;

        // Initialize the configuration objects:
        try {
            initialize();
        } catch (ConfigurationInitException e) {
            throw new IllegalStateException("configuration manager could not be initialized", e);
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        // Initialize comp mgr and shared context locator:
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);
        INamedResourceLocator serverResourceLocator = (INamedResourceLocator) compMgr.getComponent(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);

        // Get hold of the shared-configuration cache:
        final IDestinyConfigurationStore configStore = (IDestinyConfigurationStore) getManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);

        // Read the schema/rules file locs from the web-app resource locator:
        configSchemaFileLoc = serverResourceLocator.getFullyQualifiedName(ServerRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(ConfigurationFileParser.CONFIG_SCHEMA_FILE_NAME));
        digesterRulesFileLoc = serverResourceLocator.getFullyQualifiedName(ServerRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(ConfigurationFileParser.CONFIG_DIGESTER_RULES_FILE_NAME));

        // Read the actual configuration data file:
        // The default is to always use "destiny.configuration.file" - the most
        // likely customer scenario. For internal use however, this can be
        // overridden via a server.xml context param:
        String configFileName = configStore.getConfigParam(IDestinyConfigurationStore.DESTINY_CONFIG_FILE_PARAM);
        if (configFileName == null) {
            configFileName = ConfigurationFileParser.CONFIG_DATA_FILE_NAME;
        }
        this.configDataFileLoc = serverResourceLocator.getFullyQualifiedName(ServerRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(configFileName));

        // Initialize the configuration objects:
        try {
            initialize();
        } catch (ConfigurationInitException e) {
            throw new RuntimeException("configuration manager could not be initialized", e);
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return config;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDestinyConfigurationManager#getApplicationUserConfiguration()
     */
    public IApplicationUserConfigurationDO getApplicationUserConfiguration() {
        return applicationUserConfig;
    }

    public IMessageHandlersConfigurationDO getMessageHandlersConfiguration() {
        return messageHandlersConfig;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDestinyConfigurationManager#getCustomObigationsConfiguration()
     */
    public ICustomObligationsConfigurationDO getCustomObligationsConfiguration() {
        return customObligationsConfig;
    }
    
    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDestinyConfigurationManager#getActionListConfig()
     */
    public IActionListConfigDO getActionListConfig() {
        return actionListConfig;
    }
    
    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDestinyConfigurationManager#getDCCConfiguration(com.bluejungle.destiny.container.dms.components.configmgr.IDCCComponentType)
     */
    public IDCCComponentConfigurationDO getDCCConfiguration(ServerComponentType type) {
        return dccComponentConfigs.get(type);
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDestinyConfigurationManager#getRepositories()
     */
    public Set<? extends IRepositoryConfigurationDO> getRepositories() {
        return dataSources.getRepositoriesAsSet();
    }

    /**
     * Validates the configuration file against the configuration schema, and if
     * all succeeds, it parses the configuration file into the strongly typed
     * configuration objects defined by the schema.
     * 
     * @throws ConfigurationInitException
     */
    private void initialize() throws ConfigurationInitException {
        this.dccComponentConfigs = new HashMap<ServerComponentType, IDCCComponentConfigurationDO>();

        // Ensure file-names are not null:
        if ((configSchemaFileLoc != null) && (configDataFileLoc != null) && (digesterRulesFileLoc != null)) {
            // Check existence of files:
            File xsdFile = new File(configSchemaFileLoc);
            File configFile = new File(configDataFileLoc);
            File digesterRulesFile = new File(digesterRulesFileLoc);

            // Only perform validation and parsing if all files exist:
            if ((xsdFile.exists()) && (configFile.exists()) && (digesterRulesFile.exists())) {
                // Validate configuration against schema:
                this.validateConfig();

                // Parse configuration
                this.parseConfiguration();
            }
        }
    }

    /**
     * Validates the configuration file against the configuration schema.
     * 
     * @throws ConfigurationInitException
     *             if a validation error occurs.
     */
    private void validateConfig() throws ConfigurationInitException {
        ConfigurationFileParser configParser = new ConfigurationFileParser();
        Collection<String> errors = configParser.validateConfig(this.configSchemaFileLoc, ConfigurationFileParser.SCHEMA_NAMESPACE, this.configDataFileLoc);

        // If there is an error we throw an exception
        if (errors != null) {
            throw new ConfigurationInitException(errors);
        }
    }

    /**
     * Parses the configuration files to create the configuration objects using
     * the DestinyConfigurationParser.
     * 
     * @throws ConfigurationInitException
     *             if a parse error occurs.
     */
    private void parseConfiguration() throws ConfigurationInitException {
        // Parse the configuration file and store the config components
        // internally:
        ConfigurationFileParser configParser = new ConfigurationFileParser();
        Collection<String> errors = configParser.parseConfig(configDataFileLoc, digesterRulesFileLoc);

        // If there is an error we throw an exception
        if (errors != null) {
            throw new ConfigurationInitException(errors);
        } else {
            applicationUserConfig = configParser.getApplicationUserConfig();
            messageHandlersConfig = configParser.getMessageHandlersConfig();
            customObligationsConfig = configParser.getCustomObligationsConfig();
            actionListConfig = configParser.getActionListConfig();
            dccComponentConfigs.putAll(configParser.getDCCConfigs());
            dataSources = configParser.getRepositories();
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }
    
}
