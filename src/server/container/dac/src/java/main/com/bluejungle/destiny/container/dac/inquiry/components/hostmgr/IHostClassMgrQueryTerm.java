/*
 * Created on May 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.hostmgr;

/**
 * This is the host class query term interface for the host class manager.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/IHostClassMgrQueryTerm.java#1 $
 */

public interface IHostClassMgrQueryTerm {

    /**
     * Returns the field name to query on.
     * 
     * @return the field name to query on.
     */
    public HostClassMgrQueryFieldType getFieldName();

    /**
     * Returns the query expression for the field
     * 
     * @return the query expression for the field
     */
    public String getExpression();
}