/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

/**
 * This is the base resource information data object. It contains the base
 * attribute for a resource information.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseResourceInformationDO.java#1 $
 */

public abstract class BaseResourceInformationDO implements IResourceInformation {

    private String name;

    /**
     * Constructor
     */
    public BaseResourceInformationDO() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IResourceInformation#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the resource name
     * 
     * @param newName
     *            new name to set
     */
    public void setName(String newName) {
        this.name = newName;
    }
}
