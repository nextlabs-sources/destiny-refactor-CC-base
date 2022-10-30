/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

/**
 * This interface represents a property of the data source. This is generally to
 * override a system-property for this datasource, for debugging purposes.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/IPropertyDO.java#1 $
 */

public interface IPropertyDO /* extends IDomainObject */{

    /**
     * Returns the name of this property
     * 
     * @return name
     */
    public String getName();

    /**
     * Returns the value of this property
     * 
     * @return value
     */
    public String getValue();
}