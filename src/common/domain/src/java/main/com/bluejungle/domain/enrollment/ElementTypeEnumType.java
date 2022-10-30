package com.bluejungle.domain.enrollment;

/*
 * Created on Apr 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 *
 * @author safdar, atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/enrollment/ElementTypeEnumType.java#1 $
 */

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This is an enumerated type for the types of leaf elements contained in the
 * Dictionary. Currently we support User, Host and Application types.
 */
public class ElementTypeEnumType extends EnumBase {

    private static final long serialVersionUID = 1L;

    public static final ElementTypeEnumType USER = new ElementTypeEnumType("USER") {
        private static final long serialVersionUID = 1L;
    };
    public static final ElementTypeEnumType CONTACT = new ElementTypeEnumType("CONTACT") {
        private static final long serialVersionUID = 1L;
    };
    public static final ElementTypeEnumType COMPUTER = new ElementTypeEnumType("HOST") {
        private static final long serialVersionUID = 1L;
    };
    public static final ElementTypeEnumType APPLICATION = new ElementTypeEnumType("APPLICATION") {
        private static final long serialVersionUID = 1L;
    };
    public static final ElementTypeEnumType SITE = new ElementTypeEnumType("SITE") {
        private static final long serialVersionUID = 1L;
    };
    public static final ElementTypeEnumType CLIENT_INFO = new ElementTypeEnumType("CLIENT_INFO") {
        private static final long serialVersionUID = 1L;
    };

    /**
     * Constructor
     * 
     * @param name
     */
    public ElementTypeEnumType(String name) {
        super(name);
    }

    /**
     * REtrieves the type by name
     * 
     * @param name
     * @return
     */
    public static ElementTypeEnumType getByName(String name) {
        return EnumBase.getElement(name, ElementTypeEnumType.class);
    }

}
