package com.nextlabs.expression.representation;

import com.nextlabs.util.ref.IReference;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IFunctionCall.java#1 $
 */

/**
 * This interface defines the contract for function call expressions.
 *
 * @author Sergey Kalinichenko
 */
public interface IFunctionCall extends IExpression, Iterable<IExpression> {

    /**
     * This interface defines the contract for passing function arguments.
     * There are two ways of passing arguments to functions - by name, or
     * by position. The names of positional arguments are null.
     * All arguments of a function call are either positional or by name;
     * mixing types is not allowed.
     */
    interface Argument {

        /**
         * Returns the name of formal parameter for which
         * this argument is passed.
         *
         * @return the name of formal parameter for which this argument
         * was passed. If the call passes arguments by position, this method
         * will return null.
         */
        String getName();

        /**
         * Returns the expression representing the actual parameter.
         *
         * @return the expression representing the actual parameter.
         */
        IExpression getExpression();

    }

    /**
     * Returns the reference to the function (either by name or by id).
     *
     * @return the reference to the function (either by name or by id).
     */
    IReference<IFunction> getFunction();

    /**
     * Determines if this function call uses named or positional arguments.
     *
     * @return true if this call uses named arguments; false otherwise.
     */
    boolean hasNamedArguments();

    /**
     * Returns an iterable with arguments of this function call.
     *
     * @return an iterable with arguments of this function call.
     */
    Iterable<Argument> getArguments();

    /**
     * Gets the number of actual arguments passed to this function call.
     *
     * @return the number of actual arguments passed to this function call.
     */
    int getArgumentCount();

    /**
     * Gets the argument at the specified position.
     * @param index the index of the argument to get.
     * @return the argument at the specified position.
     * @throws IndexOutOfBoundsException when the index is less than zero or
     * is greater than or equal to the number of actual arguments passed to
     * this function call.
     */
    Argument getArgument(int index);

    /**
     * Gets the name of the argument at the specified position.
     * @param index the index of the argument to get.
     * @return the name of the argument at the specified position.
     * @throws IndexOutOfBoundsException when the index is less than zero or
     * is greater than or equal to the number of actual arguments passed to
     * this function call.
     */
    String getArgumentName(int index);

    /**
     * Gets the argument expression at the specified position.
     * @param index the index of the argument to get.
     * @return the argument expression at the specified position.
     * @throws IndexOutOfBoundsException when the index is less than zero or
     * is greater than or equal to the number of actual arguments passed to
     * this function call.
     */
    IExpression getExpression(int index);

    /**
     * Gets the argument expression for the specified name.
     * @param name the name of the argument to get.
     * @return the argument expression for the specified name, or null if
     * the specified argument name does not have an associated expression.
     */
    IExpression getExpression(String argumentName);

}
