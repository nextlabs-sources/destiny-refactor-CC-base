/*
 * Created on May 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.hostmgr;

import java.util.List;

import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This interface represents an implementation of the host class manager. The
 * host class manager allows fetching the list of host classes from the
 * database.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/IHostClassMgr.java#1 $
 */

public interface IHostClassMgr {

    public static final String COMP_NAME = "HostClassMgr";
    public static final String DATASOURCE_CONFIG_PARAM = "DataSource";

    /**
     * Returns the list of host classes
     * 
     * @param querySpec
     *            the query specification (null if no query spec)
     * @return the list of matching hosts
     */
    public List getHostClasses(IHostClassMgrQuerySpec querySpec) throws DataSourceException;
}