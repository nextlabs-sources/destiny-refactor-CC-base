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
 * This class represents the field that can be sorted on for a user search
 * specification.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/UserMgrSortFieldType.java#1 $
 */

public class UserMgrSortFieldType extends EnumBase {

    public static final UserMgrSortFieldType NONE = new UserMgrSortFieldType("None");
    public static final UserMgrSortFieldType FIRST_NAME = new UserMgrSortFieldType("FirstName");
    public static final UserMgrSortFieldType LAST_NAME = new UserMgrSortFieldType("LastName");
    public static final UserMgrSortFieldType USER_ID = new UserMgrSortFieldType("UserId");

    /**
     * Constructor
     * 
     * @param name
     *            name of the userQueryFieldType
     */
    private UserMgrSortFieldType(String name) {
        super(name);
    }
}