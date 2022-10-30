/*
 * Created on Jul 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the master resource cache interface
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/IMasterResourceCache.java#2 $
 */

public interface IMasterResourceCache {

    /**
     * Activity datasource configuration attribute
     */
    public static final PropertyKey<IHibernateRepository> ACTIVITY_DATASOURCE_CONFIG_ATTR =
            new PropertyKey<IHibernateRepository>("activityDS");

    /**
     * Perform a full update cycle on all caches
     */
    public void refreshCaches();
}