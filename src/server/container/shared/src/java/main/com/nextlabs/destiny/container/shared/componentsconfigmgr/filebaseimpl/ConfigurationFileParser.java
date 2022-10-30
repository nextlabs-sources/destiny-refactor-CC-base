/*
 * Created on Dec 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IGenericComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.CustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DACComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DCSFComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DEMComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DMSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.GenericComponentsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.MessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.MgmtConsoleComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ReporterComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.server.shared.configuration.type.DayOfWeek;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.crypt.IDecryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 * 
 * This class parses Destiny configuration files to create config objects. There
 * are a couple of configuration-related files that this component reads upon
 * initialization. These include: - XSD file for defining the strongly typed
 * configuration objects - Destiny configuration file that conforms to the
 * schema in the XSD file - Digester rules file for parsing and constructing the
 * XSD defined objects from the config file.
 * 
 * The location of these files is specified by the caller in the validate/parse
 * methods.
 */

public class ConfigurationFileParser {
 // Configuration-related parameter names (these are present in the
    // server.xml file)
    public static final String CONFIG_SCHEMA_FILE_NAME = "Configuration.xsd";
    public static final String CONFIG_DATA_FILE_NAME = "configuration.xml";
    public static final String CONFIG_DIGESTER_RULES_FILE_NAME = "configuration.digester.rules.xml";

    // This is the namespace for the Destiny configuration schema. We're
    // hardcoding it.
    // TODO: Decide if we want the DestinyConfigurationParser class to
    // extract this
    //       directly from the schema file -- that would make it more generic.
    public static final String SCHEMA_NAMESPACE = "http://bluejungle.com/destiny/services/management/types";


    // Error messages:
    private static final String INVALID_DATA_SOURCE_ERR = "An invalid repository definition was provided - '{0}'";
    private static final String MISSING_DATA_SOURCE_ERR = "A required repository definition was not provided - '{0}'";
    private static final String DUPLICATE_DATA_SOURCE_ERR = "A repository definition has been provided more than once - '{0}'";
    private static final String INVALID_CONNECTION_POOL_REF_ERR = "An invalid connection pool: '{0}' was referenced within repository definition: '{1}'";
    private static final String UNSUPPORTED_CONNECTION_POOL_CONFIG_ERR = "Repository definition: '{0}' contains a nested connection pool configuration";
    private static final String DUPLICATE_CONNECTION_POOL_ERR = "More than 1 connection pools exist with name: '{0}'";
    private static final String NO_CONNECTION_POOL_ERR = "No connection pool was configured. At least one must exist for valid data access.";
    private static final String DUPLICAE_GENERIC_COMPONENT = "More than one generic component with name '{0}'. The name must be unique."; 

    // Private variables:
    private ApplicationUserConfigurationDO applicationUserConfig;
    private MessageHandlersConfigurationDO messageHandleresConfig;
    private CustomObligationsConfigurationDO customObligationsConfig;
    private ActionListConfigDO actionListConfig;
    private Map<ServerComponentType, IDCCComponentConfigurationDO> dccConfigsByType;
    private Map<String, ConnectionPoolConfigurationDO> connectionPoolsByName;
    private RepositoryConfigurationList repositories;
    private Collection<String> parseErrors;
    private IDecryptor decryptor = new ReversibleEncryptor();

    /**
     * 
     * Constructor
     * 
     * Currently does nothing.
     */
    public ConfigurationFileParser() {
        this.dccConfigsByType = new HashMap<ServerComponentType, IDCCComponentConfigurationDO>();
        this.connectionPoolsByName = new HashMap<String, ConnectionPoolConfigurationDO>();
        
        ConvertUtils.register(new CalendarConverter(), Calendar.class);
        ConvertUtils.register(new DayOfWeekConverter(), DayOfWeek.class);
    }

    /**
     * Validates the XML file 'dataFileLoc' against the XSD 'schemaFileLoc'
     * using the namespace provided 'schemaNamespace'. Returns a list, if
     * applicable, of the schematic errors encountered.
     * 
     * @param schemaFileLoc
     * @param schemaNamespace
     * @param dataFileLoc
     * @return Collection of error messages gathered during validation checking
     */
    public Collection<String> validateConfig(String schemaFileLoc, String schemaNamespace, String dataFileLoc) {
        File input = new File(dataFileLoc);
        File schema = new File(schemaFileLoc);
        return validateConfig(schema, schemaNamespace, input);
    }
    
