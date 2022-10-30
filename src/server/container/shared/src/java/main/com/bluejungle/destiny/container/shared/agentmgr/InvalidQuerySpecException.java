/*
 * Created on Mar 9, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

/**
 * This exception is thrown if the query specification is invalid
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/InvalidQuerySpecException.java#1 $
 */

public class InvalidQuerySpecException extends RuntimeException{

    /**
     * Constructor
     */
    public InvalidQuerySpecException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param message
     */
    public InvalidQuerySpecException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public InvalidQuerySpecException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * 
     * @param message
     * @param cause
     */
    public InvalidQuerySpecException(String message, Throwable cause) {
        super(message, cause);
    }
}
