/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.policymgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This class represents the field that can be sorted on for a policy sort
 * specification.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/PolicyMgrSortFieldType.java#1 $
 */

public class PolicyMgrSortFieldType extends EnumBase {

    public static final PolicyMgrSortFieldType NAME = new PolicyMgrSortFieldType("Name") {
    };

    /**
     * Constructor
     * 
     * @param name
     *            name of the policy sort field name
     */
    private PolicyMgrSortFieldType(String name) {
        super(name);
    }
}