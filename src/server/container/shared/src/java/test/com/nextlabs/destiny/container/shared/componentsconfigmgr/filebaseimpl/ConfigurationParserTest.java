/*
 * Created on Dec 8, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.ConfigurationFileParser;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.ConfigurationTestFileGenerator.Part;

import junit.framework.TestCase;

/**
 * This class tests the Configuration Parser class and covers positive and
 * negative test cases.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class ConfigurationParserTest extends TestCase {

    /**
     * 
     * Default Constructor
     *  
     */
    public ConfigurationParserTest() {
        super();
    }

    /**
     * 
     * Overloaded Constructor
     * 
     * @param name
     */
    public ConfigurationParserTest(String name) {
        super(name);
    }

    /**
     * Validates the configuration parsed from a correctly specified config
     * file.
     *  
     */
    public void testCorrectConfiguration() {
        String configFileLoc = ConfigurationTestSuite.configFileRoot + "config_good.xml";
        testSemanticErrorCount(configFileLoc, 0);
    }

    /**
     * This verifies that an exception is thrown with the appropriate error
     * messages when there are missing elements in the configuration, or the
     * elements are out of order.
     *  
     */
    public void testMissingAndUnorderedElements() {
        String configFileLoc = ConfigurationTestSuite.configFileRoot + "config_neg_MissingAndUnorderedElements.xml";
        testSchemaErrorCount(configFileLoc, 1);
    }

    /**
     * This verifies that an exception is thrown with the appropriate error
     * messages when the configuration file contains invalid data. For example,
     * character data in an integer field etc.
     *  
     */
    public void testInvalidDataTypes() {
        String configFileLoc = ConfigurationTestSuite.configFileRoot + "config_neg_InvalidDataTypes.xml";
        testSchemaErrorCount(configFileLoc, 4);
    }

    /**
     * This verifies that an exception is thrown with the appropriate error
     * messages when the configuration file contains duplicate DCC config
     * objects.
     *  
     */
    public void testDuplicateDCCConfig() {
        String configFileLoc = ConfigurationTestSuite.configFileRoot + "config_neg_DuplicateDCCConfig.xml";
        testSchemaErrorCount(configFileLoc, 1);
    }

    /**
     * This verifies that an exception is thrown with the appropriate error
     * messages when the configuration file contains improper data source
     * configuration.
     *  
     */
    public void testImproperDataSources() {
        String configFileLoc = ConfigurationTestSuite.configFileRoot + "config_neg_ImproperDataSources.xml";
        testSemanticErrorCount(configFileLoc, 11);
    }

    /**
     * Generic method to do error count checking for schematic errors.
     * 
     * @param configFileLoc
     * @param nExpectedErrorCount
     */
    private void testSchemaErrorCount(String configFileLoc, int nExpectedErrorCount) {
        ConfigurationFileParser parser = new ConfigurationFileParser();
        Collection<String> errors = parser.validateConfig(
                ConfigurationTestSuite.schemaFileLoc
              , ConfigurationTestSuite.schemaNamespace
              , configFileLoc);

        // Validate number of expected errors:
        if (nExpectedErrorCount == 0) {
            if (errors != null && errors.size() != nExpectedErrorCount){
                fail("Schema errors should NOT have been encountered. Error messages = " 
                        + CollectionUtils.toString(errors));
            }
        } else {
            assertNotNull("Schematic errors were supposed to have been caught by this test", errors);
            assertEquals("Actual number of schematic errors does not match expected number of errors"
                    , nExpectedErrorCount, errors.size());
        }
    }

    /**
     * Generic method to do error count checking for semantic errors.
     * 
     * @param configFileLoc
     * @param nExpectedErrorCount
     */
    private void testSemanticErrorCount(String configFileLoc, int nExpectedErrorCount) {
        ConfigurationFileParser parser = new ConfigurationFileParser();
        Collection<String> schemaErrors = parser.validateConfig(
                ConfigurationTestSuite.schemaFileLoc
              , ConfigurationTestSuite.schemaNamespace
              , configFileLoc);
        assertTrue("Schema errors should NOT have been encountered", 
                (schemaErrors == null) || (schemaErrors.size() == 0));
        
        Collection<String> semanticErrors = parser.parseConfig(configFileLoc, ConfigurationTestSuite.digesterFileLoc);

        // Validate number of semantic errors:
        if (nExpectedErrorCount == 0) {
            if( semanticErrors != null && semanticErrors.size() != nExpectedErrorCount ){
                fail("Semantic errors should NOT have been encountered. Error messages = " 
                        + CollectionUtils.toString(semanticErrors));
            }
        } else {
            assertNotNull("Semantic errors were supposed to have been caught by this test", semanticErrors);
            assertEquals("Actual number of semantic errors does not match expected number of errors"
                    , nExpectedErrorCount, semanticErrors.size());
        }
    }
    
    
    
    
    
    private static void save(String string, File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        try {
            writer.write(string);
        } finally {
            writer.close();
        }
    }
    
    
    private void check(Collection<String> errors, boolean isGood, String description) {
        if (isGood) {
            if (errors != null && !errors.isEmpty()) {
                fail(description + "\n" + CollectionUtils.toString(errors));
            }
        } else {
            assertNotNull(description, errors);
            System.out.println(description);
            System.out.println(CollectionUtils.toString(errors));
            assertFalse(description, errors.isEmpty());
        }
    }
    
    private void validation(ConfigurationFileParser parser, File dataFile, boolean isGood, String description) {
        Collection<String> errors = parser.validateConfig(
                new File(ConfigurationTestSuite.schemaFileLoc)
              , ConfigurationTestSuite.schemaNamespace
              , dataFile
        );
        
        check(errors, isGood, description);
    }
    
    private void parse(ConfigurationFileParser parser, File dataFile, boolean isGood, String description) {
        Collection<String> errors = parser.parseConfig(
                dataFile
              , new File(ConfigurationTestSuite.digesterFileLoc)
        );
        
        check(errors, isGood, description);
    }

    public void testGeneratorOk() throws IOException {
        final ConfigurationFileParser parser = new ConfigurationFileParser();
        File tempFile = File.createTempFile(ConfigurationParserTest.class.getSimpleName(), "xml");
        String data = ConfigurationTestFileGenerator.generate();
        save(data, tempFile);
        validation(parser, tempFile, true, "");
        parse(parser, tempFile, true, "");
    }
    
    public void testGeneratorMissingPart() throws IOException {

        for (Part p : Part.values()) {
            
            ConfigurationFileParser parser = new ConfigurationFileParser();
            StringBuilder sb = new StringBuilder(ConfigurationTestFileGenerator.HEADER);

            String description = "missing " + p.name();
            for (Part p2 : Part.values()) {
                if( p != p2){
                    sb.append(p2.getContent());
                }
            }
            
            sb.append(ConfigurationTestFileGenerator.FOOTER);
            
            File tempFile = File.createTempFile(ConfigurationParserTest.class.getSimpleName(), "xml");
            save(sb.toString(), tempFile);
            if (p.optional) {
                validation(parser, tempFile, true, description);
                parse(parser, tempFile, true, description);
            }else{
                validation(parser, tempFile, false, description);
                parse(parser, tempFile, true, description);
            }
        }
    }
    
    public void testGenericComponentAnyOrder() throws IOException {

        Part previousPart = null;
        for (Part p : Part.values()) {
            ConfigurationFileParser parser = new ConfigurationFileParser();
            StringBuilder sb = new StringBuilder(ConfigurationTestFileGenerator.HEADER);

            String description = "add generic components after " + previousPart;
            for (Part p2 : Part.values()) {
                if( p == p2){
                    sb.append("<GenericComponents />");
                }
                sb.append(p2.getContent());
            }
            sb.append(ConfigurationTestFileGenerator.FOOTER);
            
            File tempFile = File.createTempFile(ConfigurationParserTest.class.getSimpleName(), "xml");
            save(sb.toString(), tempFile);
            validation(parser, tempFile, true, description);
            parse(parser, tempFile, true, description);
            
            previousPart = p;
        }
    }
    
    public void testAppearTwice() throws IOException {
        for (Part p : Part.values()) {
            ConfigurationFileParser parser = new ConfigurationFileParser();
            StringBuilder sb = new StringBuilder(ConfigurationTestFileGenerator.HEADER);

            String description = "duplicate  " + p.name();
            for (Part p2 : Part.values()) {
                if( p == p2){
                    sb.append(p2.getContent());
                }
                sb.append(p2.getContent());
            }
            sb.append(ConfigurationTestFileGenerator.FOOTER);
            
            File tempFile = File.createTempFile(ConfigurationParserTest.class.getSimpleName(), "xml");
            save(sb.toString(), tempFile);
            validation(parser, tempFile, false, description);
            
            switch(p){
            case DMS:
            case DCSF:
            case DABS:
            case DPS:
            case DAC:
            case DEM:
            case ManagementConsole:
            case Reporter:
            case Repositories :
                //those things can't be duplicated
                parse(parser, tempFile, false, description);
                break;
            default:
                parse(parser, tempFile, true, description);
                break;
            }
        }
    }
    
    ConfigurationFileParser testGenericComponents(String genericComponents, boolean isValidationGood, boolean isParserGood) throws IOException {
        ConfigurationFileParser parser = new ConfigurationFileParser();
        StringBuilder sb = new StringBuilder(ConfigurationTestFileGenerator.HEADER);

        for (Part p2 : Part.values()) {
            sb.append(p2.getContent());
        }
        
        sb.append(genericComponents);
        
        sb.append(ConfigurationTestFileGenerator.FOOTER);
        
        File tempFile = File.createTempFile(ConfigurationParserTest.class.getSimpleName(), "xml");
        String data = sb.toString();
        save(data, tempFile);
        validation(parser, tempFile, isValidationGood, "");
        parse(parser, tempFile, isParserGood, "");
        return parser;
    }
    
    public void testOneGenericComponents() throws IOException {
        testGenericComponents("<GenericComponents>" 
                + "<GenericComponent name=\"abc\">"
                + "<Properties/><HeartbeatRate>15</HeartbeatRate>"
                + "</GenericComponent>"
                + "</GenericComponents>"
              , true
              , true
        );
    }
    
    public void testMultiGenericComponents() throws IOException {
        final String namePrefix = "COMP";
        final int numberOfComponents = 4; // magic number
        StringBuffer sb = new StringBuffer();
        sb.append("<GenericComponents>");
        
        for(int i =1; i<= numberOfComponents; i++){
            sb.append("<GenericComponent name=\"").append(namePrefix).append(i).append("\">");
            
            if( i % 2 == 0){
                sb.append("<Properties/>");
            }
            
            sb.append("<HeartbeatRate>").append(300 - i).append("</HeartbeatRate>")
              .append("</GenericComponent>");
        }
        sb.append("</GenericComponents>");
        
        ConfigurationFileParser parser = testGenericComponents(sb.toString()
              , true
              , true
        );
        
        for(int i =1; i<= numberOfComponents; i++){
            ServerComponentType type = ServerComponentType.fromString(namePrefix + i);
            IDCCComponentConfigurationDO config = parser.getDCCConfig(type);
            assertNotNull(config);
            assertEquals(300 - i, config.getHeartbeatInterval());
        }
    }
    
    public void testMissingNameGenericComponents() throws IOException {
        testGenericComponents("<GenericComponents>" 
                + "<GenericComponent>"
                + "<Properties/><HeartbeatRate>15</HeartbeatRate>"
                + "</GenericComponent>"
                + "</GenericComponents>"
              , false
              , true
        );
    }
    
    public void testGenericComponentWrongElementOrder() throws IOException {
        testGenericComponents("<GenericComponents>" 
                + "<GenericComponent name=\"ORDER\">"
                + "<HeartbeatRate>15</HeartbeatRate>"
                + "<Properties/>"
                + "</GenericComponent>"
                + "</GenericComponents>"
              , false
              , true
        );
    }
    
    public void testDuplicatedNameGenericComponents() throws IOException {
        testGenericComponents("<GenericComponents>" 
                + "<GenericComponent name=\"one\">"
                + "<Properties/><HeartbeatRate>15</HeartbeatRate>"
                + "</GenericComponent>"
                + "<GenericComponent name=\"ONE\">"
                + "<Properties/><HeartbeatRate>15</HeartbeatRate>"
                + "</GenericComponent>"
                + "</GenericComponents>"
              , true
              , false
        );
    }
    
    public void testGenericComponentProperties() throws IOException {
        ConfigurationFileParser parser = testGenericComponents("<GenericComponents>" 
                + "<GenericComponent name=\"PROP\">"
                + "<Properties>"
                + "<Property><Name>a</Name><Value>1</Value></Property>"
                + "<Property><Name>b</Name><Value>2</Value></Property>"
                + "<Property><Name>c</Name><Value>3</Value></Property>"
                + "</Properties>"
                + "<HeartbeatRate>15</HeartbeatRate>"
                + "</GenericComponent>"
                + "</GenericComponents>"
              , true
              , true
        );
        
        ServerComponentType type = ServerComponentType.fromString("PROP");
        IDCCComponentConfigurationDO config = parser.getDCCConfig(type);
        assertNotNull(config);
        Properties properties = config.getProperties();
        assertNotNull(properties);
        assertEquals(3, properties.size());
        assertEquals("1", properties.get("a"));
        assertEquals("2", properties.get("b"));
        assertEquals("3", properties.get("c"));
    }
    
    public void testProperties() throws IOException {
        ConfigurationFileParser parser = new ConfigurationFileParser();
        StringBuilder sb = new StringBuilder(ConfigurationTestFileGenerator.HEADER);

        for (Part p2 : Part.values()) {
            sb.append(p2.getContent());
        }
        
        sb.append(ConfigurationTestFileGenerator.FOOTER);
        
        File tempFile = File.createTempFile(ConfigurationParserTest.class.getSimpleName(), "xml");
        String data = sb.toString();
        save(data, tempFile);
        validation(parser, tempFile, true, "");
        parse(parser, tempFile, true, "");
    }

}