    public Collection<String> validateConfig(File folder) {
        return validateConfig(
                new File(folder, CONFIG_SCHEMA_FILE_NAME), // File schemaFile
                SCHEMA_NAMESPACE,                          // String schemaNamespace
                new File(folder, CONFIG_DATA_FILE_NAME));  // File dataFile
    }
    
    public Collection<String> validateConfig(File schemaFile, String schemaNamespace, File dataFile) {
        ConfigurationFileErrorHandler errorLogger = new ConfigurationFileErrorHandler();

        DOMParser parser;
        try {
            parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
            String schemaLocation = schemaFile.toURL().toString();
            schemaLocation = schemaLocation.replaceAll(" ", "%20");
            parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
                    schemaNamespace + " " + schemaLocation);

            parser.setErrorHandler(errorLogger);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException(e);
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        try {
            parser.parse(dataFile.toURL().toString());
        } catch (SAXException e) {
            parseErrors.add(e.toString());
        } catch (IOException e) {
            parseErrors.add(e.toString());
        }

        // Check the end result of the validation parse:
        return errorLogger.doErrorsExist() ? errorLogger.getErrorMessages() : null;
    }

    /**
     * Parses the provided config file 'configFileLoc' using the XSD and
     * digester rules files and stores the corresponding strongly typed
     * configuration objects. Returns a list of errors encountered during
     * parsing - ie. duplicate entry errors.
     * 
     * @param schemaFileLoc
     * @param dataFileLoc
     * @param digesterRulesFileLoc
     * @return Collection of error messages gathered during parsing
     */
    public Collection<String> parseConfig(String dataFileLoc, String digesterRulesFileLoc) {
        File input = new File(dataFileLoc);
        File rules = new File(digesterRulesFileLoc);

        return parseConfig(input, rules);
    }
    
    public Collection<String> parseConfig(File folder) {
        return parseConfig(
                new File(folder, CONFIG_DATA_FILE_NAME), //input, 
                new File(folder, CONFIG_DIGESTER_RULES_FILE_NAME)  //rules
                );
    }
    
    private void addError(String message){
        parseErrors.add(message);
    }
    
    /**
     * @param schemaFile
     * @param dataFile
     * @param digesterRulesFile
     * @return
     */
    public Collection<String> parseConfig(File dataFile, File digesterRulesFile) {
        // Initialize the config objects:
        applicationUserConfig = new ApplicationUserConfigurationDO();
        
        messageHandleresConfig = new MessageHandlersConfigurationDO();
        customObligationsConfig = new CustomObligationsConfigurationDO(); 
        actionListConfig = new ActionListConfigDO();
        parseErrors = new ArrayList<String>();

        try {
            // Parse the configuration file using the Destiny-specific extension
            // to the Apache Digester and the Destiny configuration rules file.
            Digester digester = DigesterLoader.createDigester(digesterRulesFile.toURL());

            // Add self to the digester stack. The rules will then call methods
            // on 'this' to set the auth object and the dcc config objects:
            digester.push(this);

            // Parse:
            digester.parse(dataFile);

            // Post process configuration:
            processConfiguration();
        } catch (IllegalArgumentException e) {
            parseErrors.add(e.getMessage());
        } catch (MalformedURLException e) {
            parseErrors.add(e.getMessage());
        } catch (IOException e) {
            parseErrors.add(e.getMessage());
        } catch (SAXException e) {
            parseErrors.add(e.getMessage());
        }

        // Return error list, if any:
        return this.parseErrors.size() > 0 ? this.parseErrors : null;
    }

    

    /**
     * Post processes the configuration after all data has been parsed
     *  
     */
    private void processConfiguration() {
    }

    /**
     * This method is called by the digester library on encountering an
     * authentication configuration element.
     * 
     * @param conf
     */
    public void setApplicationUserConfiguration(ApplicationUserConfigurationDO conf) {
        applicationUserConfig = conf;
    }
    
