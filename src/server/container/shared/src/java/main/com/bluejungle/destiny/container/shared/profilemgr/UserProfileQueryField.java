/*
 * Created on Jan 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

/**
 * Enumeration of the UserProfileDO Query Term fields.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/UserProfileQueryField.java#3 $
 */
public class UserProfileQueryField extends BaseProfileQueryFields {

    public static final UserProfileQueryField ID = new UserProfileQueryField("id");
    public static final UserProfileQueryField NAME = new UserProfileQueryField("name");

    /**
     * Create an UserProfileQueryField instance
     * 
     * @param fieldName
     *            the name of the query field
     */
    public UserProfileQueryField(String fieldName) {
        super(fieldName);
    }
}