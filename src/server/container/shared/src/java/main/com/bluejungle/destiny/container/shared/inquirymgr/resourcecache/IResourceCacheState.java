/*
 * Created on Mar 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl.ResourceCacheType;

/**
 * This is the resource cache state interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/IResourceCacheState.java#1 $
 */

public interface IResourceCacheState {

    /**
     * Returns the id of the cache state record
     * 
     * @return the id of the cache state record
     */
    public Long getId();

    /**
     * Returns the time at which the resource cache was last updated
     * 
     * @return the time at which the resource cache was last updated
     */
    public Calendar getLastUpdated();

    /**
     * Returns the type of resource represented in this cache state.
     * 
     * @return the type of resource represented in this cache state.
     */
    public ResourceCacheType getType();
}