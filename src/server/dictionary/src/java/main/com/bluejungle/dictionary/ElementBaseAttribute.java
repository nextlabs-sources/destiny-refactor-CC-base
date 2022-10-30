/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/ElementBaseAttribute.java#1 $
 */

package com.bluejungle.dictionary;

import com.bluejungle.framework.expressions.IArguments;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.IExpressionVisitor.Order;

/**
 * This class implements and provides instances of attributes
 * used for querying the dictionary based on field of the
 * <code>DictionaryElementBasr</code> class.
 */
abstract class ElementBaseAttribute implements IMappedElementField {

    private final String name;
    private final String label;

    /**
     * This attribute represents the id field.
     */
    public static final ElementBaseAttribute INTERNAK_KEY = new ElementBaseAttribute("originalId", "Internal Key") {
        private static final long serialVersionUID = 1L;
        /**
         * @see IElementField#getType()
         */
        public ElementFieldType getType() {
            return ElementFieldType.NUMBER;
        }
        /**
         * @see IElementField#getValue(IElement)
         */
        public Object getValue( IElement element ) {
            return element.getInternalKey();
        }
        /**
         * @see IElementField#setValue(IElement, Object)
         */
        public void setValue( IElement element, Object value ) {
            throw new UnsupportedOperationException("Setting the ID on dictionary objects is not supported.");
        }
    };

    /**
     * This attribute represents the display name field.
     */
    public static final ElementBaseAttribute DISPLAY_NAME = new ElementBaseAttribute("displayName", "Display Name") {
        private static final long serialVersionUID = 1L;
        /**
         * @see ElementBaseAttribute#getFieldType()
         */
        public ElementFieldType getType() {
            return ElementFieldType.STRING;
        }
        /**
         * @see IElementField#getValue(IElement)
         */
        public Object getValue( IElement element ) {
            return element.getDisplayName();
        }
        /**
         * @see IElementField#setValue(IElement, Object)
         */
        public void setValue( IElement element, Object value ) {
            ((IMElement)element).setDisplayName((String)value);
        }
    };

    /**
     * This attribute represents the unique name field.
     */
    public static final ElementBaseAttribute UNIQUE_NAME = new ElementBaseAttribute("uniqueName", "Unique Name") {
        private static final long serialVersionUID = 1L;
        /**
         * @see ElementBaseAttribute#getFieldType()
         */
        public ElementFieldType getType() {
            return ElementFieldType.CS_STRING;
        }
        /**
         * @see IElementField#getValue(IElement)
         */
        public Object getValue( IElement element ) {
            return element.getUniqueName();
        }
        /**
         * @see IElementField#setValue(IElement, Object)
         */
        public void setValue( IElement element, Object value ) {
            ((IMElement)element).setUniqueName((String)value);
        }
    };

    /**
     * This is the protected constructor for <code>ElementBaseAttribute</code>.
     * It simply initializes the name of the attribute.
     * @param name the name of this attribute.
     * @param label the display name of this attribute.
     */
    protected ElementBaseAttribute(String name, String label) {
        this.name = name;
        this.label = label;
    }

    /**
     * @see IElementField#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see IElementField#getLabel()
     */
    public String getLabel() {
        return label;
    }

    /**
     * @see IMappedElementField#getMapping()
     */
    public String getMapping() {
        return name;
    }

    /**
     * @see IExpression#acceptVisitor(IExpressionVisitor, IExpressionVisitor.Order)
     */
    public void acceptVisitor( IExpressionVisitor visitor, Order order ) {
        visitor.visit(this);
    }

    /**
     * @see IExpression#buildRelation(RelationOp, IExpression)
     */
    public IRelation buildRelation( RelationOp op, IExpression rhs ) {
        return new Relation(op, this, rhs);
    }

    /**
     * @see IAttribute#getObjectSubTypeName()
     */
    public String getObjectSubTypeName() {
        throw new UnsupportedOperationException("getObjectSubTypeName");
    }

    /**
     * @see IAttribute#getObjectTypeName()
     */
    public String getObjectTypeName() {
        throw new UnsupportedOperationException("getObjectTypeName");
    }

    /**
     * @see IExpression#evaluate(IArguments)
     */
    public IEvalValue evaluate( IArguments arg ) {
        throw new UnsupportedOperationException("evaluate");
    }

}
