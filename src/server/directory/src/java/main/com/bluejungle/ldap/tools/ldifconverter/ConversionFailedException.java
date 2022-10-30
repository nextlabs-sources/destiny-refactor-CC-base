/*
 * Created on Apr 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/ldifconverter/ConversionFailedException.java#1 $
 */

public class ConversionFailedException extends Exception {

    public ConversionFailedException(String msg) {
        super(msg);
    }

    public ConversionFailedException(Throwable cause) {
        super(cause);
    }
}