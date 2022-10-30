/*
 * Created on Sep 16, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/TernaryType.java#1 $:
 */

package com.bluejungle.framework.utils;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * An enum for the tri-state boolean with the values TRUE, FALSE, and UNKNOWN
 */

public class TernaryType extends EnumBase {
    public static final TernaryType TRUE = new TernaryType("True");
    public static final TernaryType FALSE = new TernaryType("False");
    public static final TernaryType UNKNOWN = new TernaryType("Unknown");

    /**
     *
     */
    private TernaryType(String name) {
        super(name);
    }

    public static TernaryType getTernaryType(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        return getElement(name, TernaryType.class);
    }
}
