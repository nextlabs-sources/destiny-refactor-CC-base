package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IExpression.java#1 $
 */

/**
 * This interface defines the contract for expression representations.
 *
 * @author Sergey Kalinichenko
 */

public interface IExpression {

    /** The constant that represents a null value. */
    public static final Constant NULL = new Constant(null, IDataType.NULL);

    /** The constant that represents the value of 'true'. */
    public static final Constant TRUE = new Constant(true, IDataType.BOOLEAN);

    /** The constant that represents the value of 'false'. */
    public static final Constant FALSE = new Constant(
        false
    ,   IDataType.BOOLEAN
    );

    /**
     * Implementations of this method call the appropriate method
     * of the visitor, letting the caller "examine" the structure
     * of this expression.
     *
     * @param visitor The visitor to which to report the structure
     * of the expression.
     */
    void accept(IExpressionVisitor visitor);

}