    public void setMessageHandlersConfiguration(MessageHandlersConfigurationDO conf){
        messageHandleresConfig = conf;
    }

    /**
     * This method is called by the digester on encountering a custom obligations
     * configuration element
     */
    public void setCustomObligationsConfiguration(CustomObligationsConfigurationDO conf) {
        customObligationsConfig = conf;
    }
    
    /**
     * This method is called by the digester library when it enconters the
     * ActionList configuration
     * 
     * @param conf
     */
    public void setActionListConfig(ActionListConfigDO conf) {
        actionListConfig = conf;
    }

    /**
     * This method is called by the digester library on encountering a data
     * sources configuration element
     * 
     * @param dataSourcesConfig
     */
    public void setRepositories(RepositoryConfigurationList dataSources) {
        this.repositories = dataSources;
        Set<String> providedDataSources = new HashSet<String>();

        RepositoryConfigurationDO[] dataSrcArray = this.repositories.getRepositoriesAsArray();

        // Check that all provided data sources are valid. Also make sure no
        // duplicates are given:
        for (int i = 0; i < dataSrcArray.length; i++) {
            RepositoryConfigurationDO source = dataSrcArray[i];
            String name = source.getName();

            // Check for validity:
            if (DestinyRepository.getByName(name) == null) {
                addError(MessageFormat.format(INVALID_DATA_SOURCE_ERR, name));
            }

            // Check for duplicity:
            if (providedDataSources.contains(name)) {
                addError(MessageFormat.format(DUPLICATE_DATA_SOURCE_ERR, name));
            } else {
                providedDataSources.add(name);
            }
        }

        // Verify that all the required data-sources have been provided
        for (DestinyRepository repository : DestinyRepository.values()) {
            switch(repository) {
            case DICTIONARY_REPOSITORY:
            case COMMON_REPOSITORY:
                break;
            default:
                if (!providedDataSources.contains(repository.getName())) {
                    addError(MessageFormat.format(MISSING_DATA_SOURCE_ERR, repository.getName()));
                }
            }
        }

        // Store the connection pool configuration objects:
        ConnectionPoolConfigurationDO[] connectionPoolConfigs = this.repositories.getConnectionPoolsAsArray();
        if (connectionPoolConfigs != null) {
            for (ConnectionPoolConfigurationDO connectionPoolConfig : connectionPoolConfigs) {
                if (!this.connectionPoolsByName.containsKey(connectionPoolConfig.getName())) {
                    this.connectionPoolsByName.put(connectionPoolConfig.getName(), connectionPoolConfig);
                } else {
                    addError(MessageFormat.format(DUPLICATE_CONNECTION_POOL_ERR, 
                            connectionPoolConfig.getName()));
                }
            }
        }

        // Resolve connection pool references to real connection pool
        // configurations within repository configuration objects:
        RepositoryConfigurationDO[] configs = this.repositories.getRepositoriesAsArray();
        for (int i = 0; i < configs.length; i++) {
            RepositoryConfigurationDO config = configs[i];
            if (config.getConnectionPoolConfiguration() == null) {
                if (this.connectionPoolsByName.containsKey(config.getConnectionPoolName())) {
                    ConnectionPoolConfigurationDO poolCfg = this.connectionPoolsByName.get(config.getConnectionPoolName());
                    if ( !poolCfg.getIsPasswordDecoded() ) {
                        final String poolPassword = poolCfg.getPassword();
                        if (poolPassword != null) {
                            poolCfg.setPassword(this.decryptor.decrypt(poolPassword));
                            poolCfg.setIsPasswordDecoded(true);
                        }
                    }
                    config.setConnectionPoolConfiguration(poolCfg);
                } else {
                    addError(MessageFormat.format(INVALID_CONNECTION_POOL_REF_ERR, 
                            config.getConnectionPoolName(), config.getName()));
                }
            } else {
                addError(
                        MessageFormat.format(UNSUPPORTED_CONNECTION_POOL_CONFIG_ERR, config.getName()));
            }
        }
    }
    
    private void addDCCConfiguration(ServerComponentType type,
            IDCCComponentConfigurationDO config) {
        if (dccConfigsByType.containsKey(type)) {
            addError(MessageFormat.format(DUPLICAE_GENERIC_COMPONENT, type));
        } else {
            dccConfigsByType.put(type, config);
        }
    }

