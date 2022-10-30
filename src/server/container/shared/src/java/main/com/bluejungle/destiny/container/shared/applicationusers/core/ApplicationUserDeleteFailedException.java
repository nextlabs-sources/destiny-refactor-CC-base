/*
 * Created on Jul 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/ApplicationUserDeleteFailedException.java#1 $
 */

public class ApplicationUserDeleteFailedException extends Exception {

    /**
     * Constructor
     *  
     */
    public ApplicationUserDeleteFailedException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public ApplicationUserDeleteFailedException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * 
     * @param arg0
     * @param arg1
     */
    public ApplicationUserDeleteFailedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public ApplicationUserDeleteFailedException(Throwable arg0) {
        super(arg0);
    }
}