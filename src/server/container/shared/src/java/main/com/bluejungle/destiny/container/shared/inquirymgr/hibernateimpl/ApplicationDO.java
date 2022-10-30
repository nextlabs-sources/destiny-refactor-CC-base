/*
 * Created on Mar 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.framework.utils.TimeRelation;

/**
 * This is the application data object. It represents an application object in
 * the system.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ApplicationDO.java#1 $
 */

public class ApplicationDO implements IApplication {

    private Set groups = new HashSet();
    private Long id;
    private Long originalId;
    private String name;
    private TimeRelation timeRelation;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IApplication#getGroups()
     */
    public Set getGroups() {
        return this.groups;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IApplication#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser#getOriginalId()
     */
    public Long getOriginalId() {
        return this.originalId;
    }
    
    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IApplication#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IApplication#getTimeRelation()
     */
    public TimeRelation getTimeRelation() {
        return this.timeRelation;
    }

    /**
     * Sets the new groups the application belong to
     * 
     * @param newGroups
     *            set of groups
     */
    public void setGroups(Set newGroups) {
        this.groups = newGroups;
    }

    /**
     * Sets the application id
     * 
     * @param newId
     *            id of the application to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the application original id
     * 
     * @param newId
     *            new user id to set
     */
    public void setOriginalId(Long newId) {
        this.originalId = newId;
    }
    
    /**
     * Sets the application name
     * 
     * @param newName
     *            name of the application to set
     */
    public void setName(String newName) {
        this.name = newName;
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
}
