package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/ElementFieldType.java#1 $
 */

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This class defines constants representing data types of fields
 * of <code>IElement</code>/<code>IMElement</code> objects.
 */
public abstract class ElementFieldType extends EnumBase {

    /**
     * This field represents the base portion of the property name
     * for the specified field type.
     */
    private final String mappingBase;

    /**
     * Represents the string type.
     */
    static public final ElementFieldType STRING = new ElementFieldType("String", "String") {
        private static final long serialVersionUID = 1L;
    };

    /**
     * Represents case-sensitive string type.
     */
    static public final ElementFieldType CS_STRING = new ElementFieldType("CS-String", "String") {
        private static final long serialVersionUID = 1L;
    };

    /**
     * Represents case-insensitive string type capable of storing multiple strings.
     */
    static public final ElementFieldType STRING_ARRAY = new ElementFieldType("MULTI-String", "String") {
        private static final long serialVersionUID = 1L;
        /**
         * @see ElementFieldType#convertFromBase(Object)
         */
        @Override
        public Object convertToBase(Object base) {
            if (base == null) {
                return null;
            }
            if (!(base instanceof String[])) {
                throw new IllegalArgumentException("base");
            }
            return MultivalueHelper.joinElements((String[])base);
        }
        /**
         * @see ElementFieldType#convertToBase(Object)
         */
        @Override
        public Object convertFromBase(Object actual) {
            if (actual == null) {
                return null;
            }
            if (!(actual instanceof String)) {
                throw new IllegalArgumentException("actual");
            }
            return MultivalueHelper.splitElements((String)actual);
        }
    };

    /**
     * Represents case-insensitive string type capable of storing multiple Long values.
     */
    static public final ElementFieldType NUM_ARRAY = new ElementFieldType("MULTI-Long", "NumArray") {
        private static final long serialVersionUID = 1L;
    };

    /**
     * Represents the numeric type.
     */
    static public final ElementFieldType NUMBER = new ElementFieldType("Number", "Number") {
        private static final long serialVersionUID = 1L;
    };

    /**
     * Represents the DATE type.
     */
    static public final ElementFieldType DATE = new ElementFieldType("Date", "Date") {
        private static final long serialVersionUID = 1L;
    };
    
    /**
     * Represents a string with very large limit length
     */
    static public final ElementFieldType LONG_STRING = new ElementFieldType("LONG-String", "LongString") {
    	 private static final long serialVersionUID = 1L;
    };

    /**
     * Creates a new instance of this enumeration.
     * @param name
     */
    private ElementFieldType(String name, String mappingBase) {
        super( name.toLowerCase(), ElementFieldType.class );
        this.mappingBase = mappingBase;
    }

    /**
     * Obtain the base portion of the property name
     * for the specified field type.
     * @return the base portion of the property name
     * for the specified field type.
     */
    public String getMappingBase() {
        return mappingBase;
    }

    /**
     * This method converts a value of the actual type to the value
     * of its base type (i.e. the type stored in the Java element).
     * The default implementation returns the same object.
     * 
     * @param actual the object to be converted.
     * @return the result of the conversion.
     */
    public Object convertToBase(Object actual) {
        return actual;
    }

    /**
     * This method converts a value of the base type to the value
     * of its actual type (i.e. the type returned to the users).
     * The default implementation returns the same object.
     * 
     * @param base the object to be converted.
     * @return the result of the conversion.
     */
    public Object convertFromBase(Object base) {
        return base;
    }

    /**
     * Retrieves the enum type by name
     * @param name
     * @return enum type
     */
    public static ElementFieldType getByName(String name) {
        return EnumBase.getElement(name.toLowerCase(), ElementFieldType.class);
    }

}
