/*
 * Created on Sep 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/AccessGroupCreationFailedException.java#1 $
 */

public class AccessGroupCreationFailedException extends Exception {

    /**
     * Constructor
     * 
     */
    public AccessGroupCreationFailedException() {
        super();
    }

    /**
     * Constructor
     * @param arg0
     */
    public AccessGroupCreationFailedException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * @param arg0
     */
    public AccessGroupCreationFailedException(Throwable arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * @param arg0
     * @param arg1
     */
    public AccessGroupCreationFailedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
