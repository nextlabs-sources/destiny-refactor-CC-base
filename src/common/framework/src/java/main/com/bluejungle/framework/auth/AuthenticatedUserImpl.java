/*
 * Created on Apr 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.auth;

import java.util.Date;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the authenticated user implementation class. This class exposes
 * information about the user that has been authenticated.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/auth/AuthenticatedUserImpl.java#1 $
 */

class AuthenticatedUserImpl implements IAuthenticatedUser {

    private static final Log LOG = LogFactory.getLog(AuthenticatedUserImpl.class.getName());
    private Date loginDate;
    private LoginContext loginContext;

    /**
     * Cannot use this constructor
     */
    private AuthenticatedUserImpl() {
    }

    /**
     * Constructor
     * 
     * @param loginContext
     *            subject that was authenticated
     */
    public AuthenticatedUserImpl(LoginContext loginContext) {
        if (loginContext == null) {
            throw new NullPointerException("login context cannot be null");
        }
        this.loginDate = new Date();
        this.loginContext = loginContext;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * @see com.bluejungle.framework.auth.IAuthenticatedUser#getLoginDate()
     */
    public Date getLoginDate() {
        return this.loginDate;
    }

    /**
     * @see com.bluejungle.framework.auth.IAuthenticatedUser#getSubject()
     */
    public Subject getSubject() {
        return this.loginContext.getSubject();
    }

    /**
     * @see com.bluejungle.framework.auth.IAuthenticatedUser#logoff()
     */
    public void logoff() {
        try {
            this.loginContext.logout();
        } catch (LoginException e) {
            //Not much to do here, throwing this to the caller does not help
            // either
            getLog().error("Error when logging off user", e);
        }
    }

    /**
     * Logs off the user before the object gets destroyed. In theory the caller
     * should do that, but this is added as a convenience to avoid leaving
     * opened Kerberos sessions.
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        logoff();
        super.finalize();
    }
}