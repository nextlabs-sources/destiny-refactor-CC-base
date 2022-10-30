/*
 * Created on Sep 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/GroupAlreadyExistsException.java#1 $
 */

public class GroupAlreadyExistsException extends Exception {

    /**
     * Constructor
     * 
     */
    public GroupAlreadyExistsException() {
        super();
    }

    /**
     * Constructor
     * @param arg0
     */
    public GroupAlreadyExistsException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * @param arg0
     */
    public GroupAlreadyExistsException(Throwable arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * @param arg0
     * @param arg1
     */
    public GroupAlreadyExistsException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
