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
 * This class represents the field that can be sorted on for a user class search
 * specification.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/UserClassMgrSortFieldType.java#1 $
 */

public class UserClassMgrSortFieldType extends EnumBase {

    public static final UserClassMgrSortFieldType DISPLAY_NAME = new UserClassMgrSortFieldType("displayName");
    public static final UserClassMgrSortFieldType NAME = new UserClassMgrSortFieldType("name");

    /**
     * Constructor
     * 
     * @param name
     *            name of the user class sort field
     */
    private UserClassMgrSortFieldType(String name) {
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
    public static UserClassMgrSortFieldType getUserClassMgrSortFieldType(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }
        return getElement(name, UserClassMgrSortFieldType.class);
    }
}