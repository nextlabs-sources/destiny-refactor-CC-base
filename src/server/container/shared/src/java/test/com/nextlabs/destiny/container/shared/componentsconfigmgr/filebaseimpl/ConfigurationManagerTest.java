/*
 * Created on Dec 8, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDACComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCSFComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDMSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMgmtConsoleComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.DestinyConfigurationManagerImpl;

/**
 * This class tests the Configuration Manager Implementation.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class ConfigurationManagerTest extends TestCase {

    /**
     * Constructor
     *  
     */
    public ConfigurationManagerTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param name
     *            test name
     */
    public ConfigurationManagerTest(String name) {
        super(name);
    }

    /**
     * This tests a good configuration scenario where the accessor methods to
     * access the DCC Component configs and the Auth config should work.
     *  
     */
    public void testCorrectConfiguration() throws URISyntaxException {
        String configFileLoc = ConfigurationTestSuite.configFileRoot + "config_good.xml";

        assertTrue(new File(ConfigurationTestSuite.schemaFileLoc).exists());
        assertTrue(new File(configFileLoc).exists());
        assertTrue(new File(ConfigurationTestSuite.digesterFileLoc).exists());
        
        DestinyConfigurationManagerImpl configMgr = new DestinyConfigurationManagerImpl(
                ConfigurationTestSuite.schemaFileLoc, configFileLoc, ConfigurationTestSuite.digesterFileLoc);
        
        // Access the auth config object and do some validation:
        IApplicationUserConfigurationDO applicationUserConfig = configMgr.getApplicationUserConfiguration();
        assertNotNull(applicationUserConfig);
        assertEquals(applicationUserConfig.getAuthenticationMode(), AuthenticationModeEnumType.LOCAL.getName());
        assertNotNull(applicationUserConfig.getUserRepositoryConfiguration());
        assertNotNull(applicationUserConfig.getExternalDomainConfiguration());
        assertNotNull(applicationUserConfig.getUserRepositoryConfiguration().getProviderClassName());
        assertNotNull(applicationUserConfig.getUserRepositoryConfiguration().getProperties());
        assertNotNull(applicationUserConfig.getExternalDomainConfiguration().getDomainName());
        assertNotNull(applicationUserConfig.getExternalDomainConfiguration().getAuthenticatorConfiguration());
        assertNotNull(applicationUserConfig.getExternalDomainConfiguration().getUserAccessConfiguration());
        assertNotNull(applicationUserConfig.getExternalDomainConfiguration().getAuthenticatorConfiguration().getAuthenticatorClassName());
        assertNotNull(applicationUserConfig.getExternalDomainConfiguration().getAuthenticatorConfiguration().getProperties());
        assertNotNull(applicationUserConfig.getExternalDomainConfiguration().getUserAccessConfiguration().getProviderClassName());
        assertNotNull(applicationUserConfig.getExternalDomainConfiguration().getUserAccessConfiguration().getProperties());

        // Access the repositories and do some verification:
        Set repositories = configMgr.getRepositories();
        assertNotNull(repositories);
        assertEquals(repositories.size(), 4);
        for (Iterator iter = repositories.iterator(); iter.hasNext();) {
            IRepositoryConfigurationDO repConf = (RepositoryConfigurationDO) iter.next();
            assertNotNull(repConf.getName());
            assertNotNull(repConf.getConnectionPoolConfiguration());

            IConnectionPoolConfigurationDO connectionPoolConf = repConf.getConnectionPoolConfiguration();
            assertNotNull(connectionPoolConf.getName());
            assertNotNull(connectionPoolConf.getJDBCConnectString());
            assertNotNull(connectionPoolConf.getDriverClassName());
            assertNotNull(connectionPoolConf.getUserName());
            assertNotNull(connectionPoolConf.getPassword());
        }

        // Access the DCC config objects and check each attribute:
        IDCCComponentConfigurationDO config = null;

        //DCSF:
        config = configMgr.getDCCConfiguration(ServerComponentType.DCSF);
        assertNotNull(config);
        assertTrue(config instanceof IDCSFComponentConfigurationDO);
        IDCSFComponentConfigurationDO dcsfConfig = (IDCSFComponentConfigurationDO) config;
        assertEquals(61, dcsfConfig.getHeartbeatInterval());

        //DMS:
        config = configMgr.getDCCConfiguration(ServerComponentType.DMS);
        assertNotNull(config);
        assertTrue(config instanceof IDMSComponentConfigurationDO);
        IDMSComponentConfigurationDO dmsConfig = (IDMSComponentConfigurationDO) config;
        assertEquals(62, dmsConfig.getHeartbeatInterval());

        //DABS:
        config = configMgr.getDCCConfiguration(ServerComponentType.DABS);
        assertNotNull(config);
        assertTrue(config instanceof IDABSComponentConfigurationDO);
        IDABSComponentConfigurationDO dabsConfig = (IDABSComponentConfigurationDO) config;
        assertEquals(63, dabsConfig.getHeartbeatInterval());

        //DPS:
        config = configMgr.getDCCConfiguration(ServerComponentType.DPS);
        assertNotNull(config);
        assertTrue(config instanceof IDPSComponentConfigurationDO);
        IDPSComponentConfigurationDO dpsConfig = (IDPSComponentConfigurationDO) config;
        assertEquals(64, dpsConfig.getHeartbeatInterval());
        assertEquals(85, dpsConfig.getLifecycleManagerGraceWindow());

        //DAC:
        config = configMgr.getDCCConfiguration(ServerComponentType.DAC);
        assertNotNull(config);
        assertTrue(config instanceof IDACComponentConfigurationDO);
        IDACComponentConfigurationDO dacConfig = (IDACComponentConfigurationDO) config;
        assertEquals(65, dacConfig.getHeartbeatInterval());

        //Management Console:
        config = configMgr.getDCCConfiguration(ServerComponentType.MGMT_CONSOLE);
        assertNotNull(config);
        assertTrue(config instanceof IMgmtConsoleComponentConfigurationDO);
        IMgmtConsoleComponentConfigurationDO mgmtConfig = (IMgmtConsoleComponentConfigurationDO) config;
        assertEquals(66, mgmtConfig.getHeartbeatInterval());
    }

    /**
     * This tests the following cases: (1) null values for file names, (2)
     * incorrect file names
     *  
     */
    public void testMissingConfigFile() {
        String[] configFileNames = new String[] { null, new String(ConfigurationTestSuite.configFileRoot + "some_missing_file.xml") };

        for (int i = 0; i < configFileNames.length; i++) {
            DestinyConfigurationManagerImpl configMgr = new DestinyConfigurationManagerImpl(ConfigurationTestSuite.schemaFileLoc, configFileNames[i], ConfigurationTestSuite.digesterFileLoc);

            // Verify that the config objects are all null:
            // Access the auth config object:
            IApplicationUserConfigurationDO authConfig = configMgr.getApplicationUserConfiguration();
            assertNull(authConfig);

            // Access a DCC config objects:
            IDCCComponentConfigurationDO dcsfConfig = configMgr.getDCCConfiguration(ServerComponentType.DCSF);
            assertNull(dcsfConfig);

            IDCCComponentConfigurationDO dmsConfig = configMgr.getDCCConfiguration(ServerComponentType.DMS);
            assertNull(dmsConfig);

            IDCCComponentConfigurationDO dabsConfig = configMgr.getDCCConfiguration(ServerComponentType.DABS);
            assertNull(dabsConfig);
        }
    }

    /**
     * This tests the case where the configuration file has some error(s).
     *  
     */
    public void testBadConfigFile() {
        try {
            String configFileLoc = ConfigurationTestSuite.configFileRoot + "config_neg_InvalidDataTypes.xml";

            DestinyConfigurationManagerImpl configMgr = new DestinyConfigurationManagerImpl(ConfigurationTestSuite.schemaFileLoc, configFileLoc, ConfigurationTestSuite.digesterFileLoc);

            // We should never reach here since an exception MUST be thrown:
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}