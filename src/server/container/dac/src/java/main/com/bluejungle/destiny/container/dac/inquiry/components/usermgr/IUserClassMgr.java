/*
 * Created on Apr 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.usermgr;

import java.util.List;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroup;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This interface represents an implementation of the user class manager. The
 * user class manager allows fetching the list of user classes from the
 * database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/IUserClassMgr.java#1 $
 */

public interface IUserClassMgr {

    String COMP_NAME = "UserClassMgr";
    PropertyKey<IHibernateRepository> DATASOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("DataSource");

    /**
     * Returns the list of user classes
     * 
     * @param querySpec
     *            the query specification (null if no query spec)
     * @return the list of matching users
     */
    List<IUserGroup> getUserClasses(IUserClassMgrQuerySpec querySpec) throws DataSourceException;
}