/*
 * Created on Apr 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

/**
 * This is the condition element class implementation
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/ConditionElementImpl.java#1 $
 */

public class ConditionElementImpl implements IConditionElement {

    private String expression;

    /**
     * Constructor
     * 
     * @param expr
     *            expression for the condition
     */
    public ConditionElementImpl(String expr) {
        super();
        if (expr == null || expr.length() == 0) {
            throw new IllegalArgumentException("Invalid expression argument");
        }
        this.expression = expr;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IConditionElement#getExpression()
     */
    public String getExpression() {
        return this.expression;
    }

}