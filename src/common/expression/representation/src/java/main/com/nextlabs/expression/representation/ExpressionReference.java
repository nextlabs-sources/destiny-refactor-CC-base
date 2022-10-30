package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/ExpressionReference.java#1 $
 */

import com.nextlabs.util.ref.IReference;

/**
 * This class is an implementation of expression references.
 *
 * @author Sergey Kalinichenko
 */
public class ExpressionReference implements IExpressionReference {

    private final IReference<IExpression> reference;

    /**
     * Constructs new expression reference with the specified reference.
     *
     * @param reference the referenced expression reference.
     */
    public ExpressionReference(IReference<IExpression> reference) {
        if(reference == null) {
            throw new NullPointerException("reference");
        }
        this.reference = reference;
    }

    /**
     * @see IExpression#accept(IExpressionVisitor)
     */
    public void accept(IExpressionVisitor visitor) {
        visitor.visitReference(this);
    }

    /**
     * @see IExpressionReference#getReference()
     */
    public IReference<IExpression> getReference() {
        return reference;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IExpressionReference) {
            IExpressionReference other = (IExpressionReference)obj;
            return getReference().equals(other.getReference());
        } else {
            return false;
        }
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return reference.toString();
    }

}
