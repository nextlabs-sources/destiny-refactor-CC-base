/*
 * Created on Jul 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.auth.jaas;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/auth/jaas/KrbAuthenticationContext.java#1 $
 */

public class KrbAuthenticationContext implements IAuthenticationContext {

    private static final Log LOG = LogFactory.getLog(KrbAuthenticationContext.class.getName());

    protected LoginContext loginContext;

    /**
     * Constructor
     * 
     * @param ctx
     */
    KrbAuthenticationContext(LoginContext ctx) {
        this.loginContext = ctx;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext#logoff()
     */
    public void logoff() {
        try {
            this.loginContext.logout();
        } catch (LoginException e) {
            //Not much to do here, throwing this to the caller does not help
            // either
            LOG.error("Error when logging off user", e);
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