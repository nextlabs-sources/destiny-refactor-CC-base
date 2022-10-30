/*
 * Created on May 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

/**
 * This exception is thrown whenever the execution of the DB init task fails.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/DBInitException.java#1 $
 */

public class DBInitException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -624263176755844144L;

	/**
     * Constructor
     */
    public DBInitException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param message
     */
    public DBInitException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param message
     * @param cause
     */
    public DBInitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public DBInitException(Throwable cause) {
        super(cause);
    }
}