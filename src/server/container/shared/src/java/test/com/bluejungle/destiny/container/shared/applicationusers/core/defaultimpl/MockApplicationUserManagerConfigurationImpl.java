/*
 * Created on Sep 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import java.util.Properties;

import com.bluejungle.destiny.container.shared.applicationusers.auth.jaas.KrbAuthMgrImpl;
import com.bluejungle.destiny.container.shared.applicationusers.auth.jaas.PropertiesToJAASConfigurationConverter;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.LDAPAccessProvider;
import com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.PropertiesToConfigurationConverter;
import com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository;
// TODO change to hibernate application user 
// import com.bluejungle.destiny.container.shared.applicationusers.repository.openldapimpl.OpenLDAPUserRepositoryPropertiesToConfigurationConverter;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IUserRepositoryConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockApplicationUserManagerConfigurationImpl.java#6 $
 */

public class MockApplicationUserManagerConfigurationImpl implements IApplicationUserConfigurationDO {

    private String authenticationMode;

    /**
     * User repository configuration for testing
     */
    public static final String USER_REPOSITORY_ACCESS_PROVIDER = HibernateApplicationUserRepository.class.getName();
    public static final String USER_REPOSITORY_LOCAL_DOMAIN_NAME = "Local";
    public static final Properties USER_REPOSITORY_PROPS_FOR_TEST;
    static {
        USER_REPOSITORY_PROPS_FOR_TEST = new Properties();
        /*
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.SERVER_NAME, "localhost");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.SERVER_PORT, "389");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.USE_SSL, "false");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.ROOT_DN, "ou=User Management,dc=DestinyData,dc=Destiny,dc=com");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.LOGIN_DN, "cn=Manager,dc=Destiny,dc=com");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.LOGIN_PASSWD, "637c495852634e7c7a493a581a76304515704263");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.DOMAIN_NAME_ATTRIBUTE, "cn");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.DOMAIN_SEARCH_SPEC, "(objectClass=ApplicationUserDomain)");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.USER_LOGIN_ATTRIBUTE, "login");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.USER_FIRSTNAME_ATTRIBUTE, "fn");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.USER_LASTNAME_ATTRIBUTE, "ln");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.USER_DESTINYID_ATTRIBUTE, "destinyID");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.USER_PRIMARYACCESSGROUPID_ATTRIBUTE, "primaryAccessGroupId");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.USER_PASSWORD_ATTRIBUTE, "userPassword");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.USER_SEARCH_SPEC, "(objectClass=ApplicationUser)");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.INTERNAL_GROUP_SEARCH_SPEC, "(objectClass=InternalAccessGroup)");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.LINKED_GROUP_SEARCH_SPEC, "(objectClass=LinkedAccessGroup)");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.GROUP_TITLE_ATTRIBUTE, "title");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.GROUP_DESCRIPTION_ATTRIBUTE, "description");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.GROUP_DESTINYID_ATTRIBUTE, "destinyID");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.GROUP_ACCESS_CONTROL_ATTRIBUTE, "acl");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.INTERNAL_GROUP_MEMBERSHIP_ATTRIBUTE, "member");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.LINKED_GROUP_EXTERNALID_ATTRIBUTE, "externalID");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.LINKED_GROUP_ORPHANFLAG_ATTRIBUTE, "isOrphaned");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.LOCAL_DOMAIN_NAME, "Local");
        USER_REPOSITORY_PROPS_FOR_TEST.setProperty(OpenLDAPUserRepositoryPropertiesToConfigurationConverter.ENCRYPTION_ALGO_NAME, "sha");
        */
    }

    /**
     * External domain authentication configuration for testing
     */
    public static final String EXTERNAL_DOMAIN_NAME = "test.bluejungle.com";

