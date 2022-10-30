package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IMElementType.java#1 $
 */

/**
 * This interface defines contract for mutable element types.
 *
 * @author sergey
 */

public interface IMElementType extends IElementType {

    /**
     * Adds a new field to this type.
     * @param name the name of the field to be added.
     * @param type the type of the field to be added.
     * @return the newly added field.
     * @throws IllegalArgumentException if the type already has
     * the specified field.
     */
    IElementField addField( String name, ElementFieldType type );

    /**
     * Remove the specified field from this type.
     * @param field the field to be removed.
     * @throws IllegalArgumentException if the field does not belong
     * to this type.
     */
    void deleteField( IElementField field );

    /**
     * Attempts to change the name of the field.
     * @param field the field to be renamed.
     * @param name the new name of the field.
     * @throws IllegalArgumentException if the name conflicts with
     * names of existing fields of this type, or if the field does not
     * belong to this type.
     */
    void renameField( IElementField field, String name );

    /**
     * Changes the label of the specified field.
     * @param field the field the label of which needs to be changed.
     * @param label the new label of the field.
     * @throws IllegalArgumentException if the field does not belong
     * to this type.
     */
    void setFieldLabel( IElementField field, String label );

}
