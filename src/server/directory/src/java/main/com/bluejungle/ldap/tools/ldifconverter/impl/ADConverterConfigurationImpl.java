/*
 * Created on Apr 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/ADConverterConfigurationImpl.java#1 $
 */

public class ADConverterConfigurationImpl implements IADConverterConfiguration {

    private static Log LOG = LogFactory.getLog(ADConverterConfigurationImpl.class);

    /*
     * Private variables:
     */
    private Properties properties;
    private String[] attributesWithDNValues;
    private Map userMappings;
    private Map computerMappings;
    private Map structMappings;
    private Map enumMappings;
    private Map defaultMappings;

    /*
     * Property names:
     */
    private static final String ATTRIBUTES_OF_DN_TYPE = "openldap.attributes.of.dn.type";
    private static final String OPTIONAL_USER_ATTRIBUTES = "user.optional.attribute.mappings";
    private static final String OPTIONAL_COMPUTER_ATTRIBUTES = "computer.optional.attribute.mappings";
    private static final String OPTIONAL_STRUCT_ATTRIBUTES = "container.optional.attribute.mappings";
    private static final String OPTIONAL_ENUM_ATTRIBUTES = "group.optional.attribute.mappings";
    private static final String OPTIONAL_DEFAULT_ATTRIBUTES = "default.optional.attribute.mappings";

    /**
     * Constructor
     * 
     * @param propertiesFileName
     * @throws IOException
     */
    public ADConverterConfigurationImpl(String configFile) throws IOException {
        this.properties = new Properties();
        this.properties.load(new FileInputStream(configFile));

        // Read all the information:

        // DN attributes:
        String attrString = properties.getProperty(ATTRIBUTES_OF_DN_TYPE);
        String[] attrs = attrString.split(",");
        for (int i = 0; i < attrs.length; i++) {
            attrs[i] = attrs[i].trim();
        }
        this.attributesWithDNValues = attrs;

        // Mappings:
        this.userMappings = this.getMappings(OPTIONAL_USER_ATTRIBUTES);
        this.computerMappings = this.getMappings(OPTIONAL_COMPUTER_ATTRIBUTES);
        this.structMappings = this.getMappings(OPTIONAL_STRUCT_ATTRIBUTES);
        this.enumMappings = this.getMappings(OPTIONAL_ENUM_ATTRIBUTES);
        this.defaultMappings = this.getMappings(OPTIONAL_DEFAULT_ATTRIBUTES);
    }

    /**
     * @see com.bluejungle.ldap.tools.ldifconverter.IConverterConfiguration#getAttributesWithDNValues()
     */
    public String[] getAttributesWithDNValues() {
        return this.attributesWithDNValues;
    }

    /**
     * Parses the property with format : "AD/OpenLDAP, AD/OpenLDAP"
     * 
     * @param property
     * @return
     */
    private Map getMappings(String property) {
        Map mappings = new HashMap();
        String attrString = properties.getProperty(property);
        if (attrString != null) {
            String[] mappingArr = attrString.split(",");
            if (mappingArr.length > 0) {
                for (int i = 0; i < mappingArr.length; i++) {
                    mappingArr[i] = mappingArr[i].trim();
                    String[] tuple = mappingArr[i].split("/");
                    if (tuple.length == 2) {
                        mappings.put(tuple[0].trim(), tuple[1].trim());
                    } else {
                        LOG.warn("Dropping attribute mapping - '" + mappingArr[i] + "' since it is in an invalid format.");
                    }
                }
            } else {
                // No mappings were specified:
                LOG.info("No mappings were specified for '" + property + "'. Proceeding to next entry");
                LOG.debug("Attribute '" + property + "' had value : " + attrString);
            }
        }
        return mappings;
    }

    /**
     * Extracts the AD->OpenLDAP mappings for user entries
     * 
     * @return optional attribute mappings
     */
    public Map getOptionalUserAttributes() {
        return this.userMappings;
    }

    /**
     * Extracts the AD->OpenLDAP mappings for computer entries
     * 
     * @return optional attribute mappings
     */
    public Map getOptionalComputerAttributeMappings() {
        return this.computerMappings;
    }

    /**
     * Extracts the AD->OpenLDAP mappings for struct entries
     * 
     * @return optional attribute mappings
     */
    public Map getOptionalStructuralGroupAttributeMappings() {
        return this.structMappings;
    }

    /**
     * Extracts the AD->OpenLDAP mappings for group entries
     * 
     * @return optional attribute mappings
     */
    public Map getOptionalEnumeratedGroupAttributeMappings() {
        return this.enumMappings;
    }

    /**
     * Extracts the AD->OpenLDAP mappings for default (all other) entries
     * 
     * @return optional attribute mappings
     */
    public Map getOptionalDefaultAttributeMappings() {
        return this.defaultMappings;
    }
}