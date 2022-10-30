/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This class represents the field that can be sorted on for a resource class
 * search specification.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/resourceclassmgr/ResourceClassSortFieldType.java#1 $
 */

public class ResourceClassMgrSortFieldType extends EnumBase {

    public static final ResourceClassMgrSortFieldType NAME = new ResourceClassMgrSortFieldType("FirstName") {
    };

    /**
     * Constructor
     * 
     * @param name
     *            name of the resource class field to sort on
     */
    private ResourceClassMgrSortFieldType(String name) {
        super(name);
    }
}