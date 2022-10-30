/*
 * Created on Dec 7, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class specifies the test execution order of the Destiny Configuration
 * tests.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class ConfigurationTestSuite {

    public static final String schemaNamespace = "http://bluejungle.com/destiny/services/management/types";
    public static final String schemaFileLoc;
    public static final String digesterFileLoc;
    public static final String configFileRoot;

    private static final String DESTINY_INSTALL_PATH_PROPERTY_NAME = "build.root.dir";
    private static final String DEFAULT_DESTINY_INSTALL_PATH = "C:/builds/destiny";

    static {
        String installPath = System.getProperty(DESTINY_INSTALL_PATH_PROPERTY_NAME);
        if (installPath == null) {
            System.out.println("The property '"
                    + DESTINY_INSTALL_PATH_PROPERTY_NAME
                    + "' is not set. Using default value '"
                    + DEFAULT_DESTINY_INSTALL_PATH + "'.");
            installPath = DEFAULT_DESTINY_INSTALL_PATH;
        }
        
        schemaFileLoc = installPath + "/run/server/configuration/Configuration.xsd";
        digesterFileLoc = installPath + "/run/server/configuration/configuration.digester.rules.xml";
        configFileRoot = installPath + "/run/server/configuration/test/";
    }

     /**
     * Returns the configuration manager test suite
     * 
     * @return the configuration manager test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.server.shared.configuration");

        // Configuration Parser test cases:
        suite.addTest(new ConfigurationParserTest("testCorrectConfiguration"));
        suite.addTest(new ConfigurationParserTest("testMissingAndUnorderedElements"));
        suite.addTest(new ConfigurationParserTest("testInvalidDataTypes"));
        suite.addTest(new ConfigurationParserTest("testDuplicateDCCConfig"));
        suite.addTest(new ConfigurationParserTest("testImproperDataSources"));

        // Configuration Manager test cases:
        suite.addTest(new ConfigurationManagerTest("testCorrectConfiguration"));
        suite.addTest(new ConfigurationManagerTest("testMissingConfigFile"));
        suite.addTest(new ConfigurationManagerTest("testBadConfigFile"));

        return (suite);
    }
}