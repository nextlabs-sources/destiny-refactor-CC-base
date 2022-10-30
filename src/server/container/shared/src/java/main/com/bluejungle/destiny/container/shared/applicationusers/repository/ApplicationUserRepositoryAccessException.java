/*
 * Created on Jun 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/ApplicationUserRepositoryAccessException.java#1 $
 */

public class ApplicationUserRepositoryAccessException extends Exception {

    /**
     * Constructor
     * 
     * @param arg0
     */
    public ApplicationUserRepositoryAccessException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * 
     * @param arg0
     * @param arg1
     */
    public ApplicationUserRepositoryAccessException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public ApplicationUserRepositoryAccessException(Throwable arg0) {
        super(arg0);
    }
}