/*
 * Created on Jan 18, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/SharePointEnrollmentException.java#1 $
 */

public class SharePointEnrollmentException extends Exception {

    public static final long serialVersionUID = 1L;
    
    /**
     * Constructor
     */
    public SharePointEnrollmentException() {
    }

    /**
     * Constructor
     * @param message
     */
    public SharePointEnrollmentException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param cause
     */
    public SharePointEnrollmentException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public SharePointEnrollmentException(String message, Throwable cause) {
        super(message, cause);
    }

}
