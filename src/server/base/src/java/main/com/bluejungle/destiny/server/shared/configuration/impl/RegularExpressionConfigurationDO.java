/*
 * Created on Oct 29, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Next Labs Inc.,
 * San Mateo CA, Ownership remains with Next Labs Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IRegularExpressionConfigurationDO;

/**
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/RegularExpressionConfigurationDO.java#1 $
 */

public class RegularExpressionConfigurationDO implements IRegularExpressionConfigurationDO {
    /*
     * Private variables
     */
    private String name;
    private String value;

    /**
     * Constructor
     */
    public RegularExpressionConfigurationDO() {
    }

    public RegularExpressionConfigurationDO(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IRegularExpressionConfigurationDO#getName()
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
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IRegularExpressionConfigurationDO#getValue()
     */
    public String getValue() {
        return this.value;
    }
}
