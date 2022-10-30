/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr;

import java.util.Set;

import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This interface represents an implementation of the resource class manager.
 * Resource classes are provided by the Policy Framework, and ordered in memory
 * for the end user.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/resourceclassmgr/IResourceClassMgr.java#1 $
 */

public interface IResourceClassMgr {

    public static final String COMP_NAME = "ResourceClassMgr";
    public static final String DATASOURCE_CONFIG_PARAM = "DataSource";

    /**
     * Returns the list of resource classes
     * 
     * @param querySpec
     *            the query specification (null if no query spec)
     * @return the set of matching resource classes
     */
    public Set getResourceClasses(IResourceClassMgrQuerySpec querySpec) throws DataSourceException;
}