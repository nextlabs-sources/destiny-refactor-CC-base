/*
 * Created on Aug 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.loginmgr;

/**
 * This interface represents a user properly logged in through the login
 * manager. Note that this class has been built piecewise out of necessity. A
 * more formal design for authentication and an authenticated user could be
 * complete in the future when deemed necessary
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/appsecurity/loginmgr/ILoggedInUser.java#1 $
 */

public interface ILoggedInUser {

    /**
     * Returns the principal name of the logged in user
     * 
     * @return the principal name of the logged in user
     */
    public String getPrincipalName();

    /**
     * Retrieve the principal id of the logged in user
     * 
     * @return the principal id of the logged in user
     */
    public Long getPrincipalId();

    /**
     * Retrieve the username of the logged in user
     * 
     * @return the username of the logged in user
     */
    public String getUsername();

    /**
     * Determine if the user's password can be changed
     * 
     * @return the user's password can be changed
     */
    public boolean isPasswordModifiable();
}