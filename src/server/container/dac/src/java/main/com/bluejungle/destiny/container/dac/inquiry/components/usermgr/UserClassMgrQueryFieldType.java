/*
 * Created on Apr 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.usermgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This class represents the field that can be queried for a user class search
 * specification. A user class search specification can only be done on one of
 * these fields.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/UserClassMgrQueryFieldType.java#1 $
 */

public class UserClassMgrQueryFieldType extends EnumBase {

    public static final UserClassMgrQueryFieldType DISPLAY_NAME = new UserClassMgrQueryFieldType("displayName");
    public static final UserClassMgrQueryFieldType NAME = new UserClassMgrQueryFieldType("name");

    /**
     * Constructor
     * 
     * @param name
     *            name of the user class field
     */
    private UserClassMgrQueryFieldType(String name) {
        super(name);
    }

    /**
     * Retrieve a UserClassMgrQueryFieldType instance by name
     * 
     * @param name
     *            the name of the UserClassMgrQueryFieldType
     * @return the UserClassMgrQueryFieldType associated with the provided name
     * @throws IllegalArgumentException
     *             if no UserClassMgrQueryFieldType exists with the specified
     *             name
     */
    public static UserClassMgrQueryFieldType getUserClassMgrQueryFieldType(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }
        return getElement(name, UserClassMgrQueryFieldType.class);
    }
}