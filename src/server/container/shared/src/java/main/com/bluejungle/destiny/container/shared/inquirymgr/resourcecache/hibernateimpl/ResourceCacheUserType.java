/*
 * Created on Mar 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.sql.Types;

import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/ResourceCacheUserType.java#1 $
 */

public class ResourceCacheUserType extends EnumUserType<ResourceCacheType> {

    /**
     * ReportSummaryUserType is saved as char(1)
     */
    private static int[] SQL_TYPES = { Types.CHAR };

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * Constructor
     */
    public ResourceCacheUserType() {
        super(new ResourceCacheType[] { 
        			ResourceCacheType.APPLICATION, 
        			ResourceCacheType.HOST, 
        			ResourceCacheType.POLICY, 
        			ResourceCacheType.USER }, 
        		new String[] { "A", "H", "P", "U" }, 
        		ResourceCacheType.class);
    }
}