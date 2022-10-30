/*
 * Created on Mar 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.framework.utils.TimeRelation;

/**
 * This is the host interface. In Destiny, every host has a unique destiny Id
 * assigned by the system.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IHost.java#2 $
 */

public interface IHost {

    /**
     * Returns the host id
     * 
     * @return the host id
     */
    public Long getId();

    /**
     * Returns the origianl id of the host
     * 
     * @return the original id of the host
     */
    public Long getOriginalId();
    
    /**
     * Returns the host name
     * 
     * @return the host name
     */
    public String getName();

    /**
     * Returns the host time relation
     * 
     * @return the host time relation
     */
    public TimeRelation getTimeRelation();
}
