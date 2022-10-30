package com.bluejungle.domain.enrollment;

/*
 * Created on May 12, 2006
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *
 * @author safdar, atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/enrollment/UserReservedFieldEnumType.java#1 $
 */

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This enumeration defines user reserved fields.
 */
public class UserReservedFieldEnumType extends EnumBase {
    private static final long serialVersionUID = 1L;

    public static final UserReservedFieldEnumType PRINCIPAL_NAME = new UserReservedFieldEnumType("principalName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String PRINCIPAL_NAME_LABEL = "User Principal Name";

    public static final UserReservedFieldEnumType DISPLAY_NAME = new UserReservedFieldEnumType("displayname") {
        private static final long serialVersionUID = 1L;
    };
    public static final String DISPLAY_NAME_LABEL = "Full Name";

    public static final UserReservedFieldEnumType FIRST_NAME = new UserReservedFieldEnumType("firstName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String FIRST_NAME_LABEL = "First Name";

    public static final UserReservedFieldEnumType LAST_NAME = new UserReservedFieldEnumType("lastName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String LAST_NAME_LABEL = "Last Name";

    public static final UserReservedFieldEnumType MAIL = new UserReservedFieldEnumType("mail") {
        private static final long serialVersionUID = 1L;
    };
    public static final String MAIL_LABEL = "E-Mail";

    public static final UserReservedFieldEnumType WINDOWS_SID = new UserReservedFieldEnumType("windowsSid") {
        private static final long serialVersionUID = 1L;
    };
    public static final String WINDOWS_SID_LABEL = "Windows User SID";

    public static final UserReservedFieldEnumType UNIX_ID = new UserReservedFieldEnumType("unixId") {
        private static final long serialVersionUID = 1L;
    };
    public static final String UNIX_ID_LABEL = "UNIX User ID";

    /**
     * Constructor
     *
     * @param arg0
     */
    public UserReservedFieldEnumType(String name) {
        super(name);
    }

    /**
     * @see IEnum#getName()
     */
    public String getName() {
        return super.getName();
    }

}
