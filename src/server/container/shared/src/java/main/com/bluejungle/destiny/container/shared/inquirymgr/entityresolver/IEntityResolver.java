/*
 * Created on Mar 29, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.entityresolver;

/**
 * This is the entity resolver interface. The entity resolver figures out if an
 * expression entered by the end user represents a single entity, a group, or
 * none of these at all.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/entityresolver/IEntityResolver.java#1 $
 */

public interface IEntityResolver {

    /**
     * Creates a properly formatted expression
     * 
     * @param type
     *            type of the entity
     * @param value
     *            value of the expression
     * @return a properly formatted expression of the give entity type
     */
    public String create(EntityExpressionType type, String value);

    /**
     * Extracts the value of the entity from a properly formatted expression
     * 
     * @param type
     *            entity expression type. If it is unknown, the value won't be
     *            extracted and null is returned.
     * @param expression
     *            non ambiguous expression
     * @return the value of the entity, if it is possible to figure it out, null
     *         otherwise. null can also be returned if the type is unknown
     */
    public String extractValue(EntityExpressionType type, String expression);

    /**
     * Returns whether a given expression represents an entity, an entity group,
     * or none of these.
     * 
     * @param expression
     *            expression to evaluate
     * @return the entity expression type associated with this expression.
     */
    public EntityExpressionType resolve(String expression);
}