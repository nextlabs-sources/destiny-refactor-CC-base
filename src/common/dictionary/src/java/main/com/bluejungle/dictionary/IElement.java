package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IElement.java#1 $
 */

/**
 * This interface defines the contract for accessing data stored in
 * dictionary elements.
 * 
 * @author sergey
 */

public interface IElement extends IElementBase {
    /**
     * Returns the value of the given field of the element.
     * @param field the field the value for which to return.
     * @return the value of the given field of the element.
     * @throws IllegalArgumentException if the field
     * is not present in the type of this element.
     */
    Object getValue( IElementField field );

    /**
     * Returns the value of the given field of the element
     * specified by an external name. The external name is looked up
     * using the enrollment of this element.
     * @param externalName the external name of the field.
     * @return the value of the given field of the element.
     * @throws IllegalArgumentException if the external name
     * is not present in the enrollment.
     */
    Object getValue( String externalName );

}
