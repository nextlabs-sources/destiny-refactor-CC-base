/*
 * Created on Apr 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter.impl;

/**
 * @author safdar
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/InvalidEntryException.java#1 $
 */

public class InvalidEntryException extends Exception {

    /**
     * Constructor
     * 
     * @param arg0
     */
    public InvalidEntryException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public InvalidEntryException(Throwable cause) {
        super(cause);
    }
}