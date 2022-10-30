/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;

/**
 * This is the base class for report result data objects.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseReportResultDO.java#1 $
 */

abstract class BaseReportResultDO implements IResultData {

    private Long id;

    /**
     * Constructor
     */
    public BaseReportResultDO() {
        super();
    }

    public BaseReportResultDO(Long newId) {
        super();
        setId(newId);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IResultData#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the new record id
     * 
     * @param newId
     *            new id to be set
     */
    protected void setId(Long newId) {
        this.id = newId;
    }
}