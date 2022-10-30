/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the interface for a persistent report manager sort term.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/IPolicyMgrSortTerm.java#1 $
 */

public interface IPersistentReportMgrSortTerm {

    /**
     * Returns the field type to query on
     * 
     * @return the field type to query on
     */
    public PersistentReportMgrSortFieldType getFieldName();

    /**
     * Returns the sort direction for the field
     * 
     * @return the sort direction for the field
     */
    public SortDirectionType getDirection();
}