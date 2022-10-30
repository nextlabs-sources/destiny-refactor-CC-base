/*
 * Created on Mar 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the resource cache interface. A resource cache stores various types
 * of objects for a certain period of time.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/IResourceCache.java#1 $
 */

public interface IResourceCache {

    public static final PropertyKey<IHibernateRepository> ACTIVITY_DATASOURCE_CONFIG_ATTR =
            new PropertyKey<IHibernateRepository>("activityDS");

    /**
     * Refreshes the cache data. The cache implementation should decide how the
     * data should be refreshed, based on the nature of the cached data.
     */
    public void refresh();
}