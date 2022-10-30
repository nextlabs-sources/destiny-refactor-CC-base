/*
 * Created on May 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.entityresolver;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the user resolver interface. The user resolver simply exposes
 * additional configuration for its implementation.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/entityresolver/IUserResolver.java#1 $
 */

public interface IUserResolver extends IEntityResolver {

    /**
     * Name of the configuration parameter providing a data source to resolve
     * input ambiguity.
     */
    PropertyKey<IHibernateRepository> DATA_SOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("dataSource");
}