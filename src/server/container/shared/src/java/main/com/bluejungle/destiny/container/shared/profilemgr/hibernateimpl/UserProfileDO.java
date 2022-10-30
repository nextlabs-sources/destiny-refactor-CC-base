/*
 * Created on Oct 28, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.profilemgr.IUserProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IUserProfileData;

/**
 * A User Profile Data Object Implementation
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/hibernateimpl/UserProfileDO.java#1 $
 */
public class UserProfileDO extends BaseProfileDO implements IUserProfileDO {

    /**
     * Empty Constructor. For Hibernate use only
     */
    public UserProfileDO() {
    }

    /**
     * Create a UserProfileDO with the specified IUserProfileData
     */
    UserProfileDO(IUserProfileData userProfileData) {
        super(userProfileData.getName());
    }

    /**
     * Create a UserProfileDO instance
     * 
     * @param id
     *            the id of the User Profile
     * @param name
     *            the name of the User Profile
     * @param isDefault
     *            true if this is the default user profile; false otherwise
     * @param createdDate
     *            the created date of the User Profile
     * @param modifiedDate
     *            the modified date of the User Profile
     */
    public UserProfileDO(Long id, String name, boolean isDefault, Calendar createdDate, Calendar modifiedDate) {
        super(id, name, isDefault, createdDate, modifiedDate);
    }
}