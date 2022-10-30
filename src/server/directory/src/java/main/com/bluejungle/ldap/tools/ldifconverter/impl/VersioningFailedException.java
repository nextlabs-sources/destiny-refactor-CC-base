/*
 * Created on May 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter.impl;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/directory/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/VersioningFailedException.java#1 $
 */

public class VersioningFailedException extends Exception {

    /**
     * Constructor
     * 
     * @param arg0
     */
    public VersioningFailedException(Throwable arg0) {
        super(arg0);
    }
}