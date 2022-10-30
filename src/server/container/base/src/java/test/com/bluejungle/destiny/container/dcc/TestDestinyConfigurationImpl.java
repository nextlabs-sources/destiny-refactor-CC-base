/*
 * Created on Nov 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IGenericComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlerConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DACComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DCSFComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DEMComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DMSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.GenericComponentsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.MgmtConsoleComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ReporterComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.server.shared.configuration.type.DayOfWeek;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.crypt.IDecryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/test/com/bluejungle/destiny/container/dcc/TestDestinyConfigurationImpl.java#1 $
 */

public class TestDestinyConfigurationImpl implements ITestDestinyConfiguration, IInitializable, IManagerEnabled {

    private IComponentManager manager;
    private IApplicationUserConfigurationDO applicationUserConfiguration;
    private IMessageHandlersConfigurationDO messageHandlersConfiguration;
    private ICustomObligationsConfigurationDO customObligationsConfiguration;
    private ActionListConfigDO actionListConfig;
    private Map repositories = new HashMap();
    private Map connectionPools = new HashMap();
    private IDecryptor decryptor = new ReversibleEncryptor();
    private Map<ServerComponentType, IDCCComponentConfigurationDO> dccConfigsByType = new HashMap<ServerComponentType, IDCCComponentConfigurationDO>();

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
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        INamedResourceLocator resourceLocator = (INamedResourceLocator) this.manager.getComponent(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);
        InputStream configurationFileStream = resourceLocator.getResourceAsStream("run\\server\\configuration\\configuration.xml");
        File digesterRulesFile = new File(resourceLocator.getFullyQualifiedName("run\\server\\configuration\\configuration.digester.rules.xml"));

        ConvertUtils.register(new CalendarConverter(), Calendar.class);
        ConvertUtils.register(new DayOfWeekConverter(), DayOfWeek.class);
        
        try {
            Digester digester = DigesterLoader.createDigester(digesterRulesFile.toURL());

            digester.push(this);

            digester.parse(configurationFileStream);
        } catch (IOException exception) {
            // Need an IllegalStateException that accepts a cause!
            throw new RuntimeException("Unable to parse configuration file", exception);
        } catch (SAXException exception) {
            throw new RuntimeException("Unable to parse configuration file", exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.ITestDestinyConfiguration#getApplicationUserConfiguration()
     */
    public IApplicationUserConfigurationDO getApplicationUserConfiguration() {
        return this.applicationUserConfiguration;
    }

    public IRepositoryConfigurationDO getRepositoryConfiguration(DestinyRepository repository) {
        return (IRepositoryConfigurationDO) this.repositories.get(repository);
    }
    
    /**
     * @see com.bluejungle.destiny.container.dcc.ITestDestinyConfiguration#getConnectionPoolConfiguration(java.lang.String)
     */
    public IConnectionPoolConfigurationDO getConnectionPoolConfiguration(String connectionPoolName) {
        return (IConnectionPoolConfigurationDO) this.connectionPools.get(connectionPoolName);
    }

    public void setRepositories(RepositoryConfigurationList dataSources) {
        Set connectionPools = dataSources.getConnectionPoolsAsSet();
        Iterator connectionPoolsIterator = connectionPools.iterator();
        while (connectionPoolsIterator.hasNext()) {
            IConnectionPoolConfigurationDO nextConnectionPool = (IConnectionPoolConfigurationDO) connectionPoolsIterator.next();
            this.connectionPools.put(nextConnectionPool.getName(), nextConnectionPool);
        }
        
        Set repositories = dataSources.getRepositoriesAsSet();
        Iterator repositoriesIterator = repositories.iterator();
        while (repositoriesIterator.hasNext()) {
            RepositoryConfigurationDO nextRepository = (RepositoryConfigurationDO) repositoriesIterator.next();
            this.repositories.put(DestinyRepository.getByName(nextRepository.getName()), nextRepository);            
        }       
        
        repositoriesIterator = this.repositories.values().iterator();
        while (repositoriesIterator.hasNext()) {
            RepositoryConfigurationDO nextRepository = (RepositoryConfigurationDO) repositoriesIterator.next();
            IConnectionPoolConfigurationDO nextConnectionPool = (IConnectionPoolConfigurationDO) this.connectionPools.get(nextRepository.getConnectionPoolName());
            nextRepository.setConnectionPoolConfiguration(nextConnectionPool);
        }
        
        
    }
    
    
    /**
     * Set the applicationUserConfiguration
     * @param applicationUserConfiguration The applicationUserConfiguration to set.
     */
    public void setApplicationUserConfiguration(IApplicationUserConfigurationDO applicationUserConfiguration) {
        this.applicationUserConfiguration = applicationUserConfiguration;
    }
    
    public void setMessageHandlersConfiguration(IMessageHandlersConfigurationDO messageHandlersConfiguration) {
        this.messageHandlersConfiguration = messageHandlersConfiguration;
    }

    /**
     * Set the custom obligation configuration
     */
    public void setCustomObligationsConfiguration(ICustomObligationsConfigurationDO customObligationsConfiguration) {
        this.customObligationsConfiguration = customObligationsConfiguration;
    }

    /**
     * Set the action list configuration
     */
    public void setActionListConfig(ActionListConfigDO actionListConfig) {
        this.actionListConfig = actionListConfig;
    }
    
    private void addDCCConfiguration(ServerComponentType type,
            IDCCComponentConfigurationDO config) {
        if (dccConfigsByType.containsKey(type)) {
            // Ignore
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
    public void setDEMConfiguration(DEMComponentConfigurationDO conf) {
        addDCCConfiguration(ServerComponentType.DEM, conf);
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
}
