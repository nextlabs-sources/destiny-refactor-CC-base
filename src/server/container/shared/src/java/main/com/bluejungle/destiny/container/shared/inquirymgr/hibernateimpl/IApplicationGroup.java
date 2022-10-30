/*
 * Created on Mar 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Set;

/**
 * This is the application group interface.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IApplicationGroup.java#1 $
 */

public interface IApplicationGroup {

    /**
     * Returns the id of the group
     * 
     * @return the id of the group
     */
    public Long getId();

    /**
     * Returns the group name
     * 
     * @return the group name
     */
    public String getName();

    /**
     * Returns the set of applications belonging to this group
     * 
     * @return a set of objects implementing IApplication
     */
    public Set getApplications();
}