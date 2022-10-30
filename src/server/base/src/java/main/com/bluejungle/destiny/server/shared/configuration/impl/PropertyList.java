/*
 * Created on Feb 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.Properties;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/DataSourcePropertyList.java#1 $
 */

public class PropertyList {

    private Properties props;

    public PropertyList(Properties props) {
        super();
        this.props = props;
    }
    
    /**
     * Constructor
     */
    public PropertyList() {
        this(new Properties());
    }

    /**
     * Adds a property to the list of properties maintained by this object
     * 
     * @param property
     */
    public void addProperty(PropertyDO property) {
        this.props.setProperty(property.getName(), property.getValue());
    }

    /**
     * Returns the list of properties
     * 
     * @return the list of properties
     */
    public Properties getProperties() {
        return this.props;
    }
}