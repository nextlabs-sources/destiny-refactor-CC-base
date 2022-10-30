package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/PDPException.java#1 $:
 */

/**
 * This exception is thrown by the PDPAPI to indicate an error during policy evaluation
 */
public class PDPException extends Exception {
    /**
     * Constructs a <code>PDPException</code> with the specified message
     * @param message the message
     */
    public PDPException(String message ) {
        super(message);
    }

    /**
     * Wraps another exception in a <code>PDPException</code> with the specified message
     * @param message the message
     * @param cause the root exception
     */
    public PDPException(String message, Throwable cause ) {
        super(message, cause);
    }
}


