/*
 * Created on Jan 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.reporterdata;

/**
 * This exception is thrown when a user is missing from the database
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/bluejungle/destiny/tools/reporterdata/MissingUserException.java#1 $
 */

public class MissingUserException extends Exception {

    /**
     * Constructor
     * 
     */
    public MissingUserException() {
        super();
    }

    /**
     * Constructor
     * @param message
     */
    public MissingUserException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param cause
     */
    public MissingUserException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public MissingUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
