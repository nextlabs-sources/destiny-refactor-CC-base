/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.PersistentReportMgrSortFieldType;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * Persistent report manager sort term implementation class
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PersistentReportMgrSortTermImpl.java#1 $
 */

public class PersistentReportMgrSortTermImpl implements IPersistentReportMgrSortTerm {

    private PersistentReportMgrSortFieldType fieldName;
    private SortDirectionType direction;

    /**
     * 
     * Constructor
     * 
     * @param newField
     *            field to sort on
     * @param newDirection
     *            direction to sort on
     */
    private PersistentReportMgrSortTermImpl(PersistentReportMgrSortFieldType newField, SortDirectionType newDirection) {
        if (newDirection == null) {
            throw new NullPointerException("direction cannot be null");
        }
        if (newField == null) {
            throw new NullPointerException("field name cannot be null");
        }
        this.direction = newDirection;
        this.fieldName = newField;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm#getFieldName()
     */
    public PersistentReportMgrSortFieldType getFieldName() {
        return this.fieldName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm#getDirection()
     */
    public SortDirectionType getDirection() {
        return this.direction;
    }
}