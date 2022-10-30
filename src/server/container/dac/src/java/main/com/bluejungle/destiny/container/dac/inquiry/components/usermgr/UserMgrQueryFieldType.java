/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.usermgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This class represents the field that can be queried for a user search
 * specification. A user search specification can only be done on one of these
 * fields.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/UserMgrQueryFieldType.java#1 $
 */

public class UserMgrQueryFieldType extends EnumBase {

    public static final UserMgrQueryFieldType NONE = new UserMgrQueryFieldType("None");
    public static final UserMgrQueryFieldType FIRST_NAME = new UserMgrQueryFieldType("FirstName");
    public static final UserMgrQueryFieldType LAST_NAME = new UserMgrQueryFieldType("LastName");
    public static final UserMgrQueryFieldType USER_ID = new UserMgrQueryFieldType("UserId");

    /**
     * Constructor
     * 
     * @param name
     *            name of the userQueryFieldType
     */
    private UserMgrQueryFieldType(String name) {
        super(name);
    }
}