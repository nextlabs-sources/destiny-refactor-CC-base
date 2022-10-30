package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IAttributeReference.java#1 $
 */

/**
 * This interface defines the contract for attribute references.
 * This corresponds to "dot expressions" in the language, where the left-hand
 * side of the expression (the "base") is followed by a dot '.' and the name
 * of the attribute to obtain from the expression (e.g. user.firstName).
 *
 * @author Sergey Kalinichenko
 */
public interface IAttributeReference extends IExpression {

    /**
     * Gets the base of the expression (i.e. the part to the left of the dot).
     *
     * @return the base of the reference.
     */
    IExpression getBase();

    /**
     * Gets the name of the referenced attribute.
     *
     * @return the name of the referenced attribute.
     */
    String getAttributeName();

}
