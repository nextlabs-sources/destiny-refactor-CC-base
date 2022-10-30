/*
 * Created on Mar 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IResourceCacheState;

/**
 * This is the data object for the resource cache state. This data object
 * exposes the state of a resource cache.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/ResourceCacheStateDO.java#1 $
 */

class ResourceCacheStateDO implements IResourceCacheState {

    private Long id;
    private Calendar lastUpdated;
    private ResourceCacheType type;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.resourceCache.hibernateimpl.IResourceCacheState#getOriginalId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.resourceCache.hibernateimpl.IResourceCacheState#getLastUpdated()
     */
    public Calendar getLastUpdated() {
        return this.lastUpdated;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.resourceCache.hibernateimpl.IResourceCacheState#getType()
     */
    public ResourceCacheType getType() {
        return this.type;
    }

    /**
     * Sets the id for the resource cache state record.
     * 
     * @param newId
     *            new id to set
     */
    protected void setId(Long newId) {
        this.id = newId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.resourceCache.hibernateimpl.IResourceCacheState#setLastUpdated(java.util.Calendar)
     */
    protected void setLastUpdated(Calendar newExpiration) {
        this.lastUpdated = newExpiration;
    }

    /**
     * Sets the resource cache state type.
     * 
     * @param newType
     *            new type to set
     */
    protected void setType(ResourceCacheType newType) {
        this.type = newType;
    }
}