    public static final String EXTERNAL_DOMAIN_ACCESS_PROVIDER = LDAPAccessProvider.class.getName();
    public static final Properties EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST;
    static {
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST = new Properties();
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.SERVER, "linuxad01.linuxtest.bluejungle.com");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.PORT, "389");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.ROOT_DN, "ou=Presidents,dc=linuxtest,dc=bluejungle,dc=com");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.LOGIN_DN, "cn=Jimmy Carter,ou=Users,ou=Presidents,dc=linuxtest,dc=bluejungle,dc=com");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.LOGIN_PWD, "6b5a4461016b335a29440c61275e436d1e7d3b5a3042304430551765");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.USE_SSL, "false");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.USER_FIRSTNAME_ATTR, "givenName");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.USER_LASTNAME_ATTR, "sn");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.USER_LOGIN_ATTR, "sAMAccountName");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.USER_SEARCH_SPEC, "(&(objectClass=user)(!(objectClass=computer)))");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.GLOBALLY_UNIQUE_IDENTIFIER_ATTR, "objectGUID");

        // Create configuration for the structural groups:
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.STRUCTURAL_GROUP_SEARCH_SPEC_PREFIX + "1", "(objectClass=organization)");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.STRUCTURAL_GROUP_TITLE_ATTR_PREFIX + "1", "o");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.STRUCTURAL_GROUP_SEARCH_SPEC_PREFIX + "2", "(objectClass=organizationalUnit)");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.STRUCTURAL_GROUP_TITLE_ATTR_PREFIX + "2", "ou");

        // Create configuration for the enumerated groups:
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.ENUMERATED_GROUP_SEARCH_SPEC_PREFIX + "1", "(objectClass=group)");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.ENUMERATED_GROUP_TITLE_ATTR_PREFIX + "1", "cn");
        EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST.setProperty(PropertiesToConfigurationConverter.ENUMERATED_GROUP_MEMBERSHIP_ATTR_PREFIX + "1", "member");
    }

    public static final String EXTERNAL_DOMAIN_AUTHENTICATOR = KrbAuthMgrImpl.class.getName();
    public static final Properties EXTERNAL_DOMAIN_AUTH_PROPS_FOR_TEST;
    static {
        EXTERNAL_DOMAIN_AUTH_PROPS_FOR_TEST = new Properties();
        EXTERNAL_DOMAIN_AUTH_PROPS_FOR_TEST.setProperty(PropertiesToJAASConfigurationConverter.KDC_CONFIG_PARAM, "linuxad01.linuxtest.bluejungle.com");
        EXTERNAL_DOMAIN_AUTH_PROPS_FOR_TEST.setProperty(PropertiesToJAASConfigurationConverter.REALM_CONFIG_PARAM, "LINUXTEST.BLUEJUNGLE.COM");
    }

    /**
     * Constructor
     *  
     */
    public MockApplicationUserManagerConfigurationImpl(AuthenticationModeEnumType authenticationMode) {
        super();
        this.authenticationMode = authenticationMode.getName();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerConfiguration#getAuthenticationMode()
     */
    public String getAuthenticationMode() {
        return this.authenticationMode;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerConfiguration#getLocalRepositoryConfiguration()
     */
    public IUserRepositoryConfigurationDO getUserRepositoryConfiguration() {
        return new MockUserRepositoryProviderConfigurationImpl(USER_REPOSITORY_ACCESS_PROVIDER, USER_REPOSITORY_PROPS_FOR_TEST);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerConfiguration#getExternalDomainConfigurations()
     */
    public IExternalDomainConfigurationDO getExternalDomainConfiguration() {
        return new MockExternalDomainConfigurationImpl(EXTERNAL_DOMAIN_NAME, EXTERNAL_DOMAIN_ACCESS_PROVIDER, EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST, EXTERNAL_DOMAIN_AUTHENTICATOR, EXTERNAL_DOMAIN_AUTH_PROPS_FOR_TEST);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerConfiguration#getSuperUserLogin()
     */
    public String getSuperUserLogin() {
        return "Administrator";
    }
}
