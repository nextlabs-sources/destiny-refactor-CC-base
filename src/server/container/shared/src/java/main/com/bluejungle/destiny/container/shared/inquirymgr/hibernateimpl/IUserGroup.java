/*
 * Created on Mar 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.framework.utils.TimeRelation;

import java.util.Set;

/**
 * This is the interface for a group of user. Since the user group information
 * will be loaded in "bulk" from the database, we do not need API to add users
 * one by one to the group.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IUserGroup.java#1 $
 */

public interface IUserGroup {

    /**
     * Returns the group display name
     * 
     * @return the group display name
     */
    public String getDisplayName();

    /**
     * Returns the group name
     * 
     * @return the group name
     */
    public String getName();

    /**
     * Returns the id of the group
     * 
     * @return the id of the group
     */
    public Long getOriginalId();

    /**
     * Returns the user group time relation
     * 
     * @return the user group time relation
     */
    public TimeRelation getTimeRelation();
    
    /**
     * Returns the enrollment type of the user group
     * 
     * @return the enrollment type of the user group
     */
    public String getEnrollmentType();
}
