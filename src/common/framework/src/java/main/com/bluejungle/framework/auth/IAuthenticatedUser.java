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

/**
 * This interface represents an authenticated user. It contains information
 * about the authenticated user
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/auth/IAuthenticatedUser.java#1 $
 */

public interface IAuthenticatedUser {

    /**
     * Returns the date / time at which the user successfully authenticated in
     * the system
     * 
     * @return the date and time of the user authentication
     */
    public Date getLoginDate();

    /**
     * Returns the subject associated with the authenticated user
     * 
     * @return the subject associated with the authenticated user
     */
    public Subject getSubject();

    /**
     * Logs off an authenticated user
     */
    public void logoff();
}