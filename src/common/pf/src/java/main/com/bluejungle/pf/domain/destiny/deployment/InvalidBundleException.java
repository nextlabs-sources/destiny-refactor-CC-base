/*
 * Created on Dec 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.deployment;


/**
 * Exception thrown when a bundle is determined to be invalid
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/deployment/InvalidBundleException.java#1 $
 */

public class InvalidBundleException extends Exception {

    /**
     * Create an instance of InvalidBundleException
     * 
     * @param message
     */
    public InvalidBundleException(String message) {
        super(message);
    }

    /**
     * Create an instance of InvalidBundleException
     * 
     * @param message
     * @param cause
     */
    public InvalidBundleException(String message, Throwable cause) {
        super(message, cause);
    }

}
