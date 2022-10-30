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
 * This class represents the field that can be queried for a host search
 * specification. A host search specification can only be done on one of these
 * fields.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/HostMgrQueryFieldType.java#1 $
 */

public class HostMgrQueryFieldType extends EnumBase {

    public static final HostMgrQueryFieldType NONE = new HostMgrQueryFieldType("None");
    public static final HostMgrQueryFieldType NAME = new HostMgrQueryFieldType("Name");

    /**
     * Constructor
     * 
     * @param name
     *            name of the hostQueryFieldType
     */
    private HostMgrQueryFieldType(String name) {
        super(name);
    }
}