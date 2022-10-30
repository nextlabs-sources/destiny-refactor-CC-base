/*
 * Created on Mar 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.framework.utils.TimeRelation;

/**
 * This is the group of users data object.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/UserGroupDO.java#1 $
 */

public class UserGroupDO implements IUserGroup {

    private Long originalId;
    private Long id;
    private String displayName;
    private String name;
    private TimeRelation timeRelation;
    String enrollmentType;

    /**
     * Constructor
     * 
     */
    public UserGroupDO() {
        super();
    }

    /**
     * Copy Constructor
     * 
     * @param groupToCopy
     *            user group object to copy
     */
    public UserGroupDO(UserGroupDO groupToCopy) {
        setDisplayName(groupToCopy.getDisplayName());
        setId(groupToCopy.getId());
        setName(groupToCopy.getName());
        setOriginalId(groupToCopy.getOriginalId());
        setTimeRelation(groupToCopy.getTimeRelation());
        setEnrollmentType(groupToCopy.getEnrollmentType());
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroup#getDisplayName()
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroup#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroup#getOriginalId()
     */
    public Long getOriginalId() {
        return this.originalId;
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
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroup#getTimeRelation()
     */
    public TimeRelation getTimeRelation() {
        return this.timeRelation;
    }

    /**
     * Sets the user id
     * 
     * @param newId
     *            new user id to set
     */
    public void setOriginalId(Long newId) {
        this.originalId = newId;
    }

    /**
     * Sets the group display name
     * 
     * @param newDisplayName
     *            new display name to set
     */
    public void setDisplayName(String newDisplayName) {
        this.displayName = newDisplayName;
    }

    /**
     * Sets the group name
     * 
     * @param newName
     *            name of the group to set
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Sets the sequence id
     * 
     * @param newId
     *            new sequence id to set
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
     * Returns the enrollmentType.
     * @return the enrollmentType.
     */
    public String getEnrollmentType() {
        return this.enrollmentType;
    }

    
    /**
     * Sets the enrollmentType
     * @param enrollmentType The enrollmentType to set.
     */
    public void setEnrollmentType(String enrollmentType) {
        this.enrollmentType = enrollmentType;
    }           
}