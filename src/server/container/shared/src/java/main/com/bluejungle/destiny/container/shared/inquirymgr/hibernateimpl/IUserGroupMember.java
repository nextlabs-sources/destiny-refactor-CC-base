/*
 * Created on Mar 19, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.framework.utils.TimeRelation;

/**
 * This is the user group member relationship interface. This interface
 * represents the membership of a user within a user group during a particular
 * time period.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IUserGroupMember.java#1 $
 */

public interface IUserGroupMember {

    /**
     * Returns the group id
     * 
     * @return the group id
     */
    public Long getGroupId();

    /**
     * Returns the user id
     * 
     * @return the user id
     */
    public Long getUserId();

    /**
     * Returns the time relation
     * 
     * @return the time relation
     */
    public TimeRelation getTimeRelation();
}
