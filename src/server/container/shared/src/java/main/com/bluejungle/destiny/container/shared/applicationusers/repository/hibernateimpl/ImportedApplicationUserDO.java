/*
 * Created on May 31, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/ImportedApplicationUserDO.java#1 $
 */

public class ImportedApplicationUserDO extends BaseApplicationUserDO {

    
    /**
     * Create an instance of ImportedApplicationUserDO
     * @param login
     * @param firstName
     * @param lastName
     * @param domainDO
     */
    ImportedApplicationUserDO(String login, String firstName, String lastName, AccessDomainDO accessDomain) {
        super(login, firstName, lastName, accessDomain);
    }
    
    /**
     * Create an instance of ImportedApplicationUserDO.  For Hibernate User Only
     */
    ImportedApplicationUserDO() {
        
    }
}
