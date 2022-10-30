/*
 * Created on Sep 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.auth.jaas;

import java.util.Properties;

import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/auth/jaas/PropertiesToJAASConfigurationConverter.java#1 $
 */

public class PropertiesToJAASConfigurationConverter {

    public static final String KDC_CONFIG_PARAM = "java.security.krb5.kdc";
    public static final String REALM_CONFIG_PARAM = "java.security.krb5.realm";

    /**
     * Constructor
     */
    private PropertiesToJAASConfigurationConverter() {
        super();
    }

    /**
     * Extracts the configuration interface from the given properties object
     * 
     * @param properties
     * @return configuration interface
     * @throws InvalidConfigurationException
     */
    public static IJAASConfiguration extractJAASConfiguration(Properties properties) throws InvalidConfigurationException {
        JAASConfigurationImpl config = new JAASConfigurationImpl(properties);
        config.validate();
        return config;
    }

    /**
     * Wrapper to convert properties to an IJAASConfiguration interface.
     * 
     * @author safdar
     */
    public static class JAASConfigurationImpl implements IJAASConfiguration {

        private Properties properties;

        /**
         * Constructor
         * 
         * @param properties
         */
        public JAASConfigurationImpl(Properties properties) {
            if (properties == null) {
                throw new NullPointerException("properties is null");
            }
            this.properties = properties;
        }

        /**
         * Validates the configuration properties
         * 
         * @throws InvalidConfigurationException
         */
        public void validate() throws InvalidConfigurationException {
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.auth.jaas.IJAASConfiguration#getKDC()
         */
        public String getKDC() {
            return this.properties.getProperty(KDC_CONFIG_PARAM);
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.auth.jaas.IJAASConfiguration#getRealm()
         */
        public String getRealm() {
            return this.properties.getProperty(REALM_CONFIG_PARAM);
        }
    }
}