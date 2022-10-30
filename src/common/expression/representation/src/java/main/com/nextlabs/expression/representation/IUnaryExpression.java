package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IUnaryExpression.java#1 $
 */

/**
 * This interface defines the contract for unary expressions.
 *
 * @author Sergey Kalinichenko
 */
public interface IUnaryExpression extends IExpression {

    /**
     * Gets the operator of this unary expression.
     * @return the operator of this unary expression.
     */
    UnaryOperator getOperator();

    /**
     * Gets the operand of this unary expression.
     * @return the operand of this unary expression.
     */
    IExpression getOperand();

}
