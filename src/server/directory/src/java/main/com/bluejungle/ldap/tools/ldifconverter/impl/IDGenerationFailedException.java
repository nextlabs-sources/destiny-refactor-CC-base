/*
 * Created on Jun 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter.impl;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/directory/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/IDGenerationFailedException.java#1 $
 */

public class IDGenerationFailedException extends Exception {

    /**
     * Constructor
     * 
     * @param entryDN
     * @param cause
     */
    public IDGenerationFailedException(String entryDN, Throwable cause) {
        super("Failed to create import id for entry: '" + entryDN + "'", cause);
    }
}