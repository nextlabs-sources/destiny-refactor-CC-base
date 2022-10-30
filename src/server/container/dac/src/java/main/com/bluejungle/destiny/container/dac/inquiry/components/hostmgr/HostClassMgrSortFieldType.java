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
 * This class represents the field that can be sorted on for a host class search
 * specification.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/HostClassMgrSortFieldType.java#1 $
 */

public class HostClassMgrSortFieldType extends EnumBase {

    public static final HostClassMgrSortFieldType NAME = new HostClassMgrSortFieldType("Name");

    /**
     * Constructor
     * 
     * @param name
     *            name of the host class sort field
     */
    private HostClassMgrSortFieldType(String name) {
        super(name);
    }
}