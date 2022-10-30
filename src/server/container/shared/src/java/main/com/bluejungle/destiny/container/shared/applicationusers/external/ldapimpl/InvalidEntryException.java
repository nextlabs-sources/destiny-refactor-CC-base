/*
 * Created on Oct 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/InvalidEntryException.java#1 $
 */

public class InvalidEntryException extends Exception {

    /**
     * Constructor
     * 
     */
    public InvalidEntryException() {
        super();
    }

    /**
     * Constructor
     * @param message
     */
    public InvalidEntryException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param cause
     */
    public InvalidEntryException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public InvalidEntryException(String message, Throwable cause) {
        super(message, cause);
    }

}
