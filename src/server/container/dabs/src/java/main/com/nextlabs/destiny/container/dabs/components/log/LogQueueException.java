/*
 * Created on Feb 14, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log;

/**
 * This is the exception for ILogQueueMgr and any of its implementation
 * classes.  
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/container/dabs/components/log/LogQueueException.java#1 $
 */

public class LogQueueException extends Exception {

    /**
     * Constructor
     */
    public LogQueueException() {
    }

    /**
     * Constructor
     * @param message
     */
    public LogQueueException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param cause
     */
    public LogQueueException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public LogQueueException(String message, Throwable cause) {
        super(message, cause);
    }
}
