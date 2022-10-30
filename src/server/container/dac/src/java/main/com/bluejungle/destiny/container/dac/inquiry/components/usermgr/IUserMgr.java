/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.usermgr;

import java.util.List;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This interface represents an implementation of the user manager. The user
 * manager allows fetching the list of users from the database, based on some
 * simple (optional) query specifications.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/IUserMgr.java#1 $
 */

public interface IUserMgr {

    String COMP_NAME = "UserMgr";
    PropertyKey<IHibernateRepository> DATASOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("DataSource");

    /**
     * Returns the list of users, according to a given (optional) search spec or
     * sort spec.
     * 
     * @param querySpec
     *            the query specification (null if no query spec)
     * @return the list of matching users
     */
    List<IUser> getUsers(IUserMgrQuerySpec querySpec) throws DataSourceException;
}
