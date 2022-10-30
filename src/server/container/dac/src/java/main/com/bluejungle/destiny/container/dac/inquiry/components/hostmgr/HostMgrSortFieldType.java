/*
 * Created on May 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.hostmgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This class represents the field that can be sorted on for a host search
 * specification.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/HostMgrSortFieldType.java#1 $
 */

public class HostMgrSortFieldType extends EnumBase {

    public static final HostMgrSortFieldType NONE = new HostMgrSortFieldType("None");
    public static final HostMgrSortFieldType NAME = new HostMgrSortFieldType("Name");

    /**
     * Constructor
     * 
     * @param name
     *            name of the hostQueryFieldType
     */
    private HostMgrSortFieldType(String name) {
        super(name);
    }
}