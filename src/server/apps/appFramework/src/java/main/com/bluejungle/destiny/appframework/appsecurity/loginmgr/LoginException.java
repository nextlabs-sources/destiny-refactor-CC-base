/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.loginmgr;


/**
 * This exception is thrown when a login attempt results in an error
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/appsecurity/loginmgr/LoginException.java#1 $
 */
public class LoginException extends Exception {

    /**
     * Constructor
     * @param cause
     */
    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

}
