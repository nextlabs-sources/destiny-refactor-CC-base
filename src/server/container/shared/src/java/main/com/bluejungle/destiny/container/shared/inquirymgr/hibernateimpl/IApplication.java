/*
 * Created on Mar 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Set;

import com.bluejungle.framework.utils.TimeRelation;

/**
 * This is the application interface. Every cached application data object
 * implements this interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IApplication.java#1 $
 */

public interface IApplication {

    /**
     * Returns the set of groups that this application belongs to
     * 
     * @return the set of groups that this application belongs to
     */
    public Set getGroups();

    /**
     * Returns the application id
     * 
     * @return the application id
     */
    public Long getId();

    /**
     * Returns the id of the group
     * 
     * @return the id of the group
     */
    public Long getOriginalId();
    
    /**
     * Returns the application name
     * 
     * @return the application name
     */
    public String getName();

    /**
     * Returns the application time relation
     * 
     * @return the application time relation
     */
    public TimeRelation getTimeRelation();
}
