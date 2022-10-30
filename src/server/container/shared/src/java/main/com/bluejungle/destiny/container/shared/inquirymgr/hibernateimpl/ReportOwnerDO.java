/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportOwner;

/**
 * This is the report owner data object. It stores information about the report
 * ownership.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportOwnerDO.java#1 $
 */

public class ReportOwnerDO implements IReportOwner {

    private boolean shared;
    private Long ownerId;

    /**
     * Constructor
     */
    public ReportOwnerDO() {
        super();
    }

    /**
     * Constructor
     * 
     * @param owner
     *            owner to copy
     */
    public ReportOwnerDO(IReportOwner owner) {
        if (owner == null) {
            throw new NullPointerException("owner cannot be null");
        }
        setIsShared(owner.getIsShared());
        setOwnerId(owner.getOwnerId());
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportOwner#getIsShared()
     */
    public boolean getIsShared() {
        return this.shared;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportOwner#getOwnerId()
     */
    public Long getOwnerId() {
        return this.ownerId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportOwner#setIsShared(boolean)
     */
    public void setIsShared(boolean shared) {
        this.shared = shared;
    }

    /**
     * Sets the owner name
     * 
     * @param newOwner
     *            new owner id to set
     */
    public void setOwnerId(Long newId) {
        this.ownerId = newId;
    }
}