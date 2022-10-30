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
 * This class represents the field that can be queried for a policy search
 * specification. A policy search specification can only be done on one of these
 * fields.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/PolicyMgrQueryFieldType.java#1 $
 */

public class PolicyMgrQueryFieldType extends EnumBase {

    public static final PolicyMgrQueryFieldType NAME = new PolicyMgrQueryFieldType("None") {
    };

    /**
     * Constructor
     * 
     * @param name
     *            name of the policy field to be queried.
     */
    private PolicyMgrQueryFieldType(String name) {
        super(name);
    }
}