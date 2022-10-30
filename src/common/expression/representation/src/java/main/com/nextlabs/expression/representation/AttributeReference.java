package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/AttributeReference.java#1 $
 */

import com.nextlabs.util.Strings;

/**
 * This class implements an attribute reference.
 *
 * @author Sergey Kalinichenko
 */
public class AttributeReference implements IAttributeReference {

    /**
     * The base of this attribute reference.
     */
    private final IExpression base;

    /**
     * The name of the referenced attribute.
     */
    private final String attributeName;

    /**
     * Creates an attribute reference with the specified base and name.
     *
     * @param base the base of the attribute reference.
     * @param attrName the name of the referenced attribute.
     */
    public AttributeReference(IExpression base, String attrName) {
        if (base == null) {
            throw new NullPointerException("base");
        }
        if (attrName==null) {
            throw new NullPointerException("attributeName");
        }
        if (Strings.isEmpty(attrName) || !Strings.isTrimmed(attrName)) {
            throw new IllegalArgumentException("attributeName");
        }
        this.base = base;
        this.attributeName = attrName;
    }

    /**
     * @see IAttributeReference#getAttributeName()
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @see IAttributeReference#getBase()
     */
    public IExpression getBase() {
        return base;
    }

    /**
     * @see IExpression#accept(IExpressionVisitor)
     */
    public void accept(IExpressionVisitor visitor) {
        visitor.visitAttributeReference(this);
    }

    /**
     * Gets a String representation of this attribute reference.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return base.toString()+"."+attributeName;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AttributeReference)) {
            return false;
        }
        AttributeReference other = (AttributeReference)obj;
        return base.equals(other.getBase())
            && attributeName.equals(other.getAttributeName());
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return base.hashCode() ^ attributeName.hashCode();
    }

}
