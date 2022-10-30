/*
 * Created on Feb 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.auth;

import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
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

public class KrbAuthMgrImpl implements IAuthMgr, IConfigurable, IInitializable, ILogEnabled {

    protected IConfiguration configuration;
    protected Log log;

    public IAuthenticatedUser authenticate(final String userName, final String password) throws AuthenticationException {
        IAuthenticatedUser result = null;
        try {
            CredentialCallBackHandler callback = new CredentialCallBackHandler(userName, password);
            LoginContext lc = new LoginContext(AuthConfiguration.DESTINY_CONTEXT, callback);
            lc.login();
            result = new AuthenticatedUserImpl(lc);
            //Logoff has to be taken care of by the caller
        } catch (LoginException e) {
            getLog().info("Login failed for user '" + userName + "':" + e.getMessage(), e);
            throw new AuthenticationException(e);
        }
        return result;
    }

    /**
     * Sets the configuration for the component
     * 
     * @param newConfig
     *            new configuration
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.configuration = newConfig;
    }

    /**
     * Returns the configuration object
     * 
     * @return the configuration object
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * This is the initialization method. This method sets up the security
     * context configuration.
     */
    public void init() {
        //Initializes the configuration
        AuthConfiguration authConfig = new AuthConfiguration();
        Configuration.setConfiguration(authConfig);

        final String kdc = (String) this.configuration.get(KDC_CONFIG_PARAM);
        if (kdc == null) {
            throw new NullPointerException("KDC configuration parameter cannot be null");
        }

        final String realm = (String) this.configuration.get(REALM_CONFIG_PARAM);
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
        this.getLog().info("Setting default realm to: " + realm);
        this.getLog().info("Setting KDC to: " + kdc);
    }

    /**
     * Sets the log object
     * 
     * @param newLog
     *            log object to set
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    public Log getLog() {
        return log;
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

    class AuthConfiguration extends Configuration {

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