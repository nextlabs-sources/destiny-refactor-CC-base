/*
 * Created on May 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

/**
 * This exception is thrown when the seed task is invoked with invalid
 * parameters.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/InvalidSeedParamsException.java#1 $
 */

public class InvalidSeedParamsException extends Exception {

    /**
     * Constructor
     */
    public InvalidSeedParamsException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param message
     */
    public InvalidSeedParamsException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param message
     * @param cause
     */
    public InvalidSeedParamsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public InvalidSeedParamsException(Throwable cause) {
        super(cause);
    }
}