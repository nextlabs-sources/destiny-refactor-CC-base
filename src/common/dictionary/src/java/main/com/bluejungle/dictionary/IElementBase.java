package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IElementBase.java#1 $
 */

/**
 * This is the base interface for the <code>IGroup</code> and
 * <code>IElement</code> interfaces. It defines the operations for
 * getting external and internal keys from elements.
 */
public interface IElementBase extends IReferenceable {

    /**
     * This method lets dictionary users access the internal key
     * of this dictionary element. Calling this method on an unsaved
     * dictionary element is not allowed.
     * @return the internal key of this dictionary element.
     * @throws IllegalStateException if the method is called
     * on an unsaved dictionary element.
     */
    Long getInternalKey();

    /**
     * This method lets dictionary users access the external key
     * of this dictionary element.
     * @return the external key of this dictionary element.
     */
    DictionaryKey getExternalKey();

    /**
     * This method lets dictionary users obtain a display name
     * of the given item. The display name may be empty or null.
     * @return a display name of the given item.
     */
    String getDisplayName();

    /**
     * This method lets dictionary users obtain a unique name
     * of the given item. The unique name may be empty or null.
     * The dictionary does not enforce the uniqueness
     * of the unique name, and does not perform searches based on it.
     * @return a unique name of the given item.
     */
    String getUniqueName();

    /**
     * Returns the enrollment from which this element was created.
     * @return the enrollment from which this element was created.
     */
    IEnrollment getEnrollment();
    
    /**
     * Returns the type of this element.
     * @return the type of this element.
     */
    IElementType getType();
}
