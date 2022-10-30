/*
 * Created on Feb 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.auth.jaas;

import java.util.HashMap;
import java.util.Properties;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext;
import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.sun.security.auth.module.Krb5LoginModule;

/**
 * This is the authentication manager implementation for Kerberos. The
 * authentication manager retrieves configuration parameters during the
 * initialization phase (location of the KDC, default realm, etc.) Then, the
 * authentication manager creates a security context that will be used to test
 * user credentials.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/auth/KrbAuthMgrImpl.java#2 $
 */

public class KrbAuthMgrImpl implements IAuthenticator {

    private static final Log LOG = LogFactory.getLog(KrbAuthMgrImpl.class);
    public static final String COMP_NAME = "AuthMgr";

    private IJAASConfiguration configuration;

    public IAuthenticationContext authenticate(final String login, final String password) throws AuthenticationFailedException {
        IAuthenticationContext ctx = null;
        try {
            CredentialCallBackHandler callback = new CredentialCallBackHandler(login, password);
            LoginContext lc = new LoginContext(ApplicationUserConfiguration.DESTINY_CONTEXT, callback);
            lc.login();
            ctx = new KrbAuthenticationContext(lc);
        } catch (LoginException e) {
            LOG.info("Login failed for user '" + login + "':" + e.getMessage(), e);
            throw new AuthenticationFailedException(e);
        }
        return ctx;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator#initialize(java.util.Properties)
     */
    public void initialize(Properties properties) throws InvalidConfigurationException {
        // Initialize self configuration:
        this.configuration = PropertiesToJAASConfigurationConverter.extractJAASConfiguration(properties);
        
        ApplicationUserConfiguration authConfig = new ApplicationUserConfiguration();
        Configuration.setConfiguration(authConfig);

        final String kdc = this.configuration.getKDC();
        if (kdc == null) {
            throw new NullPointerException("KDC configuration parameter cannot be null");
        }

        final String realm = this.configuration.getRealm();
        if (realm == null) {
            throw new NullPointerException("Realm configuration parameter cannot be null");
        }

        //Sets the realm value and the KDC. Kerberos requires these values to
        // be saved as system properties. This is not too good, since system
        // properties span accross the entire JVM. However, there does not seem
        // to be any other way to do it for now. So, let's set the system
        // property.
        System.setProperty("java.security.krb5.realm", realm);
        System.setProperty("java.security.krb5.kdc", kdc);
        LOG.info("Setting default realm to: " + realm);
        LOG.info("Setting KDC to: " + kdc);
    }

    /**
     * This is the authentication manager configuration class. This
     * configuration allows to setup the security context. It extends the basic
     * abstract configuration from Java security. Using a configuration class
     * like this one avoids using the regular security configuration files and
     * gives a more dynamic behavior.
     * 
     * @author ihanen
     */

    class ApplicationUserConfiguration extends Configuration {

        public static final String DESTINY_CONTEXT = "DestinyContext";

        /**
         * @see javax.security.auth.login.Configuration#refresh()
         */
        public void refresh() {
        }

        /**
         * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
         */
        public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName) {
            return createConfiguration();
        }

        /**
         * Creates a hardcoded configuration for the Kerberos module
         * 
         * @return a hardcoded configuration for the Kerberos module
         */
        private AppConfigurationEntry[] createConfiguration() {
            String name = Krb5LoginModule.class.getName();
            AppConfigurationEntry conf = new AppConfigurationEntry(name, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap());
            AppConfigurationEntry[] result = { conf };
            return result;
        }
    }
}