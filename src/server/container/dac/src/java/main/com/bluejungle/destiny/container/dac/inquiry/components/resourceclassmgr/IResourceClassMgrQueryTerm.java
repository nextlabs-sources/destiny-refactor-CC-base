/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr;

/**
 * This is the interface for a query term regarding the resource class manager.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/resourceclassmgr/IResourceClassMgrQueryTerm.java#1 $
 */

public interface IResourceClassMgrQueryTerm {

    /**
     * Returns the field name to query on.
     * 
     * @return the field name to query on.
     */
    public ResourceClassMgrQueryFieldType getFieldName();

    /**
     * Returns the query expression for the field
     * 
     * @return the query expression for the field
     */
    public String getExpression();
}