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
 * This is the user data object. It represents a user of the Destiny product. A
 * user can belong to one or more multiple user groups.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/UserDO.java#1 $
 */

public class UserDO implements IUser {

    private String displayName;
    private Long originalId;
    private String firstName;
    private String lastName;
    private Long id;
    private String sid;
    private TimeRelation timeRelation;

    /**
     * Constructor
     *  
     */
    public UserDO() {
        super();
    }

    /**
     * 
     * Constructor
     * 
     * @param fn
     *            First name of the user
     * @param ln
     *            Last name of the user
     * @param dn
     *            Display name of the user
     */
    public UserDO(String fn, String ln, String dn) {
        this.firstName = fn;
        this.lastName = ln;
        this.displayName = dn;
    }

    /**
     * Copy Constructor
     * 
     * @param userToCopy
     *            user object to copy
     */
    public UserDO(UserDO userToCopy) {
        setDisplayName(userToCopy.getDisplayName());
        setFirstName(userToCopy.getFirstName());
        setId(userToCopy.getId());
        setLastName(userToCopy.getLastName());
        setOriginalId(userToCopy.getOriginalId());
        setSID(userToCopy.getSID());
        setTimeRelation(userToCopy.getTimeRelation());
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser#getDisplayName()
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser#getFirstName()
     */
    public String getFirstName() {
        return this.firstName;
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
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser#getLastName()
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser#getOriginalId()
     */
    public Long getOriginalId() {
        return this.originalId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser#getSID()
     */
    public String getSID() {
        return this.sid;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser#getTimeRelation()
     */
    public TimeRelation getTimeRelation() {
        return this.timeRelation;
    }

    /**
     * Sets the display name
     * 
     * @param newName
     *            new display name to set
     */
    public void setDisplayName(String newName) {
        this.displayName = newName;
    }

    /**
     * Sets the first name
     * 
     * @param newFirstName
     *            first name to set
     */
    public void setFirstName(String newFirstName) {
        this.firstName = newFirstName;
    }

    /**
     * Sets the last name
     * 
     * @param newLastName
     *            last name to set
     */
    public void setLastName(String newLastName) {
        this.lastName = newLastName;
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
     * Sets the sequence id
     * 
     * @param newId
     *            new sequence id to set
     */
    private void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the user SID
     * 
     * @param newSID
     *            new SID to set
     */
    public void setSID(String newSID) {
        this.sid = newSID;
    }

    /**
     * Sets the time relation
     * 
     * @param timeRelation
     *            the time relation to set
     */
    public void setTimeRelation(TimeRelation timeRelation) {
        this.timeRelation = timeRelation;
    }
}