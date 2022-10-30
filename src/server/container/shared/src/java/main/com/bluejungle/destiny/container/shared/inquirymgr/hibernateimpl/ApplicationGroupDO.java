/*
 * Created on Mar 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashSet;
import java.util.Set;

/**
 * This is the application group data object. It represents one group of
 * applications.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ApplicationGroupDO.java#1 $
 */

public class ApplicationGroupDO implements IApplicationGroup {

    private Long id;
    private String name;
    private Set applications = new HashSet();

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IApplicationGroup#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IApplicationGroup#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IApplicationGroup#getApplications()
     */
    public Set getApplications() {
        return this.applications;
    }

    /**
     * Sets the user id
     * 
     * @param newId
     *            new user id to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the group name
     * 
     * @param newName
     *            name of the group to set
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Sets the applications in the group
     * 
     * @param newApps
     *            applications to set. This is a set of IApplication objects
     */
    public void setApplications(Set newApps) {
        this.applications = newApps;
    }
}