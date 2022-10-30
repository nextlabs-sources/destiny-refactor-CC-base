/*
 * Created on Jul 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.MissingConfigurationPropertyException;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/PropertiesToConfigurationConverter.java#1 $
 */

public class PropertiesToConfigurationConverter {

    /*
     * Property names:
     */
    public static final String SERVER = "server.name";
    public static final String PORT = "server.port";
    public static final String ROOT_DN = "root.dn";
    public static final String LOGIN_DN = "login.dn";
    public static final String LOGIN_PWD = "login.password";
    public static final String USE_SSL = "useSSL";
    public static final String USER_SEARCH_SPEC = "user.searchspec";
    public static final String USER_FIRSTNAME_ATTR = "user.attributefor.firstname";
    public static final String USER_LASTNAME_ATTR = "user.attributefor.lastname";
    public static final String USER_LOGIN_ATTR = "user.attributefor.login";
    public static final String GLOBALLY_UNIQUE_IDENTIFIER_ATTR = "attributefor.globallyuniqueidentifier";
    public static final String STRUCTURAL_GROUP_SEARCH_SPEC_PREFIX = "structural.group.searchspec";
    public static final String STRUCTURAL_GROUP_TITLE_ATTR_PREFIX = "structural.group.attributefor.title";
    public static final String ENUMERATED_GROUP_SEARCH_SPEC_PREFIX = "enumerated.group.searchspec";
    public static final String ENUMERATED_GROUP_MEMBERSHIP_ATTR_PREFIX = "enumerated.group.attributefor.membership";
    public static final String ENUMERATED_GROUP_TITLE_ATTR_PREFIX = "enumerated.group.attributefor.title";

    private PropertiesToConfigurationConverter() {
    }

    /**
     * Validates and converts a property list to a configuration interface
     * 
     * @param properties
     * @return validated configuration interface instance
     * @throws InvalidConfigurationException
     */
    public static ILDAPAccessProviderConfiguration extractConfiguration(Properties properties) throws InvalidConfigurationException {
        LDAPAccessProviderConfiguration config = new LDAPAccessProviderConfiguration(properties);
        return config;
    }

    /**
     * Wrapper class that is an implementation of the configuration interface
     * that can also validate the configuration properties
     * 
     * @author safdar
     */
    private static class LDAPAccessProviderConfiguration implements ILDAPAccessProviderConfiguration {

        private Properties properties;
        private String userFirstNameAttribute;
        private String userLastNameAttribute;
        private String userLoginAttribute;
        private String loginDN;
        private String loginPwd;
        private int port;
        private String rootDN;
        private String server;
        private String userSearchSpec;
        private boolean usingSSL;
        private String globallyUniqueIdentifierAttribute;
        private Collection<ILDAPEnumeratedGroupConfiguration> enumeratedGroupConfigurations;
        private Collection<ILDAPStructuralGroupConfiguration> structuralGroupConfigurations;

        /**
         * Returns the desired property value
         * 
         * @param propertyName
         * @return property value
         * @throws MissingConfigurationPropertyException
         */
        private String getProperty(String propertyName) throws MissingConfigurationPropertyException {
            String propertyValue = this.properties.getProperty(propertyName);
            if (propertyValue == null || propertyValue.trim().equals("")) {
                throw new MissingConfigurationPropertyException("No value specified for configuration property: '" + propertyName + "'");
            }
            return propertyValue.trim();
        }

