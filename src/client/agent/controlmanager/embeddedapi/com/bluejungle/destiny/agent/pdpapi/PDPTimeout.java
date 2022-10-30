package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/PDPTimeout.java#1 $:
 */

/**
 * This exception is thrown by the PDPAPI to indicate a timeout
 */
public class PDPTimeout extends Exception {
    /**
     * Constructs a <code>PDPTimeout</code> exception with no additional data
     */
    public PDPTimeout() {
        super();
    }

    /**
     * Constructs a <code>PDPTimeout</code> exception with the specified message
     * @param message the message
     */
    public PDPTimeout(String message ) {
        super(message);
    }

    /**
     * Wraps another exception in a <code>PDPTimeout</code> exception with the specified message 
     * @param message the message
     * @param cause the root exception
     */
    public PDPTimeout(String message, Throwable cause ) {
        super(message, cause);
    }
}


