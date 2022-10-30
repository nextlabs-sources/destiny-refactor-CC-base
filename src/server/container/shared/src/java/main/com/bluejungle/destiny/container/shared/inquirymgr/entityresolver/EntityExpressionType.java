/*
 * Created on Mar 29, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.entityresolver;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This is the user expression type class. It represents what a user expression
 * can be.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/entityresolver/EntityExpressionType.java#1 $
 */

public class EntityExpressionType extends EnumBase {

    public static final EntityExpressionType UNKNOWN = new EntityExpressionType("Unknown") {
    };
    public static final EntityExpressionType ENTITY = new EntityExpressionType("Entity") {
    };
    public static final EntityExpressionType ENTITY_AND_ENTITY_GROUP = new EntityExpressionType("Both") {
    };
    public static final EntityExpressionType ENTITY_GROUP = new EntityExpressionType("Group") {
    };

    /**
     * Constructor
     * 
     * @param type
     *            type to use
     */
    protected EntityExpressionType(String type) {
        super(type);
    }
}