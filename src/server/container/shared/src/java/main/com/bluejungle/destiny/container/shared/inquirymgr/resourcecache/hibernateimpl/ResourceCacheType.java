/*
 * Created on Mar 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This is the resource cache type class. The resource cache type enumerates the
 * various types of resource cache entries.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/ResourceCacheType.java#1 $
 */

public abstract class ResourceCacheType extends EnumBase {

    public static final ResourceCacheType APPLICATION = new ResourceCacheType("Application") {
    };
    public static final ResourceCacheType HOST = new ResourceCacheType("Host") {
    };
    public static final ResourceCacheType POLICY = new ResourceCacheType("Policy") {
    };
    public static final ResourceCacheType USER = new ResourceCacheType("User") {
    };

    /**
     * The constructor is private to prevent unwanted instanciations from the
     * outside.
     * 
     * @param name
     *            is passed through to the constructor of the superclass.
     */
    private ResourceCacheType(String name) {
        super(name);
    }
}