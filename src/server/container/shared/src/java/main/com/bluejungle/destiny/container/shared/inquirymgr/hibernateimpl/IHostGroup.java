/*
 * Created on Mar 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.framework.utils.TimeRelation;

import java.util.Set;

/**
 * This is the host group interface
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IHostGroup.java#2 $
 */

public interface IHostGroup {

    /**
     * Returns the id of the group
     * 
     * @return the id of the group
     */
    public Long getId();

    /**
     * Returns the id of the group
     * 
     * @return the id of the group
     */
    public Long getOriginalId();

    /**
     * Returns the group name
     * 
     * @return the group name
     */
    public String getName();

    /**
     * Retrieve the time relation for this host group
     * 
     * @return the time relation for this host group
     */
    public TimeRelation getTimeRelation();
}