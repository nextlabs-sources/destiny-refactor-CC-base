/*
 * Created on Feb 3, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.common;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/common/EnrollmentCheckException.java#1 $
 */

public class EnrollmentCheckException extends Exception {
    public EnrollmentCheckException(String message) {
        super(message);
    }

    public EnrollmentCheckException(Throwable cause) {
        super(cause);
    }
}
