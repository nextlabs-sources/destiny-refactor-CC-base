/*
 * Created on Mar 3, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.common;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/common/src/java/main/com/bluejungle/destiny/tools/common/SecureLoginException.java#1 $
 */

public class SecureLoginException extends Exception {

    /**
     * Constructor
     * 
     */
    public SecureLoginException() {
        super();
    }

    /**
     * Constructor
     * @param message
     */
    public SecureLoginException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param cause
     */
    public SecureLoginException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public SecureLoginException(String message, Throwable cause) {
        super(message, cause);
    }

}
