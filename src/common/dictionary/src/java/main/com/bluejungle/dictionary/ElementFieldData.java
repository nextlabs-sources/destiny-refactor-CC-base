package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/ElementFieldData.java#1 $
 */

/**
 * Instances of this class represent fields of an object
 * returned from the 
 * {@link IDictionary#queryFields(IElementField[], IPredicate, Date, Order[], Page)}
 * method.
 */
public class ElementFieldData {

    /** This is the internal key of the element from which we got fields. */
    private final Long internalKey;

    /** This is the type of the element from which we got the fields. */
    private final IElementType type;

    /** This is the unique name of the element from which we got the fields.*/
    private final String uniqueName;

    /** This is the collection of field definitions. */
    private final IElementField[] fields;

    /** This field represents the field data. */
    private final Object[] data;

    /**
     * Makes an instance of ElementFieldData with the specified parameters. 
     * @param internalKey the internal key of the dictionary element.
     * @param type the type of the dictionary element.
     * @param fields the fields of the dictionary elements.
     * @param data the data for the fields of the dictionary element.
     */
    public ElementFieldData(
        Long internalKey
    ,   IElementType type
    ,   String uniqueName
    ,   IElementField[] fields
    ,   Object[] data) {
        this.internalKey = internalKey;
        this.type = type;
        this.uniqueName = uniqueName;
        this.fields = fields;
        this.data = data;
    }

    /**
     * @return the data
     */
    public Object[] getData() {
        return data;
    }

    /**
     * @return the type
     */
    public IElementType getType() {
        return type;
    }

    /**
     * @return the fields
     */
    public IElementField[] getFields() {
        return fields;
    }

    /**
     * @return the internalKey
     */
    public Long getInternalKey() {
        return internalKey;
    }

    /**
     * @return the uniqueName
     */
    public String getUniqueName() {
        return uniqueName;
    }

}
