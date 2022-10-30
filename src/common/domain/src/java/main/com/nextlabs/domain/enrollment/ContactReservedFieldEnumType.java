/*
 * Created on Aug 15, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.domain.enrollment;

import com.bluejungle.framework.patterns.EnumBase;


/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/nextlabs/domain/enrollment/ContactReservedFieldEnumType.java#1 $
 */

public class ContactReservedFieldEnumType extends EnumBase {
    private static final long serialVersionUID = 1L;

    public static final ContactReservedFieldEnumType PRINCIPAL_NAME = new ContactReservedFieldEnumType("principalName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String PRINCIPAL_NAME_LABEL = "Contact Principal Name";

    public static final ContactReservedFieldEnumType DISPLAY_NAME = new ContactReservedFieldEnumType("displayname") {
        private static final long serialVersionUID = 1L;
    };
    public static final String DISPLAY_NAME_LABEL = "Full Name";

    public static final ContactReservedFieldEnumType FIRST_NAME = new ContactReservedFieldEnumType("firstName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String FIRST_NAME_LABEL = "First Name";

    public static final ContactReservedFieldEnumType LAST_NAME = new ContactReservedFieldEnumType("lastName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String LAST_NAME_LABEL = "Last Name";
    
    public static final ContactReservedFieldEnumType MAIL = new ContactReservedFieldEnumType("mail") {
        private static final long serialVersionUID = 1L;
    };
    public static final String MAIL_LABEL = "E-Mail";
  
    /**
     * Constructor
     *
     * @param arg0
     */
    public ContactReservedFieldEnumType(String name) {
        super(name);
    }    
}
