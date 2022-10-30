/*
 * Created on Sep 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/IDomainEntity.java#1 $
 */

public interface IDomainEntity {

    /**
     * Returns the name of the domain to which this entity belongs
     * 
     * @return domain name
     */
    public String getDomainName();

}