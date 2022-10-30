package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/ICompositeExpression.java#1 $
 */

/**
 * This interface defines the contract for composite expressions,
 * i.e. expressions that referece other expressions.
 *
 * The structure of composite expressions is as follows:
 * <first:IExpression> {<operator IExpression>}+.
 *
 * All operators in the expression must have the same precedence,
 * meaning that a composite expression could represent
 * "A + B - C + D" or "A * B % C / D", but not "A + B * C".
 *
 * @author Sergey Kalinichenko
 */
public interface ICompositeExpression
       extends IExpression, Iterable<ICompositeExpression.Element> {

    /**
     * This interface represents a point in a composite expression chain.
     * Each element has an expression; elements in the middle have both
     * an operator before and an operator after; initial elements have only
     * the operator before; trailing elements have only an operator after.
     */
    interface Element {

        /**
         * Gets the operator preceding the expression in this element.
         *
         * @return the operator preceding the expression in this element.
         */
        BinaryOperator getOperatorBefore();

        /**
         * Gets the operator following the expression in this element.
         *
         * @return the operator following the expression in this element.
         */
        BinaryOperator getOperatorAfter();

        /**
         * Gets the expression of this element.
         *
         * @return the expression of this element.
         */
        IExpression getExpression();

        /**
         * Determines if this is the initial element.
         *
         * @return true if this is the initial element; false otherwise.
         */
        boolean isFirst();

        /**
         * Determines if this is the element.
         *
         * @return true if this is an initial element; false otherwise.
         */
        boolean isLast();

    }

    /**
     * Returns the expression at the specified index. Valid indexes are
     * between 0 and size()-1, inclusive.
     * getExpression(0) and getInitialExpression() return the same value.
     * @param index the expression at the specified index.
     * @throws IndexOutOfBoundsException when the index is invalid.
     */
    IExpression getExpression(int index);

    /**
     * Returns the operator preceding the expression at the specified index.
     * This method returns null for index zero.
     *
     * @param index the operator preceding the expression
     * at the specified index.
     * @throws IndexOutOfBoundsException when the index is invalid.
     */
    BinaryOperator getOperatorBefore(int index);

    /**
     * Returns the operator following the expression at the specified index.
     * This method returns null for the index that equals the size().
     *
     * @param index the operator preceding the expression
     * at the specified index.
     * @throws IndexOutOfBoundsException when the index is invalid.
     */
    BinaryOperator getOperatorAfter(int index);

    /**
     * Returns the number of expressions in this composite expression.
     * @return the number of expressions in this composite expression.
     */
    int size();

}
