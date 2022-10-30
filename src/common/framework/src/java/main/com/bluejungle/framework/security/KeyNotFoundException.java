/*
 * Created on Dec 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.security;

/**
 * Exception thrown by {@link IKeyManager} when a requested key cannot be found
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/security/KeyNotFoundException.java#1 $
 */

public class KeyNotFoundException extends RuntimeException {

    /**
     * Create an instance of KeyNotFoundException
     * 
     * @param message
     *            the error message associated with this exception
     */
    public KeyNotFoundException(String message) {
        super(message);
    }

}
