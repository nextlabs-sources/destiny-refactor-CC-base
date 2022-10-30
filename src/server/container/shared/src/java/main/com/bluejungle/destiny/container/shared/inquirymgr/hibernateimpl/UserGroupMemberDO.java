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
 * This is the user membership to a user group class implementation. Each
 * instance of this class represents the membership of a user within a usergroup
 * during a particular time period.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/UserGroupMemberDO.java#1 $
 */

public class UserGroupMemberDO implements IUserGroupMember {

    private Long groupId;
    private Long id;
    private TimeRelation timeRelation;
    private Long userId;

    /**
     * Constructor
     */
    public UserGroupMemberDO() {
        super();
    }

    /**
     * Copy Constructor
     * 
     * @param memberToCopy
     *            member object to copy
     */
    public UserGroupMemberDO(UserGroupMemberDO memberToCopy) {
        super();
        setGroupId(memberToCopy.getGroupId());
        setId(memberToCopy.getId());
        setTimeRelation(memberToCopy.getTimeRelation());
        setUserId(memberToCopy.getUserId());
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroupMember#getGroupId()
     */
    public Long getGroupId() {
        return this.groupId;
    }

    /**
     * Returns the sequence id
     * 
     * @return the sequence id
     */
    private Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroupMember#getTimeRelation()
     */
    public TimeRelation getTimeRelation() {
        return this.timeRelation;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroupMember#getUserId()
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Sets the group id
     * 
     * @param groupId
     *            group id to set
     */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Sets the sequence id
     * 
     * @param newId
     *            the sequence id to set
     */
    private void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the time relation
     * 
     * @param timeRelation
     *            time relation to set
     */
    public void setTimeRelation(TimeRelation timeRelation) {
        this.timeRelation = timeRelation;
    }

    /**
     * Sets the user id
     * 
     * @param userId
     *            user id to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
