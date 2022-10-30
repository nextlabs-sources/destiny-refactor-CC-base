/*
 * Created on Jan 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

/**
 * Base class for concrete profile query field enumerations
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/BaseProfileQueryFields.java#1 $
 */

abstract class BaseProfileQueryFields {

    private String fieldName;

    /**
     * Create a BaseProfileQueryFields instance
     * 
     * @param fieldName
     *            the name of the query field
     */
    protected BaseProfileQueryFields(String fieldName) {
        super();

        if (fieldName == null) {
            throw new IllegalArgumentException("fieldName cannot be null.");
        }

        this.fieldName = fieldName;
    }

    /**
     * Retrieve the field name associated with this query field
     * 
     * @return the field name associated with this query field
     */
    public String getFieldName() {
        return this.fieldName;
    }

    public boolean equals(Object queryFieldToCompare) {
        boolean valueToReturn = false;
        if (this == queryFieldToCompare) {
            valueToReturn = true;
        } else if ((queryFieldToCompare != null) && (queryFieldToCompare.getClass().equals(this.getClass()))) {
            valueToReturn = this.getFieldName().equals(((BaseProfileQueryFields) queryFieldToCompare).getFieldName());
        }
        return valueToReturn;
    }

    public int hashCode() {
        return fieldName.hashCode();
    }
}