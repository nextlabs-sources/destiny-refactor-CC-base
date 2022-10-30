package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IExpressionVisitor.java#1 $
 */

/**
 * This interface is part of the visitor pattern implementation
 * for predicates. It defines visiting operations for predicates
 * of built-in types, and a catch-all visit method for user-defined ones.
 *
 * @author Sergey Kalinichenko
 */

public interface IExpressionVisitor {

    /**
     * Visits a constant expression.
     *
     * @param constant the constant expression to visit.
     */
    void visitConstant(Constant constant);

    /**
     * Visits a composite expression.
     *
     * @param composite the composite expression being visited.
     */
    void visitComposite(ICompositeExpression composite);

    /**
     * Visits an attribute reference.
     *
     * @param attrReference the attribute reference being visited.
     */
    void visitAttributeReference(IAttributeReference attrReference);

    /**
     * Visits a reference to an expression.
     *
     * @param reference the expression reference being visited.
     */
    void visitReference(IExpressionReference reference);

    /**
     * Visits a function call.
     *
     * @param function the function call being visited.
     */
    void visitFunction(IFunctionCall function);

    /**
     * Visits a relation expression.
     *
     * @param relation the relation expression being visited.
     */
    void visitRelation(IRelation relation);

    /**
     * Visit a unary expression.
     *
     * @param unary the unary expression being visited.
     */
    void visitUnary(IUnaryExpression unary);

    /**
     * Visit any other subclass of IExpression.
     *
     * @param expr the IExpression being visited.
     */
    void visitExpression(IExpression expr);

}
