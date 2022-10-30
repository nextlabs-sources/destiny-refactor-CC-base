/*
 * Created on Feb 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.loginmgr;

/**
 * This is the login manager interface. The login manager is invoked by various
 * applications to ensure that a given user has the correct credential
 * information for a given application.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_112/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/appsecurity/loginmgr/ILoginMgr.java#1 $
 */

public interface ILoginMgr {

    /**
     * Component name
     */
    public static final String COMP_NAME = "LoginManagerComponent";

    /**
     * Authenticates a user with a given set of credentials
     * 
     * @param loginInfo
     *            authentication credentials
     * @return a user structure if the login is valid
     * @throws LoginException
     *             if the login is invalid
     */
    public ILoggedInUser login(final ILoginInfo loginInfo) throws LoginException;
}