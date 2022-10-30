package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/DefaultExpressionVisitor.java#1 $
 */

/**
 * This is an empty implementation of the IExpressionVisitor interface.
 * Classes that process a subset of expression subtypes
 * use this class as their base.
 *
 * @author Sergey Kalinichenko
 */
public class DefaultExpressionVisitor implements IExpressionVisitor {

    /**
     * @see IExpressionVisitor#visitConstant(Constant)
     */
    public void visitConstant(Constant constant) {
        // Default implementation does nothing.
    }

    /**
     * @see IExpressionVisitor#visitComposite(ICompositeExpression)
     */
    public void visitComposite(ICompositeExpression composite) {
        // Default implementation does nothing.
    }

    /**
     * @see IExpressionVisitor#visitAttributeReference(IAttributeReference)
     */
    public void visitAttributeReference(IAttributeReference attrReference) {
        // Default implementation does nothing.
    }

    /**
     * @see IExpressionVisitor#visitReference(IExpressionReference)
     */
    public void visitReference(IExpressionReference reference) {
        // Default implementation does nothing.
    }

    /**
     * @see IExpressionVisitor#visitFunction(IFunctionCall)
     */
    public void visitFunction(IFunctionCall function) {
        // Default implementation does nothing.
    }

    /**
     * @see IExpressionVisitor#visitExpression(IExpression)
     */
    public void visitExpression(IExpression expr) {
        // Default implementation does nothing.
    }

    /**
     * @see IExpressionVisitor#visitRelation(IRelation)
     */
    public void visitRelation(IRelation relation) {
        // Default implementation does nothing.
    }

    /**
     * @see IExpressionVisitor#visitUnary(IUnaryExpression)
     */
    public void visitUnary(IUnaryExpression unary) {
        // Default implementation does nothing.
    }

}
