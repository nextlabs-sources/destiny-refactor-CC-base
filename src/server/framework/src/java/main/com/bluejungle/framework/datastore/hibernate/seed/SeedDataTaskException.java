/*
 * Created on May 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.seed;

/**
 * This exception is thrown whenever a seed data task execution fails.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/seed/SeedDataTaskException.java#1 $
 */

public class SeedDataTaskException extends Exception {

    /**
     * Constructor
     */
    public SeedDataTaskException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param message
     */
    public SeedDataTaskException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param message
     * @param cause
     */
    public SeedDataTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public SeedDataTaskException(Throwable cause) {
        super(cause);
    }
}