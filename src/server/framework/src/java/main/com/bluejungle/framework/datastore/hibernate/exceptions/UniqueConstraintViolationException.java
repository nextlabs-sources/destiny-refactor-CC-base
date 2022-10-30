/*
 * Created on Apr 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.exceptions;

/**
 * UniqueConstraintViolationException may be thrown when an insert or update
 * will or did cause a datbase unique constraint violation. APIs may thrown this
 * exception before or after the insert/update is attempted
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/exceptions/UniqueConstraintViolationException.java#1 $
 */

public class UniqueConstraintViolationException extends DataSourceException {
    private String[] constrainingFields;
    
    /**
     * Create an instance of UniqueConstraintViolationException
     *  
     */
    public UniqueConstraintViolationException(String[] constrainingFields) {
        super();
        this.constrainingFields = constrainingFields;
    }

    /**
     * Create an instance of UniqueConstraintViolationException
     * 
     * @param cause
     */
    public UniqueConstraintViolationException(String[] constrainingFields, Throwable cause) {
        super(cause);
        this.constrainingFields = constrainingFields;
    }
    
    /**
     * Retrieve the array of fields on which the violated constraint is defined
     * @return the array of fields on which the violated constraint is defined
     */
    public String[] getConstrainingFields() {
        return this.constrainingFields;
    }
}