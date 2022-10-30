/*
 * Created on Dec 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.engine.destiny;


/**
 * BundleVaultException is thrown when there is an error writing to or reading
 * the bundle from the bundle vault
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/engine/destiny/BundleVaultException.java#1 $
 */

public class BundleVaultException extends Exception {

    /**
     * Create an instance of BundleVaultException
     * @param message
     * @param cause
     */
    public BundleVaultException(String message, Throwable cause) {
        super(message, cause);
    }

}
