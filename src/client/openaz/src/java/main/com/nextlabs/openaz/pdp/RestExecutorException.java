/*
 * Created on Aug 11, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author sduan
 */
package com.nextlabs.openaz.pdp;

class RestExecutorException extends RuntimeException {

	private static final long serialVersionUID = -7355068060716532789L;

	public RestExecutorException() {
		super();
	}

	public RestExecutorException(String message, Throwable cause) {
		super(message, cause);
	}

	public RestExecutorException(String message) {
		super(message);
	}

	public RestExecutorException(Throwable cause) {
		super(cause);
	}
	
}
