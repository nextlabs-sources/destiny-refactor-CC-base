/*
 * Created on Oct 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/IGroup.java#1 $
 */

public interface IGroup extends IDomainEntity {

    /**
     * Returns the title of this external group
     * 
     * @return title
     */
    public String getTitle();
}