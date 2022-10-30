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
 * This class represents the field that can be queried for a host class search
 * specification. A host class search specification can only be done on one of
 * these fields.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/HostClassMgrQueryFieldType.java#1 $
 */

public class HostClassMgrQueryFieldType extends EnumBase {

    public static final HostClassMgrQueryFieldType NAME = new HostClassMgrQueryFieldType("Name");

    /**
     * Constructor
     * 
     * @param name
     *            name of the host class field
     */
    private HostClassMgrQueryFieldType(String name) {
        super(name);
    }
}