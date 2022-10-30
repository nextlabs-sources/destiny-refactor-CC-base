/*
 * Created on Nov 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filter;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filter/FileFormatException.java#1 $
 */

public class FileFormatException extends Exception {

    static final long serialVersionUID = 0;
    
    /**
     * Constructor
     * 
     */
    public FileFormatException() {
        super();
    }

    /**
     * Constructor
     * @param message
     */
    public FileFormatException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param cause
     */
    public FileFormatException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public FileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}
