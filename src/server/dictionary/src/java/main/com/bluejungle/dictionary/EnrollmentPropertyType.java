/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/EnrollmentPropertyType.java#1 $
 */

package com.bluejungle.dictionary;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * Instances of this class represent types of enrollment properties.
 */
abstract class EnrollmentPropertyType extends EnumBase {

    public static final EnrollmentPropertyType STRING = new EnrollmentPropertyType("String") {
        private static final long serialVersionUID = 1L;
    };

    public static final EnrollmentPropertyType STRING_ARRAY = new EnrollmentPropertyType("String[]") {
        private static final long serialVersionUID = 1L;
    };

    public static final EnrollmentPropertyType NUMBER = new EnrollmentPropertyType("Number") {
        private static final long serialVersionUID = 1L;
    };

    public static final EnrollmentPropertyType BINARY = new EnrollmentPropertyType("Binary") {
        private static final long serialVersionUID = 1L;
    };

    public EnrollmentPropertyType( String name ) {
        super( name, EnrollmentPropertyType.class );
    }

}
