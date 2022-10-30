/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

/**
 * This is the resource information data object implementation. It contains
 * various information about a "from resource" from the file system.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/FromResourceInformationDO.java#1 $
 */

public class FromResourceInformationDO extends BaseResourceInformationDO implements IFromResourceInformation {

    private Calendar createdDate;
    private Calendar modifiedDate;
    private Long size;
    private String ownerId;

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IResourceInformation#getCreatedDate()
     */
    public Calendar getCreatedDate() {
        return this.createdDate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IResourceInformation#getModifiedDate()
     */
    public Calendar getModifiedDate() {
        return this.modifiedDate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IResourceInformation#getSize()
     */
    public Long getSize() {
        return this.size;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IResourceInformation#getOwnerId()
     */
    public String getOwnerId() {
        return this.ownerId;
    }

    /**
     * Sets the created date
     * 
     * @param newDate
     *            new date to set
     */
    public void setCreatedDate(Calendar newDate) {
        this.createdDate = newDate;
    }

    /**
     * Sets the modified date
     * 
     * @param newDate
     *            new date to set
     */
    public void setModifiedDate(Calendar newDate) {
        this.modifiedDate = newDate;
    }

    /**
     * Sets the new size
     * 
     * @param newSize
     *            new size to set
     */
    public void setSize(Long newSize) {
        this.size = newSize;
    }

    /**
     * Sets the new owner id
     * 
     * @param newId
     *            new owner id to set
     */
    public void setOwnerId(String newId) {
        this.ownerId = newId;
    }
}
