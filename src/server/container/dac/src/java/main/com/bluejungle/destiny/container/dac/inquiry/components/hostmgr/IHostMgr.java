/*
 * Created on May 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.hostmgr;

import java.util.List;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This interface represents an implementation of the host manager. The host
 * manager allows fetching the list of hosts from the database, based on some
 * simple (optional) query specifications.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/IHostMgr.java#1 $
 */

public interface IHostMgr {

    String COMP_NAME = "HostMgr";
    PropertyKey<IHibernateRepository> DATASOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("DataSource");
    
    /**
     * Returns the list of hosts, according to a given (optional) search spec or
     * sort spec.
     * 
     * @param querySpec
     *            the query specification (null if no query spec)
     * @return the list of matching hosts
     */
    List<IHost> getHosts(IHostMgrQuerySpec querySpec) throws DataSourceException;
}
