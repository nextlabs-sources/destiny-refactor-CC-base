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
 * This class represents the field that can be queried for a resource class
 * search specification. A resource class search specification can only be done
 * on one of these fields.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/resourceclassmgr/ResourceClassMgrQueryFieldType.java#1 $
 */

public class ResourceClassMgrQueryFieldType extends EnumBase {

    public static final ResourceClassMgrQueryFieldType NAME = new ResourceClassMgrQueryFieldType("Name") {
    };

    /**
     * Constructor
     * 
     * @param name
     *            name of the resource class field to query on
     */
    private ResourceClassMgrQueryFieldType(String name) {
        super(name);
    }
}