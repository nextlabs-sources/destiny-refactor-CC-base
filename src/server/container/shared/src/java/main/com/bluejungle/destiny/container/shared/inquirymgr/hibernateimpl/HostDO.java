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
 * This is the host resource cache data object. It implements the IHost
 * interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/HostDO.java#2 $
 */

public class HostDO implements IHost {

    private Long id;
    private Long originalId;
    private String name;
    private TimeRelation timeRelation;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost#getId()
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
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost#getTimeRelation()
     */
    public TimeRelation getTimeRelation() {
        return this.timeRelation;
    }

    /**
     * Sets the host id
     * 
     * @param newId
     *            id of the host to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the host original id
     * 
     * @param newId
     *            new user id to set
     */
    public void setOriginalId(Long newId) {
        this.originalId = newId;
    }
    
    /**
     * Sets the host name
     * 
     * @param newName
     *            name of the host to set
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
