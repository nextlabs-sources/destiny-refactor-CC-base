/*
 * Created on Mar 27, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.common;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/common/EnrollmentValidationException.java#1 $
 */

public class EnrollmentValidationException extends EnrollmentException {

    /**
     * Constructor
     * @param message
     */
    public EnrollmentValidationException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param cause
     */
    public EnrollmentValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public EnrollmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
