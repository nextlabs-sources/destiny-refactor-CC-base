/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/ExternalFieldMapping.java#1 $
 */

package com.bluejungle.dictionary;

/**
 * Instanes of this class represent external names
 * defined in enrollments for fields of entity types.
 */
class ExternalFieldMapping {

    /** ID used in Hibernate. */
    Long id;

    /** Version used in Hibernate for optimistic locking. */
    int version;

    /** The field for which this mapping is defined. */
    private ElementField field;

    /**
     * This field is assessible through field.getParentType().
     * It is added here to make it possible to create a DB constraint
     * to enforce the uniqueness of external names within a type.
     */
    ElementType fieldType;

    /** The enrollment for which this mapping is defined. */
    private Enrollment enrollment;

    /** The external name defined by this mapping. */
    private String externalName;

    /**
     * Package-private default constructor for Hibernate.
     *
     */
    ExternalFieldMapping() {
    }

    /**
     * Package-private constructor for Enrollment.
     * @param enrollment the enrollment that owns this mapping.
     * @param field the field for which this mapping is defined.
     * @param externalName the new external name for the field.
     */
    ExternalFieldMapping( Enrollment enrollment, ElementField field, String externalName ) {
        if ( enrollment == null ) {
            throw new NullPointerException("enrollment");
        }
        if ( field == null ) {
            throw new NullPointerException("field");
        }
        this.enrollment = enrollment;
        this.field = field;
        fieldType = field.getParentType();
        this.externalName = externalName;
    }

    /**
     * Returns the external name defined by this mapping.
     * @return the external name defined by this mapping.
     */
    public String getExternalName() {
        return externalName;
    }

    /**
     * Sets the external name to the required value.
     * @param externalName the new value for externalName.
     */
    public void setExternalName( String externalName ) {
        this.externalName = externalName;
    }

    /**
     * Returns the field for which this mapping is defined.
     * @return the field for which this mapping is defined.
     */
    public IElementField getField() {
        return field;
    }

    /**
     * Returns the enrollment in which this mapping is defined.
     * @return the enrollment in which this mapping is defined.
     */
    public IEnrollment getEnrollment() {
        return enrollment;
    }

}
