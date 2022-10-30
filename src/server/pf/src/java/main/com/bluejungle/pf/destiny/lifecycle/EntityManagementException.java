package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/EntityManagementException.java#1 $
 */

// TODO: This exception should be subclassed, and more descriptive errors
// such as "duplicate entry," "missing entry," etc. should be created.

public class EntityManagementException extends Exception {

    /**
     * Constructs an empty EntityManagementException.
     */
    public EntityManagementException() {
    }

    /**
     * Constructs an EntityManagementException with the
     * specified message.
     * @param message The message of the EntityManagementException.
     */
    public EntityManagementException( String message ) {
        super( message );
    }

    /**
     * Constructs an EntityManagementException with the
     * specified message and the cause.
     * @param message The message of the EntityManagementException.
     * @param cause The cause of the EntityManagementException.
     */
    public EntityManagementException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructs an EntityManagementException with the
     * specified cause.
     * @param cause The cause of the EntityManagementException.
     */
    public EntityManagementException( Throwable cause ) {
        super( cause );
    }

}
