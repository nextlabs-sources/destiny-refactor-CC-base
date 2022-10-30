package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/ICallableType.java#1 $
 */

import java.util.Iterator;

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;

/**
 * This interface defines the common functionality for definitions of functions
 * and obligations.
 *
 * @author Sergey Kalinichenko
 */

public interface ICallableType<T extends ICallableType<T>>
       extends Iterable<ICallableType.IArgument>, IDefinition<T> {

    /**
     * Callable types define arguments. This interface defines the contract
     * for arguments of a callable type.
     */
    public interface IArgument {

        /**
         * Returns the name of this argument.
         *
         * @return a String representing the name of the argument.
         */
        String getName();

        /**
         * Returns the type of this argument.
         *
         * @return the type of this argument.
         */
        IDataType getType();

        /**
         * Determines if this argument is required or not.
         *
         * @return true if the argument is required; false otherwise.
         */
        boolean isRequired();

        /**
         * Obtains the default expression attached to this argument.
         *
         * @return the default expression attached to this argument;
         * null if no default expression is attached.
         */
        public IExpression getDefault();

        /**
         * Determines if the argument has a default expression.
         *
         * @return true if this argument has a default expression;
         * false otherwise.
         */
        public boolean hasDefault();

    }

    /**
     * @see Iterable#iterator()
     */
    Iterator<IArgument> iterator();

    /**
     * Obtains the number of arguments of callable type.
     *
     * @return the number of arguments of callable type.
     */
    int getArgumentCount();

}
