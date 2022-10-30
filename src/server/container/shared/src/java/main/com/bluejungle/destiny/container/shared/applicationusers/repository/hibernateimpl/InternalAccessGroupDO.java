/*
 * Created on May 31, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/InternalAccessGroupDO.java#1 $
 */

public class InternalAccessGroupDO extends BaseAccessGroupDO implements IInternalAccessGroup {

    /**
     * Create an instance of InternalAccessGroupDO
     * @param title
     * @param description
     * @param accessDomain
     */
    InternalAccessGroupDO(String title, String description, AccessDomainDO accessDomain) {
        super(title, description, accessDomain);
    }

    /**
     * Create an instance of InternalAccessGroupDO.  For Hibernate use only
     */
    InternalAccessGroupDO() {
    }

}
