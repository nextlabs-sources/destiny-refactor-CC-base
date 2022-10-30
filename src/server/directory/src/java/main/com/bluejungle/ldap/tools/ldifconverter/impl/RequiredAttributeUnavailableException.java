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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/directory/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/RequiredAttributeUnavailableException.java#1 $
 */

public class RequiredAttributeUnavailableException extends Exception {

    public RequiredAttributeUnavailableException(String attributeName, String entryDN) {
        super("Entry with DN '" + entryDN + "' does not have the required attribute '" + attributeName + "'.");
    }
}