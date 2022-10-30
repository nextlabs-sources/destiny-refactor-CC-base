package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/pdpapi/PDPTimeout.java#1 $:
 */

public class PDPTimeout extends Exception {
    public PDPTimeout() {
        super();
    }

    public PDPTimeout(String message ) {
        super(message);
    }

    public PDPTimeout(String message, Throwable cause ) {
        super(message, cause);
    }
}


