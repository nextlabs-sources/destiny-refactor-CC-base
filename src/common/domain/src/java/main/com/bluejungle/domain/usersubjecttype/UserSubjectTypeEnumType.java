/*
 * Created on Aug 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.usersubjecttype;

import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.patterns.EnumBase;

/**
 * Enum for User Subject types
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/usersubjecttype/UserSubjectTypeEnumType.java#1 $
 */

public class UserSubjectTypeEnumType extends EnumBase {
    private static final long serialVersionUID = 1L;

    public static final UserSubjectTypeEnumType WINDOWS_USER_SUBJECT_TYPE = new UserSubjectTypeEnumType(
        UserReservedFieldEnumType.WINDOWS_SID.getName()
    ) {
        private static final long serialVersionUID = 1L;
    };
    public static final UserSubjectTypeEnumType LINUX_USER_SUBJECT_TYPE = new UserSubjectTypeEnumType(
        UserReservedFieldEnumType.UNIX_ID.getName()
    ) {
        private static final long serialVersionUID = 1L;
    };

    private UserSubjectTypeEnumType(String name) {
        super(name, UserSubjectTypeEnumType.class);
    }

    /**
     * Retrieve the user subject type for the specified user subject type id
     * @param nextUserSubjectId the user subject type id associate with the user subject type to retrieve
     * @return The user subject type for the specified user subject type id
     */
    public static UserSubjectTypeEnumType getUserSubjectType(String nextUserSubjectId) {
        return getElement(nextUserSubjectId, UserSubjectTypeEnumType.class);
    }
}
