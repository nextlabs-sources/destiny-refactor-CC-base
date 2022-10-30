package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IElementField.java#1 $
 */

import com.bluejungle.framework.expressions.IAttribute;

/**
 * Defines a contract for fields of <code>IElementType</code>
 * objects.

 * IElementField objects may be included in predicates as an
 * expression part of an IRelation.
 *
 * @author sergey
 */

public interface IElementField extends IAttribute {

    /**
     * Returns the name of this element field.
     * @return the name of this element field.
     */
    String getName();

    /**
     * Returns the label of this element field.
     * @return the label of this element field.
     */
    String getLabel();

    /**
     * Returns the type of this element field.
     * @return the type of this element field.
     */
    ElementFieldType getType();

    /**
     * Gets the value corresponding to this fields from the element.
     * @param element the element from which to get the data.
     * @return the data corresponding to the given field.
     * @throws IllegalArgumentException if the element's type
     * and the type to which this field belongs are different.
     */
    Object getValue( IElement element );

    /**
     * Sets the value corresponding to this fields into the element.
     * @param element the element to which to set the data.
     * @param value the value to set.
     * @throws IllegalArgumentException if the element's type
     * and the type to which this field belongs are different.
     * @throws ClassCastException if the value is not type-compatible
     * with the element type.
     */
    void setValue( IElement element, Object value );

}
