/*
 * Created on Aug 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the stored query result manager interface
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/IStoredQueryMgr.java#1 $
 */

public interface IStoredQueryMgr {

    /**
     * Activity data source config parameter
     */
    PropertyKey<IHibernateRepository> ACTIVITY_DATA_SOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("activityDS");

    /**
     * Component name
     */
    String COMP_NAME = "StoredQueryMgr";

    /**
     * Configuration parameter for expiration amount
     */
    PropertyKey<Long> DATA_EXPIRATION_AMOUNT_CONFIG_PARAM = new PropertyKey<Long>("expiration");

    /**
     * Configuration parameter for sleep time between cleanups
     */
    PropertyKey<Long> SLEEP_AMOUNT_CONFIG_PARAM = new PropertyKey<Long>("sleep");
}