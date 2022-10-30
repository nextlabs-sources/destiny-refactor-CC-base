/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the sort specification for the inquiry.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/ISortSpec.java#1 $
 */

public interface ISortSpec {

    /**
     * Returns the sorting direction
     * 
     * @return the sorting direction
     */
    public SortDirectionType getSortDirection();

    /**
     * Returns the sorted field
     * 
     * @return the sorted field
     */
    public SortFieldType getSortField();

    /**
     * Sets the sort direction
     * 
     * @param newSortDirection
     *            new sort direction to set
     */
    public void setSortDirection(SortDirectionType newSortDirection);

    /**
     * Sets the field to sort on
     * 
     * @param type
     *            type of the field to sort on
     */
    public void setSortField(SortFieldType type);
}