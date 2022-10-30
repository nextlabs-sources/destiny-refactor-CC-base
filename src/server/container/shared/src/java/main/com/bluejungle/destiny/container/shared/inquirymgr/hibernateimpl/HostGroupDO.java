/*
 * Created on Mar 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.framework.utils.TimeRelation;

/**
 * This is the host group data object. It represents one group of hosts.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/HostGroupDO.java#1 $
 */

public class HostGroupDO implements IHostGroup {

    private Long id;
    private Long originalId;
    private String name;
    private TimeRelation timeRelation;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHostGroup#getId()
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
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHostGroup#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHostGroup#getTimeRelation()
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
     * Sets the group name
     * 
     * @param newName
     *            name of the group to set
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
    private void setTimeRelation(TimeRelation timeRelation) {
        this.timeRelation = timeRelation;
    }
}