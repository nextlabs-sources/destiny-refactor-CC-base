/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.policymgr;

import java.util.List;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is the policy manager component interface. This component allows
 * retrieving data about existing policies in the system.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/IPolicyMgr.java#1 $
 */

public interface IPolicyMgr {

    String COMP_NAME = "PolicyMgr";
    PropertyKey<IHibernateRepository> DATASOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("DataSource");
    
    /**
     * Returns the list of policies, according to a given (optional) search spec
     * or sort spec.
     * 
     * @param querySpec
     *            the query specification (null if no query spec)
     * @return the list of matching policies
     */
    List<IPolicy> getPolicies(IPolicyMgrQuerySpec querySpec) throws DataSourceException;

}
