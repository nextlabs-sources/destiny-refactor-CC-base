package com.nextlabs.expression.representation;

import java.util.HashMap;
import java.util.Map;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/BinaryOperator.java#1 $
 */

/**
 * This is the enumeration of binary arithmetic and logic operators.
 *
 * @author Sergey Kalinichenko
 */
public enum BinaryOperator {

    /**
     * Represents logical OR.
     */
    OR("OR", 0)

    /**
     * Represents addition.
     */
,   AND("AND", 1)
    /**
     * Represents addition.
     */
,   ADD("+", 2)
    /**
     * Represents subtraction.
     */
,   SUBTRACT("-", 2)
    /**
     * Represents multiplication.
     */
,   MULTIPLY("*", 3)
    /**
     * Represents division.
     */
,   DIVIDE("/", 3)
    /**
     * Represents the operation of taking an integer remainder.
     */
,   REMAINDER("%", 3);

    /**
     * The precedence of this operator.
     */
    private final int precedence;

    /** This is the common string representation of this operator. */
    private final String representation;

    /**
     * This is the mapping between operator's representation and the operator.
     */
    private static Map<String,BinaryOperator> forRepresentation;

    /**
     * Creates an operator with the given representation and precedence.
     *
     * @param representation the representation of this operator.
     * @param precedence the precedence of this operator.
     */
    private BinaryOperator(String representation, int precedence) {
        this.representation = representation;
        this.precedence = precedence;
        register(this);
    }

    /**
     * Gets the precedence of this operator. This is used for compatibility
     * checks in construction of composite expressions.
     *
     * @return the precedence of this operator.
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Returns the representation of this operator.
     * @return the representation of this operator.
     */
    public String getRepresentation() {
        return representation;
    }

    /**
     * Gets a String representation of this operation, which corresponds
     * to its representation string.
     *
     * @see Enum#toString()
     */
    @Override
    public String toString() {
        return representation;
    }

    /**
     * Given a representation of an operator, returns the operator
     * if it exists.
     *
     * @param representation the representation the operator for which
     * needs to be retrieved.
     * @return the operator with the specified representation.
     */
    public synchronized static BinaryOperator forRepresentation(
        String representation) {
        if (representation == null) {
            throw new NullPointerException("representation");
        }
        if (!forRepresentation.containsKey(representation)) {
            throw new IllegalArgumentException(representation);
        }
        return forRepresentation.get(representation);
    }

    /**
     * Registers the specified operator's representation.
     * @param op the operator that needs to be registered.
     */
    synchronized static void register(BinaryOperator op) {
        // Initializers run after individual constructors,
        // so we must check for null here:
        if (forRepresentation == null) {
            forRepresentation = new HashMap<String,BinaryOperator>();
        }
        if (forRepresentation.containsKey(op.representation)) {
            throw new IllegalArgumentException(op.representation);
        }
        forRepresentation.put(op.representation, op);
    }

    /**
     * Obtains the size of this enumeration.
     *
     * @return the size of this enumeration.
     */
    public static int size() {
        return values().length;
    }

}
