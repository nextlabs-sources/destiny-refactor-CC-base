/*
 * Created on Jul 26, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/DDACComponentImpl.java#1 $:
 */

package com.nextlabs.destiny.container.ddac;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.server.shared.configuration.IGenericComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.ddac.components.deployment.IPolicyDeploymentManager;
import com.nextlabs.destiny.container.ddac.components.deployment.PolicyDeploymentManagerImpl;
import com.nextlabs.destiny.container.ddac.configuration.DDACConfiguration;


public class DDACComponentImpl extends BaseDCCComponentImpl {
    private static final String COMPONENT_TYPE_NAME = "DDAC";
    private static final String DDAC_CONFIG_FILE = "ddac_config.xml";
    private static final String DDAC_MAPPING_FILE = "ddac_mapping.xml";
    private static final IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

    public static final ServerComponentType COMPONENT_TYPE = ServerComponentType.fromString(COMPONENT_TYPE_NAME);

    private DDACConfiguration ddacConfig = null;

    @Override public ServerComponentType getComponentType() {
        return COMPONENT_TYPE;
    }

    @Override
    public void init() {
        super.init();
        loadConfiguration();

        final IDestinyConfigurationStore configMgr = getManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);

        IGenericComponentConfigurationDO componentConfig = (IGenericComponentConfigurationDO) configMgr.retrieveComponentConfiguration(COMPONENT_TYPE_NAME);

        if (componentConfig == null) {
            throw new NullPointerException("Destiny Configuration not defined for " + COMPONENT_TYPE_NAME);
        }

        final ComponentInfo<AgentManager> agentMgrCompInfo = new ComponentInfo<AgentManager>(IAgentManager.COMP_NAME, 
                                                                                             AgentManager.class, 
                                                                                             IAgentManager.class, 
                                                                                             LifestyleType.SINGLETON_TYPE);

        final IAgentManager agentMgr = compMgr.getComponent(agentMgrCompInfo);

        final ComponentInfo<HibernateProfileManager> profileMgrCompInfo =
            new ComponentInfo<HibernateProfileManager>(IProfileManager.COMP_NAME, 
                                                       HibernateProfileManager.class, 
                                                       IProfileManager.class, 
                                                       LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(profileMgrCompInfo);

        IHibernateRepository mgmtDataSrc = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        if (mgmtDataSrc == null) {
            throw new RuntimeException("Data source " + DestinyRepository.MANAGEMENT_REPOSITORY + " is not correctly setup for the DDAC component.");
        }

        DDACAgentServiceImpl agentServiceImpl = new DDACAgentServiceImpl();
        agentServiceImpl.registerAgents(getDDACConfig().getADConfig());

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration policyDeploymentConfig = new HashMapConfiguration();
        policyDeploymentConfig.setProperty(IPolicyDeploymentManager.DDAC_CONFIGURATION, getDDACConfig());
        policyDeploymentConfig.setProperty(IPolicyDeploymentManager.DDAC_AGENT_SERVICE, agentServiceImpl);
        policyDeploymentConfig.setProperty(IPolicyDeploymentManager.MGMT_DATA_SOURCE, mgmtDataSrc);
        policyDeploymentConfig.setProperty(IPolicyDeploymentManager.DDAC_COMPONENT_NAME, getComponentName());
        
        ComponentInfo<PolicyDeploymentManagerImpl> policyDeploymentComponentInfo =
            new ComponentInfo<PolicyDeploymentManagerImpl>(IPolicyDeploymentManager.COMP_NAME,
                                                           PolicyDeploymentManagerImpl.class,
                                                           IPolicyDeploymentManager.class,
                                                           LifestyleType.SINGLETON_TYPE,
                                                           policyDeploymentConfig);

        IPolicyDeploymentManager policyDeploymentManger = compMgr.getComponent(policyDeploymentComponentInfo);
    }

    private DDACConfiguration getDDACConfig() {
        return ddacConfig;
    }

    private void loadConfiguration() {
        INamedResourceLocator serverResourceLocator = (INamedResourceLocator) compMgr.getComponent(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);
        String configFile = serverResourceLocator.getFullyQualifiedName(ServerRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(DDAC_CONFIG_FILE));
        String mappingFile = serverResourceLocator.getFullyQualifiedName(ServerRelativeFolders.CONFIGURATION_FOLDER.getPathOfContainedFile(DDAC_MAPPING_FILE));

        Unmarshaller ddacUnmarshaller = new Unmarshaller(DDACConfiguration.class);

        Mapping mapping = new Mapping();

        try {
            mapping.loadMapping(new InputSource(mappingFile));

            ddacUnmarshaller.setMapping(mapping);
        } catch (FileNotFoundException fnfe) {
            getLog().error("Could not open file: " + mappingFile, fnfe);
        } catch (MappingException me) {
            getLog().error("Error in " + mappingFile, me);
        } catch (IOException ioe) {
            getLog().error("General I/O exception for " + mappingFile, ioe);
        }


        try {
            FileReader reader = new FileReader(configFile);
            ddacConfig = (DDACConfiguration)ddacUnmarshaller.unmarshal(reader);
        } catch(FileNotFoundException fnfe) {
            getLog().error("Can not find file " + configFile, fnfe);
        } catch(MarshalException me) {
            getLog().error("File " + configFile + " is not of correct format", me);
        } catch(ValidationException ve) {
            getLog().error("Can not validate file " + configFile, ve);
        }

        getLog().debug("Read configuration information");
    }
}
