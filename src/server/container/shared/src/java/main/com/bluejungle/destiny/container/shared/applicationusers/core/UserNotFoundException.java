/*
 * Created on Jul 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

import java.util.Set;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/UserNotFoundException.java#1 $
 */

public class UserNotFoundException extends Exception {

    private Set missingIds;

    /**
     * Constructor
     *  
     */
    public UserNotFoundException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param missingIds
     */
    public UserNotFoundException(Set missingIds) {
        this.missingIds = missingIds;
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public UserNotFoundException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * 
     * @param arg0
     * @param arg1
     */
    public UserNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public UserNotFoundException(Throwable arg0) {
        super(arg0);
    }

    /**
     * Returns the set of missing ids, if more than 1 user missing
     * 
     * @return
     */
    public Set getMissingIds() {
        return this.missingIds;
    }
}