package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/UnaryExpression.java#1 $
 */

/**
 *
 * @author Alan Morgan
 */

public class UnaryExpression implements IUnaryExpression {
    
    /**
     * Stores the operator and operand (expression) for this unary
     * expression.  These fields are set in the constructor and do
     * not change after that.
     */
    private final UnaryOperator operator;
    private final IExpression operand;

    /**
     * Creates a unary expression from an operator and an operand
     */
    public UnaryExpression(UnaryOperator operator, IExpression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * @see IUnaryExpression#getOperator()
     */
    public UnaryOperator getOperator() {
        return operator;
    }

    /**
     * @see IUnaryExpression#getOperand()
     */
    public IExpression getOperand() {
        return operand;
    }

    /**
     * @see IExpression#accept(IExpressionVisitor)
     */
    public void accept(IExpressionVisitor visitor) {
        visitor.visitUnary(this);
    }

}
