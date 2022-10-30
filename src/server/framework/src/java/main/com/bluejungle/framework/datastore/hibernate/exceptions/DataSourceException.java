/*
 * Created on Jan 5, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.exceptions;


/**
 * This Exception represents errors with the persistance layer in Destiny. This
 * includes errors with the initialization, query, insert, update and delete
 * operations dealing with the persistence layer.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class DataSourceException extends Exception {

    /**
     * Constructor
     *  
     */
    public DataSourceException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public DataSourceException(Throwable cause) {
        super(cause);
    }
}