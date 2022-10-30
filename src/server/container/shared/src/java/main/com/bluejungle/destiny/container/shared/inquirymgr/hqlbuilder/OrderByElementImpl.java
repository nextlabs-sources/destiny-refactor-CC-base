/*
 * Created on Apr 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

/**
 * This is the "order by" element implementation.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/OrderByElementImpl.java#1 $
 */

public class OrderByElementImpl implements IOrderByElement {

    private boolean ascending;
    private String expression;

    /**
     * Constructor
     * 
     * @param ascending
     *            ascending flag
     * @param expr
     *            order by expression
     */
    public OrderByElementImpl(boolean ascending, String expr) {
        super();
        this.ascending = ascending;
        this.expression = expr;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IOrderByElement#getExpression()
     */
    public String getExpression() {
        return this.expression;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IOrderByElement#isAcending()
     */
    public boolean isAcending() {
        return this.ascending;
    }
}