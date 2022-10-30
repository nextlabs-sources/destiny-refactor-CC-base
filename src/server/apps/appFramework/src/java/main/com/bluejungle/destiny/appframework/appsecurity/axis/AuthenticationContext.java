/*
 * Created on Feb 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.axis;

/**
 * A context for passing authentication credentials to the AuthenticationHandler.  The context is stored in a ThreadLocal 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/appsecurity/axis/AuthenticationContext.java#1 $
 */

public class AuthenticationContext {
    private static ThreadLocal contextThreadLocal = new ThreadLocal();
    private String username;
    private String password;
    
    /**
     * Constructor
     * 
     */
    private AuthenticationContext() {}

    /**
     * Retrieve the current Authentication Context
     * @return the current Authentication Context
     */
    public static AuthenticationContext getCurrentContext() {
        AuthenticationContext currentContext = (AuthenticationContext)contextThreadLocal.get();
        if (currentContext == null) {
            currentContext = new AuthenticationContext();
            contextThreadLocal.set(currentContext);
        }
        
        return currentContext;
    }
    
    /**
     * Set the username for this context
     * @param username the username for this context
     */
    public void setUsername(String username) {
        if (username == null) {
            throw new NullPointerException("username cannot be null");
        }
        
        this.username = username;
    }
    
    /**
     * Retrieve the username for this context
     * @return the username for this context
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Set the password for this context
     * @param password the password for this context
     */
    public void setPassword(String password) {
        if (password == null) {
            throw new NullPointerException("password cannot be null");
        }
        
        this.password = password;
    }
    
    /**
     * Retrieve the password for this context
     * @return the password for this context
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Clear the current context
     */
    public static void clearCurrentContext() {
        contextThreadLocal.set(null);
    }    
}
