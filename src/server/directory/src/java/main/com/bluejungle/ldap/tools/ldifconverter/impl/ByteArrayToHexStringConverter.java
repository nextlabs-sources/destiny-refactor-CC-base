/*
 * Created on Apr 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter.impl;

/**
 * Converts a byte array into a hex string
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/ByteArrayToHexStringConverter.java#1 $
 */

public class ByteArrayToHexStringConverter {

    /**
     * Converts an array of bytes into a hex string
     * 
     * @param bytes
     * @return hex string
     */
    public static String convertToHexString(byte[] bytes) {
        String hexStr = null;
        if (bytes != null) {
            StringBuffer hexStrBuffer = new StringBuffer(128);
            for (int i = 0; i < bytes.length; i++) {
                int nextByte = bytes[i];
                if (nextByte < 0) {
                    nextByte &= 0xFF;
                }
                String hexValue = Integer.toHexString(nextByte);
                if (hexValue.length() == 1) {
                    hexValue = "0" + hexValue;
                }
                hexStrBuffer.append(hexValue);
            }
            hexStr = hexStrBuffer.toString();
        }
        return hexStr;
    }
}