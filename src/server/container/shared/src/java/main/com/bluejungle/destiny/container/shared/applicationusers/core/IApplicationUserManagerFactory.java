/*
 * Created on Jul 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;


/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/IApplicationUserManagerFactory.java#1 $
 */

public interface IApplicationUserManagerFactory {

    /*
     * Component name
     */
    public static final String COMP_NAME = "ApplicationUserManagerFactory";

    /*
     * Configuration paramters
     */
    // IApplicationUserManagerConfiguration
    public static final PropertyKey<IApplicationUserConfigurationDO> APPLICATION_USER_CONFIGURATION = 
        new PropertyKey<IApplicationUserConfigurationDO>("ApplicationUserConfiguration");

    // IHibernateDataSource
    public static final PropertyKey<IHibernateRepository> MANAGEMENT_REPOSITORY = 
        new PropertyKey<IHibernateRepository>("ManagementRepository");
    
    /**
     * Returns an application user manager
     * 
     * @return
     */
    public IApplicationUserManager getSingleton();
}