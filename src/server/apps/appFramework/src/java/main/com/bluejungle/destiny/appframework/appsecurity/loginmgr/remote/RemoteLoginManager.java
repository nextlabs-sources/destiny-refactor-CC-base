/*
 * Created on Mar 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote;

import com.bluejungle.destiny.appframework.appsecurity.axis.AuthenticationContext;
import com.bluejungle.destiny.appframework.appsecurity.axis.SecureSessionVaultGateway;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoggedInUser;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginInfo;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginMgr;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.destiny.interfaces.secure_session.v1.SecureSessionServiceIF;
import com.bluejungle.destiny.services.secure_session.v1.SecureSessionServiceLocator;
import com.bluejungle.destiny.types.basic.v1.Property;
import com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.ILogEnabled;

import org.apache.commons.logging.Log;

import javax.xml.rpc.ServiceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * The Remote Log Manager logs on to a remote Destiny Component through the
 * Secure Session web service. The end point of the web service must be provided
 * through a configuration property.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/apps/webFramework/src/java/main/com/bluejungle/destiny/webui/framework/loginmgr/remote/RemoteLoginManager.java#1 $
 */
public class RemoteLoginManager implements ILoginMgr, ILogEnabled, IConfigurable {

    public static final String SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME = "SecureSessionServiceEnpoint";
    public static final String SECURE_SESSION_ID_PROP_NAME = "Id";
    public static final String SECURE_SESSION_PRINCIPALNAME_PROP_NAME = "PrincipalName";
    public static final String SECURE_SESSION_USERNAME_PROP_NAME = "Username";
    public static final String SECURE_SESSION_CAN_CHANGE_PASSWORD = "CanChangePassword";

    private Log log;
    private IConfiguration config;

    /**
     * Create an instance of a RemoteLoginManager
     */
    public RemoteLoginManager() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginMgr#login(com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginInfo)
     */
    public ILoggedInUser login(ILoginInfo loginInfo) throws LoginException {
        if (loginInfo == null) {
            throw new NullPointerException("loginInfo cannot be null.");
        }
        ILoggedInUser result = null;
        final String username = loginInfo.getUserName();
        final String password = loginInfo.getPassword();

        if (username == null || username.length() == 0) {
            IllegalArgumentException e = new IllegalArgumentException("Username cannot be null or empty");
            throw new LoginException("Empty or null username specified", e);
        }
        if (password == null || password.length() == 0) {
            IllegalArgumentException e = new IllegalArgumentException("Password cannot be null or empty");
            throw new LoginException("Empty or null password specified", e);
        }

        try {
            SecureSession secureSession = initSession(username, password);
            if (secureSession != null) {
                SecureSessionVaultGateway.setSecureSession(secureSession);
                result = new LoggedInUserImpl(secureSession);
            }

            // FIX ME - Some of the exception below should be split into
            // specific exception. Currently, the client has to check the
            // "cause" to see what happened
        } catch (MalformedURLException exception) {
            throw new LoginException("Invalid Secure Session Endpoint", exception);
        } catch (AccessDeniedFault accesDeniedFault) {
            throw new LoginException("Access denied for user, " + username, accesDeniedFault);
        } catch (RemoteException exception) {
            throw new LoginException("Error occured while attempting to authenticate user", exception);
        } catch (ServiceException exception) {
            throw new LoginException("Error occured while attempting to authenticate user", exception);
        }
        return result;
    }

    /**
     * Initalize the secure session
     * 
     * @param username
     *            the users username
     * @param password
     *            the users password
     * @return the created secure session
     * @throws ServiceException
     * @throws RemoteException
     * @throws MalformedURLException
     */
    private SecureSession initSession(String username, String password) throws ServiceException, RemoteException, MalformedURLException {
        SecureSession sessionToReturn = null;

        AuthenticationContext authContext = AuthenticationContext.getCurrentContext();
        authContext.setUsername(username);
        authContext.setPassword(password);

        String secureSessionServiceEndpoint = (String) config.get(SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME);
        if (secureSessionServiceEndpoint == null) {
            throw new IllegalArgumentException("Secure Session Service Enpoint configuration property was not set.");
        }

        try {
            URL location = new URL(secureSessionServiceEndpoint);
            SecureSessionServiceIF secureSessionService = getSecureSessionService(secureSessionServiceEndpoint);
            sessionToReturn = secureSessionService.initSession();
        } finally {
            // Clear the auth context to remove remnant of auth credentials
            AuthenticationContext.clearCurrentContext();
        }

        return sessionToReturn;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(null)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Retrieve the secure session service. (Currently protected to support a
     * classloading problem for web apps)
     * 
     * @param location
     *            the location of the service
     * @return the secure session service
     * @throws ServiceException
     * @throws RemoteException
     */
    protected SecureSessionServiceIF getSecureSessionService(String serviceLocation) throws ServiceException, RemoteException {
        SecureSessionServiceLocator locator = new SecureSessionServiceLocator();
        locator.setSecureSessionServiceEndpointAddress(serviceLocation);
        return locator.getSecureSessionService();
    }

    /**
     * Logged in user implementation class
     * 
     * @author ihanen
     */
    protected class LoggedInUserImpl implements ILoggedInUser {

        private String principalName;
        private Long principalId;
        private String username;
        private boolean canChangePassword = false;

        /**
         * Constructor
         */
        public LoggedInUserImpl(SecureSession secureSession) {
            if (secureSession == null) {
                throw new NullPointerException("secureSession cannot be null.");
            }

            Property[] secureSessionProperties = secureSession.getProperties().getProperty();
            for (int i = 0; i < secureSessionProperties.length; i++) {
                Property nextProperty = secureSessionProperties[i];
                String nextPropertyName = nextProperty.getName();
                if (nextPropertyName.equals(SECURE_SESSION_ID_PROP_NAME)) {
                    this.principalId = new Long(nextProperty.getValues().getValue()[0]);
                } else if (nextPropertyName.equals(SECURE_SESSION_PRINCIPALNAME_PROP_NAME)) {
                    this.principalName = nextProperty.getValues().getValue()[0];
                } else if (nextPropertyName.equals(SECURE_SESSION_USERNAME_PROP_NAME)) {
                    this.username = nextProperty.getValues().getValue()[0];
                } else if (nextPropertyName.equals(SECURE_SESSION_CAN_CHANGE_PASSWORD)) {
                    this.canChangePassword = Boolean.valueOf(nextProperty.getValues().getValue()[0]).booleanValue();
                }
            }

            if (this.username == null) {
                throw new IllegalStateException("Username property not found in secure session");
            }

            if (this.principalName == null) {
                throw new IllegalStateException("Principal name property not found in secure session");
            }

            if (this.principalId == null) {
                throw new IllegalStateException("Principal id property not found in secure session");
            }
        }

        /**
         * @see com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoggedInUser#getPrincipalName()
         */
        public String getPrincipalName() {
            return this.principalName;
        }

        /**
         * com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoggedInUser#getPrincipalId()
         */
        public Long getPrincipalId() {
            return this.principalId;
        }

        /**
         * @see com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoggedInUser#isPasswordModifiable()
         */
        public boolean isPasswordModifiable() {
            return this.canChangePassword;
        }

        /**
         * @see com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoggedInUser#getUsername()
         */
        public String getUsername() {
            return this.username;
        }
    }
}