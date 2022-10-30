/*
 * Created on Sep 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

/**
 * Various utilities related to password management
 * 
 * @author sasha
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/PasswordUtils.java#1 $:
 */

public final class PasswordUtils {
    public static final int DEFAULT_PASSWORD_MIN_LENGTH = 7;
    public static final int DEFAULT_PASSWORD_MAX_LENGTH = 12;
    public static final boolean DEFAULT_PASSWORD_NUMBER_REQUIRED = true;
    public static final boolean DEFAULT_PASSWORD_NON_WORD_REQUIRED = true;

    /**
     * Validates the supplied password according to the following default
     * requirements: <br/>
     * <br/>
     * Minimum length: {@see #DEFAULT_PASSWORD_MIN_LENGTH}<br />
     * Maximum length: {@see #DEFAULT_PASSWORD_MAX_LENGTH}<br />
     * Requires Number: {@see #DEFAULT_PASSWORD_NUMBER_REQUIRED}<br />
     * Requires Non Word: {@see #DEFAULT_NON_WORD_REQUIRED}<br />
     * <br />
     * In addition, the password must not be null and must contain at least one
     * alphabet character [a-zA-Z].
     * 
     * @param password
     *            the password to verify
     * @return true if the password satisfies all the rules, false otherwise
     * @see #isValidPassword(String, int, int, boolean, boolean)
     */
    public static final boolean isValidPasswordDefault(String password) {
        return isValidPassword(password, DEFAULT_PASSWORD_MIN_LENGTH, DEFAULT_PASSWORD_MAX_LENGTH, DEFAULT_PASSWORD_NUMBER_REQUIRED, DEFAULT_PASSWORD_NON_WORD_REQUIRED);
    }

    /**
     * Validates supplied password according to the specified options and rules.
     * Rules that are always enforced are: password can never be null, and it
     * must contain at least one alphabet character [a-zA-Z]. Additional rules
     * include minimum and maximum length requirements, requiring at least one
     * numeric character, and requiring at least one non-alphanumeric character
     * 
     * @param password
     *            the password to verify
     * @param minLength
     *            minimum required length, specify 0 if no minimum required
     * @param maxLength
     *            maximum supported length, specify Integer.MAX_VALUE if no
     *            maximum is enforced
     * @param numberRequired
     *            if true then password will be checked for presense of at least
     *            one numeric character
     * @param nonWordRequired
     *            if true then password will be checked for presense of at least
     *            one non-alphanumeric character, such as $
     * @return true if the password satisfies all the rules, false otherwise
     */
    public static final boolean isValidPassword(String password, int minLength, int maxLength, boolean numberRequired, boolean nonWordRequired) {
        if (password == null) {
            return false;
        }

        int length = password.length();
        if (length < minLength || length > maxLength) {
            return false;
        }

        if (!password.matches(".*[a-zA-Z]+.*")) {
            return false;
        }

        if (numberRequired) {
            if (!password.matches(".*\\d+.*")) {
                return false;
            }
        }

        if (nonWordRequired) {
            if (!password.matches(".*\\W+.*")) {
                return false;
            }
        }
        return true;
    }

}