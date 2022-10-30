/*
 * Created on Feb 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IPropertyDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/DataSourcePropertyDO.java#1 $
 */

public class PropertyDO implements IPropertyDO {

    /*
     * Private variables
     */
    private String name;
    private String value;

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IPropertyDO#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name
     * 
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value
     * 
     * @param value
     *            The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IPropertyDO#getValue()
     */
    public String getValue() {
        return this.value;
    }
}