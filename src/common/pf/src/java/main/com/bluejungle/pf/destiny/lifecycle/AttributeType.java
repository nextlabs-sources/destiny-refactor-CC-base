package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $ Id: $
 */

import com.bluejungle.framework.patterns.EnumBase;

/**
 * An enumeration representing the type of an attribute.
 */
public class AttributeType extends EnumBase {

    /**
     * Creates an attribute type.
     * @param name The name of this attribute type.
     */
    private AttributeType( String name ) {
        super( name, AttributeType.class );
    }

    /** Represents attributes of type long. */
    public static final AttributeType LONG = new AttributeType("LONG") {
        private static final long serialVersionUID = 1L;
    };

    /** Represents attributes of type String. */
    public static final AttributeType STRING = new AttributeType("STRING") {
        private static final long serialVersionUID = 1L;
    };

    /** Represents attributes of type Date. */
    public static final AttributeType DATE = new AttributeType("DATE") {
        private static final long serialVersionUID = 1L;
    };

    /** Represents attributes of type Boolean. */
    public static final AttributeType BOOLEAN = new AttributeType("BOOLEAN") {
        private static final long serialVersionUID = 1L;
    };

    /** Represents attributes of type Enum. */
    public static final AttributeType ENUM = new AttributeType("ENUM") {
        private static final long serialVersionUID = 1L;
    };

    /**
     * Retrieves an element by its name.
     * @param name the name of the element to retrieve.
     * @return the element with the given name.
     */
    public static AttributeType forName( String name ) {
        return getElement( name, AttributeType.class );
    }

}
