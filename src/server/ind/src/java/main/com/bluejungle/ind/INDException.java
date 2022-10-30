/*
 * Created on Feb 21, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/INDException.java#1 $
 */

public class INDException extends Exception {

    public static final long serialVersionUID = 1; 
    
    public INDException() {
    }

    public INDException(String message) {
        super(message);
    }

    public INDException(Throwable cause) {
        super(cause);
    }

    public INDException(String message, Throwable cause) {
        super(message, cause);
    }

}
