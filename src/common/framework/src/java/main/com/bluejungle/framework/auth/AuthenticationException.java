/*
 * Created on Apr 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.auth;


/**
 * This exception is thrown when a user authentication fails.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/auth/AuthenticationException.java#1 $
 */

public class AuthenticationException extends Exception {

    /**
     * Constructor
     */
    public AuthenticationException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}