/*
 * Created on Mar 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.common;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/common/EnrollmentException.java#1 $
 */

public abstract class EnrollmentException extends Exception {

	public EnrollmentException() {
		super();
	}

	public EnrollmentException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnrollmentException(String message) {
		super(message);
	}

	public EnrollmentException(Throwable cause) {
		super(cause);
	}
}
