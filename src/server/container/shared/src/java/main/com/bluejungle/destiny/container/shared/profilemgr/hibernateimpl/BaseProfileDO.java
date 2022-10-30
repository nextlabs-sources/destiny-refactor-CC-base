/*
 * Created on Nov 10, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.bluejungle.destiny.container.shared.profilemgr.IBaseProfileDO;

/**
 * Base Profile Data Object. Provided base functionality for Agent, User, and
 * Communication profiles Data Object implementations
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/data/BaseProfileDO.java#3 $
 */
public abstract class BaseProfileDO implements IBaseProfileDO {

    private Long id;
    private boolean isDefault;
    private String name;
    private Calendar createdDate;
    private Calendar modifiedDate;

    /**
     * Empty constructor. For Hibernate use only
     */
    BaseProfileDO() {
    }

    /**
     * Create a BaseProfileDO with the given name
     * 
     * @param name
     *            The name of the profile
     * @throws An
     *             IllegalArgumentException if the provided name is null,
     *             contains a space, or is greater than 128 characters
     */
    BaseProfileDO(String name) {
        validateName(name);

        this.name = name;
        this.createdDate = new GregorianCalendar();
        this.modifiedDate = createdDate;
    }

    /**
     * Create a BaseProfileDO instance
     * 
     * @param id
     *            the id of the profile
     * @param name
     *            the name of the profile
     * @param createdDate
     *            the created date of the profile
     * @param modifiedDate
     *            the modified date of the profile
     */
    BaseProfileDO(Long id, String name, boolean isDefault, Calendar createdDate, Calendar modifiedDate) {
        validateName(name);

        if (createdDate == null) {
            throw new IllegalArgumentException("createdDate cannot be null");
        }

        if (modifiedDate == null) {
            throw new IllegalArgumentException("modifiedDate cannot be null");
        }

        this.id = id;
        this.isDefault = isDefault;
        this.name = name;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    /**
     * Validate the profile name
     * 
     * @param name
     *            the name to validate
     */
    private void validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The profile name cannot be null");
        }

        if (name.length() > 128) {
            throw new IllegalArgumentException("A profile name cannot be longer than 128 characters");
        }
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    
    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IBaseProfileDO#isDefault()
     */
    public boolean isDefault() {
        return this.isDefault;
    }
    
    /**
     * Set this profile as the default
     * 
     * @param isDefault true to be the default; false otherwise
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * Returns the name.
     * 
     * @return the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name
     * 
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the createdDate.
     * 
     * @return the createdDate.
     */
    public Calendar getCreatedDate() {
        return this.createdDate;
    }

    /**
     * Returns the modifiedDate.
     * 
     * @return the modifiedDate.
     */
    public Calendar getModifiedDate() {
        return this.modifiedDate;
    }

    /**
     * Set the modified date to the current date.
     */
    public void setModifiedDate() {
        this.modifiedDate = new GregorianCalendar();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object baseProfileToCompare) {
        boolean valueToReturn = false;

        if (baseProfileToCompare == this) {
            valueToReturn = true;
        } else if ((baseProfileToCompare != null) && ((baseProfileToCompare instanceof IBaseProfileDO))) {
            valueToReturn = this.getName().equals(((IBaseProfileDO) baseProfileToCompare).getName());
        }

        return valueToReturn;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.getName().hashCode();
    }
}