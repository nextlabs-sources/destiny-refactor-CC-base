/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/expressions/DefaultExpressionVisitor.java#1 $
 */

package com.bluejungle.framework.expressions;

/**
 * Provides default (empty) implementation for the visiting methods.
 */
public class DefaultExpressionVisitor implements IExpressionVisitor {

    /**
     * @see com.bluejungle.framework.expressions.IExpressionVisitor#visit(com.bluejungle.framework.expressions.IAttribute)
     */
    public void visit( IAttribute attribute ) {
    }

    /**
     * @see com.bluejungle.framework.expressions.IExpressionVisitor#visit(com.bluejungle.framework.expressions.Constant)
     */
    public void visit( Constant constant ) {
    }

    /**
     * @see com.bluejungle.framework.expressions.IExpressionVisitor#visit(com.bluejungle.framework.expressions.FunctionExpression)
     */
    public void visit( IFunctionApplication func ) {
    }

    /**
     * @see com.bluejungle.framework.expressions.IExpressionVisitor#visit(com.bluejungle.framework.expressions.IExpressionReference)
     */
    public void visit( IExpressionReference ref ) {
    }

    /**
     * @see com.bluejungle.framework.expressions.IExpressionVisitor#visit(com.bluejungle.framework.expressions.IExpression)
     */
    public void visit( IExpression expression ) {
    }

}
