package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/main/com/nextlabs/util/Strings.java#1 $
 */

/**
 * This class contains utility methods for strings.
 *
 * @author Sergey Kalinichenko
 */
public class Strings {

    /**
     * Private constructor prevents instantiations.
     */
    private Strings() {
    }

    /**
     * Checks if the string is empty or null.
     *
     * @param s the String to be checked.
     * @return true if the string is null or empty; false otherwise.
     */
    public static boolean isEmpty(String s) {
        return s==null || s.length() == 0;
    }

    /**
     * Checks if the string starts or ends in a whitespace character.
     * @param s the String to be checked.
     * @return true if the string is trimmed; false otherwise.
     */
    public static boolean isTrimmed(String s) {
        // Empty strings are considered trimmed
        if (isEmpty(s)) {
            return true;
        }
        return !Character.isWhitespace(s.charAt(0))
            && !Character.isWhitespace(s.charAt(s.length()-1));
    }

}
