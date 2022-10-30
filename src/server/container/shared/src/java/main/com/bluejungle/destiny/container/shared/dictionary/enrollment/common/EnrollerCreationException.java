/*
 * Created on Mar 28, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.common;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/common/EnrollerCreationException.java#1 $
 */

public class EnrollerCreationException extends EnrollmentException {

    /**
     * Constructor
     * @param message
     */
    public EnrollerCreationException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public EnrollerCreationException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
