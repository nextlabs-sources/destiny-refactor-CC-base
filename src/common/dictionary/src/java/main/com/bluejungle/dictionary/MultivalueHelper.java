package com.bluejungle.dictionary;

import java.util.ArrayList;
import java.util.List;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/MultivalueHelper.java#1 $
 */

/**
 * This is a helper class with static methods for use
 * in the implementation of the dictionary's methods
 * that deal with multivalued properties and attributes.
 *
 * @author Sergey Kalinichenko
 */
class MultivalueHelper {

    /**
     * The separator character for the elements of multi-character strings.
     */
    public static final char SEPARATOR = ':';

    /**
     * The escape character for separators and escape characters.
     */
    public static final char ESCAPE_CHARACTER = '\\';

    /**
     * This method escapes the separator characters in a string.
     *
     * @param s the string to escape.
     * @return the escaped string.
     */
    public static String escapeElement(String s) {
        // Optimize the common case, when the string has no colons or slashes
        if (s.indexOf(ESCAPE_CHARACTER) == -1 && s.indexOf(SEPARATOR) == -1) {
            return s;
        }
        // Escape the string character-by-character
        StringBuffer buf = new StringBuffer();
        for ( int j = 0; j != s.length() ; j++ ) {
            char ch = s.charAt(j);
            if ( ch == SEPARATOR || ch == ESCAPE_CHARACTER ) {
                buf.append(ESCAPE_CHARACTER);
            }
            buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * Undoes the results of joining strings.
     *
     * @param value the string representing an array of string values.
     * @return an array of strings. 
     */
    public static String[] splitElements(String value) {
        if ( value == null ) {
            return null;
        }
        List<String> res = new ArrayList<String>();
        String tmp = null;
        int i = 0;
        int last = value.length();
        if (value.charAt(i) == SEPARATOR) {
            i++;
        }
        if (value.charAt(last-1) == SEPARATOR) {
            last--;
        }
        for ( ; i < last ; i++ ) {
            if ( tmp == null ) {
                tmp = "";
            }
            if ( value.charAt(i) == SEPARATOR ) {
                res.add(tmp);
                tmp = null;
            } else {
                if ( value.charAt(i) == ESCAPE_CHARACTER && i != value.length()-1 ) {
                    i++;
                }
                tmp += value.charAt(i);
            }
        }
        if ( tmp != null ) {
            res.add(tmp);
        }
        return res.toArray(new String[res.size()]);
    }

    /**
     * Escapes and joins strings together.
     *
     * @param strValue the strings to join.
     * @return a concatenation of the original strings,
     * with the separator in between them. The values
     * with the separator character are escaped. 
     */
    public static String joinElements(String[] strValue) {
        StringBuffer buf = new StringBuffer(":");
        if ( strValue != null ) {
            for ( int i = 0 ; i != strValue.length ; i++ ) {
                if ( strValue[i] == null ) {
                    throw new NullPointerException("strValue["+i+"]");
                }
                if ( i != 0 ) {
                    buf.append(SEPARATOR);
                }
                buf.append(escapeElement(strValue[i]));
            }
            buf.append(':');
            return buf.toString();
        } else {
            return null;
        }
    }

}
