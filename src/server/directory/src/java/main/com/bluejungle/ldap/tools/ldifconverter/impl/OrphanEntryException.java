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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/directory/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/OrphanEntryException.java#1 $
 */

public class OrphanEntryException extends Exception {

    public OrphanEntryException(String dn, String parentDN) {
        super("Entry '" + dn + "' would be an orphan as its parent '" + parentDN + "' does not exist, or could not be converted.");
    }
}