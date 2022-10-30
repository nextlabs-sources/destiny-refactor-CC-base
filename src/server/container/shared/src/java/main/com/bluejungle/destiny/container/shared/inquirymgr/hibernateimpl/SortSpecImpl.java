/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec;
import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the implementation of the sort spec class. Since the sort
 * specification can be either in memory or persisted, and because the two
 * implementation are indentical, there is no need to have a "DO" class for sort
 * spec.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/SortSpecImpl.java#1 $
 */

public class SortSpecImpl implements ISortSpec {

    private SortDirectionType sortDirection;
    private SortFieldType sortField;

    /**
     * Constructor
     */
    public SortSpecImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec#getSortDirection()
     */
    public SortDirectionType getSortDirection() {
        return this.sortDirection;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec#getSortField()
     */
    public SortFieldType getSortField() {
        return this.sortField;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec#setSortDirection(com.bluejungle.destiny.container.shared.inquirymgr.ISortDirection)
     */
    public void setSortDirection(SortDirectionType newSortDirection) {
        this.sortDirection = newSortDirection;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec#setSortField(com.bluejungle.destiny.container.shared.inquirymgr.ISortFieldType)
     */
    public void setSortField(SortFieldType type) {
        this.sortField = type;
    }
}