        public LDAPAccessProviderConfiguration(Properties properties) throws InvalidConfigurationException {
            this.properties = properties;

            try {
                this.userFirstNameAttribute = getProperty(USER_FIRSTNAME_ATTR);
                this.loginDN = getProperty(LOGIN_DN);
                this.loginPwd = new ReversibleEncryptor().decrypt(getProperty(LOGIN_PWD));
                this.userLoginAttribute = getProperty(USER_LOGIN_ATTR);
                this.port = new Integer(getProperty(PORT)).intValue();
                this.rootDN = getProperty(ROOT_DN);
                this.server = getProperty(SERVER);
                this.userSearchSpec = getProperty(USER_SEARCH_SPEC);
                this.usingSSL = new Boolean(getProperty(USE_SSL)).booleanValue();
                this.userLastNameAttribute = getProperty(USER_LASTNAME_ATTR);
                this.globallyUniqueIdentifierAttribute = getProperty(GLOBALLY_UNIQUE_IDENTIFIER_ATTR);

                // Read the enumerated group configurations:
                boolean moreEnumAttributes = true;
                this.enumeratedGroupConfigurations = new HashSet<ILDAPEnumeratedGroupConfiguration>();
                for (int i = 1; moreEnumAttributes; i++) {
                    String searchSpecProperty = ENUMERATED_GROUP_SEARCH_SPEC_PREFIX + i;
                    String searchSpecValue = this.properties.getProperty(searchSpecProperty);
                    if (searchSpecValue != null) {
                        String titleAttrProperty = ENUMERATED_GROUP_TITLE_ATTR_PREFIX + i;
                        String titleAttrValue = getProperty(titleAttrProperty);
                        if (titleAttrValue != null) {
                            String memberAttrProperty = ENUMERATED_GROUP_MEMBERSHIP_ATTR_PREFIX + i;
                            String memberAttrValue = getProperty(memberAttrProperty);
                            if (memberAttrValue != null) {
                                EnumeratedGroupConfigurationImpl config = new EnumeratedGroupConfigurationImpl(searchSpecValue, titleAttrValue, memberAttrValue);
                                this.enumeratedGroupConfigurations.add(config);
                            }
                        }
                    } else {
                        moreEnumAttributes = false;
                    }
                }

                // Read the structural group configurations:
                boolean moreStructAttributes = true;
                this.structuralGroupConfigurations = new HashSet<ILDAPStructuralGroupConfiguration>();
                for (int i = 1; moreStructAttributes; i++) {
                    String searchSpecProperty = STRUCTURAL_GROUP_SEARCH_SPEC_PREFIX + i;
                    String searchSpecValue = this.properties.getProperty(searchSpecProperty);
                    if (searchSpecValue != null) {
                        String titleAttrProperty = STRUCTURAL_GROUP_TITLE_ATTR_PREFIX + i;
                        String titleAttrValue = getProperty(titleAttrProperty);
                        if (titleAttrValue != null) {
                            StructuralGroupConfigurationImpl config = new StructuralGroupConfigurationImpl(searchSpecValue, titleAttrValue);
                            this.structuralGroupConfigurations.add(config);
                        }
                    } else {
                        moreStructAttributes = false;
                    }
                }

            } catch (MissingConfigurationPropertyException e) {
                throw new InvalidConfigurationException(e);
            }

        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getUserFirstNameAttribute()
         */
        public String getUserFirstNameAttribute() {
            return this.userFirstNameAttribute;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getLoginDN()
         */
        public String getLoginDN() {
            return this.loginDN;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getUserLoginAttribute()
         */
        public String getUserLoginAttribute() {
            return this.userLoginAttribute;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getLoginPwd()
         */
        public String getLoginPwd() {
            return this.loginPwd;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getPort()
         */
        public int getPort() {
            return this.port;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getRootDN()
         */
        public String getRootDN() {
            return this.rootDN;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getServer()
         */
        public String getServer() {
            return this.server;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getUserSearchSpec()
         */
        public String getUserSearchSpec() {
            return this.userSearchSpec;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#isUsingSSL()
         */
        public boolean isUsingSSL() {
            return this.usingSSL;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getUserLastNameAttribute()
         */
        public String getUserLastNameAttribute() {
            return this.userLastNameAttribute;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getGloballyUniqueIdentifierAttribute()
         */
        public String getGloballyUniqueIdentifierAttribute() {
            return this.globallyUniqueIdentifierAttribute;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getEnumeratedGroupConfigurations()
         */
        public Collection<ILDAPEnumeratedGroupConfiguration> getEnumeratedGroupConfigurations() {
            return this.enumeratedGroupConfigurations;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPAccessProviderConfiguration#getStructuralGroupConfigurations()
         */
        public Collection<ILDAPStructuralGroupConfiguration> getStructuralGroupConfigurations() {
            return this.structuralGroupConfigurations;
        }

        /**
         * Implementation for enumerated group configuration
         * 
         * @author safdar
         */
        private class EnumeratedGroupConfigurationImpl implements ILDAPEnumeratedGroupConfiguration {

            private String searchSpec;
            private String attributeForTitle;
            private String attributeForMembership;

            /**
             * Constructor
             * 
             * @param searchSpec
             * @param attributeForTitle
             * @param attributeForMembership
             */
            public EnumeratedGroupConfigurationImpl(String searchSpec, String attributeForTitle, String attributeForMembership) {
                this.searchSpec = searchSpec;
                this.attributeForTitle = attributeForTitle;
                this.attributeForMembership = attributeForMembership;
            }

            /**
             * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPEnumeratedGroupConfiguration#getMembershipAttribute()
             */
            public String getMembershipAttribute() {
                return this.attributeForMembership;
            }

            /**
             * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPEnumeratedGroupConfiguration#getSearchSpec()
             */
            public String getSearchSpec() {
                return this.searchSpec;
            }

            /**
             * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPEnumeratedGroupConfiguration#getTitleAttribute()
             */
            public String getTitleAttribute() {
                return this.attributeForTitle;
            }
        }

        /**
         * Implementation for structural group configuration
         * 
         * @author safdar
         */
        private class StructuralGroupConfigurationImpl implements ILDAPStructuralGroupConfiguration {

            private String searchSpec;
            private String attributeForTitle;

            public StructuralGroupConfigurationImpl(String searchSpec, String attributeForTitle) {
                this.searchSpec = searchSpec;
                this.attributeForTitle = attributeForTitle;
            }

            /**
             * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPStructuralGroupConfiguration#getSearchSpec()
             */
            public String getSearchSpec() {
                return this.searchSpec;
            }

            /**
             * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPStructuralGroupConfiguration#getTitleAttribute()
             */
            public String getTitleAttribute() {
                return this.attributeForTitle;
            }
        }
    }
}
