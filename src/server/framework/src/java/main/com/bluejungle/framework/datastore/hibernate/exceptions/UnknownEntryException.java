/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.exceptions;

import java.util.Map;

/**
 * UknownEntryException is thrown when an attempt is made to retrieve a
 * persisted entity through a unique field value which is not currently assigned
 * to any persisted entities of the type being requested
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/exceptions/UnknownEntryException.java#1 $
 */

public class UnknownEntryException extends DataSourceException {

    private String objectName;
    private Map fieldValues;

    /**
     * Create an instance of UnknownEntryException
     *  
     */
    public UnknownEntryException(String objectName, Map fieldValues) {
        super();

        if (objectName == null) {
            throw new NullPointerException("objectName cannot be null.");
        }

        if (fieldValues == null) {
            throw new NullPointerException("fieldValues cannot be null.");
        }

        this.objectName = objectName;
        this.fieldValues = fieldValues;
    }

    /**
     * Retrieve the fieldValues.
     * 
     * @return the fieldValues.
     */
    public Map getFieldValues() {
        return this.fieldValues;
    }

    /**
     * Retrieve the objectName.
     * 
     * @return the objectName.
     */
    public String getObjectName() {
        return this.objectName;
    }
}