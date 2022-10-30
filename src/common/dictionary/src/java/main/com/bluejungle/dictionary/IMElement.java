package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IMElement.java#1 $
 */

/**
 * This interface defines the contract for accessing and modifying
 * data stored in dictionary elements.
 * @author sergey
 */

public interface IMElement extends IElement, IMElementBase {

    /**
     * Sets the value of the corresponding field to the value specified.
     * @param field the field the value of which you are changing.
     * @param value the new value of the field.
     * @throws IllegalArgumentException if the specified field
     * is not defined in the element's type, or the type of the field
     * does not match the type of the supplied object.
     * @return true if the value is changed
     */
    boolean setValue( IElementField field, Object value );

    /**
     * Sets he value of the given field of the element
     * specified by an external name. The external name is looked up
     * using the enrollment of this element.
     * @param externalName the external name of the field.
     * @param value the new value of the field.
     * @throws IllegalArgumentException if the external name
     * is not present in the enrollment.
     * @return true if the value is changed
     */
    boolean setValue( String externalName, Object value );

    /**
     * Returns the enrollment from which this element was created.
     * @return the enrollment from which this element was created.
     */
    IEnrollment getEnrollment();

}