    /**
     * This method is called by the digester library when it encounteres the DMS
     * configuration
     * 
     * @param conf
     */
    public void setDMSConfiguration(DMSComponentConfigurationDO conf) {
        addDCCConfiguration(ServerComponentType.DMS, conf);
    }

    /**
     * This method is called by the digester library when it encounteres the
     * DCSF configuration
     * 
     * @param conf
     */
    public void setDCSFConfiguration(DCSFComponentConfigurationDO conf) {
        addDCCConfiguration(ServerComponentType.DCSF, conf);
    }

    /**
     * This method is called by the digester library when it encounteres the
     * DABS configuration
     * 
     * @param conf
     */
    public void setDABSConfiguration(DABSComponentConfigurationDO conf) {
        addDCCConfiguration(ServerComponentType.DABS, conf);
    }

    /**
     * This method is called by the digester library when it encounteres the DAC
     * configuration
     * 
     * @param conf
     */
    public void setDACConfiguration(DACComponentConfigurationDO conf) {
        addDCCConfiguration(ServerComponentType.DAC, conf);
    }

    /**
     * Sets the DEM configuration into the master config
     * 
     * @param config
     *            DEM configuration to set
     */
    public void setDEMConfiguration(DEMComponentConfigurationDO config) {
        addDCCConfiguration(ServerComponentType.DEM, config);
    }

    /**
     * This method is called by the digester library when it encounteres the DPS
     * configuration
     * 
     * @param conf
     */
    public void setDPSConfiguration(DPSComponentConfigurationDO conf) {
        addDCCConfiguration(ServerComponentType.DPS, conf);
    }

    /**
     * This method is called by the digester library when it encounteres the
     * MgmtConsole configuration
     * 
     * @param conf
     */
    public void setMgmtConsoleConfiguration(MgmtConsoleComponentConfigurationDO conf) {
        addDCCConfiguration(ServerComponentType.MGMT_CONSOLE, conf);
    }

    /**
     * This method is called by the digester library when it encounteres the
     * Reporter configuration
     * 
     * @param conf
     */
    public void setReporterConfiguration(ReporterComponentConfigurationDO conf) {
        addDCCConfiguration(ServerComponentType.REPORTER, conf);
    }
    
    public void setGenericComponentsConfiguration(GenericComponentsConfigurationDO conf) {
        List<IGenericComponentConfigurationDO> compList = conf.getGenericComponents();
       
        for(IGenericComponentConfigurationDO comp : compList) {
            ServerComponentType type = comp.getComponentType();
            addDCCConfiguration(type, comp);
        }
    }
    
    /**
     * Returns a hashmap of the DCC component config objects that were parsed
     * from the config file.
     * 
     * @return HashMap of (component name, DCCConfiguration) objects
     */
    public Map<ServerComponentType, IDCCComponentConfigurationDO> getDCCConfigs() {
        return dccConfigsByType;
    }
    
    public IDCCComponentConfigurationDO getDCCConfig(ServerComponentType type) {
        return dccConfigsByType.get(type);
    }

    /**
     * Returns the authentication component config object that was parsed from
     * the config file.
     * 
     * @return ApplicationUserConfiguration
     */
    public ApplicationUserConfigurationDO getApplicationUserConfig() {
        return applicationUserConfig;
    }
    
    public MessageHandlersConfigurationDO getMessageHandlersConfig(){
        return messageHandleresConfig;
    }

    /**
     * Returns the custom obligations configuration object that was parsed from
     * the config file.
     * 
     * @return CustomObligationsConfiguration
     */
    public CustomObligationsConfigurationDO getCustomObligationsConfig() {
        return customObligationsConfig;
    }
    
    /**
     * Returns Action List that was parsed from the config file.
     * 
     * @return Action List configuration
     */
    public ActionListConfigDO getActionListConfig() {
        return actionListConfig;
    }

    /**
     * Returns the data sources configuration object that was parsed from the
     * config file
     * 
     * @return
     */
    public RepositoryConfigurationList getRepositories() {
        return repositories;
    }
    
}
