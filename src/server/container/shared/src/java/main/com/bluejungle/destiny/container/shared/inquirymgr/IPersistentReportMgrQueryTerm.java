/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This interface represents a query term for a persistent report query.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/IPolicyMgrQueryTerm.java#1 $
 */

public interface IPersistentReportMgrQueryTerm {

    /**
     * Returns the field name to query on.
     * 
     * @return the field name to query on.
     */
    public PersistentReportMgrQueryFieldType getFieldName();

    /**
     * Returns the query expression for the field
     * 
     * @return the query expression for the field
     */
    public String getExpression();
}