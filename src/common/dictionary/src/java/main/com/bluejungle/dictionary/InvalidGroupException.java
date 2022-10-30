/*
 * All sources, binaries and HTML pages (C) Copyright 2008 by NextLabs Inc,
 * San Mateo, CA. Ownership remains with NextLabs Inc.
 * All rights reserved worldwide.
 *
 * @author Naomaru itoi
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/InvalidGroupException.java#1 $
 */
package com.bluejungle.dictionary;

public class InvalidGroupException extends DictionaryException {

	/**
	 * Default Constructor
	 */
	public InvalidGroupException() {
		super();
	}

    /**
     * Constructor with message
     * @param message Message for this <code>InvalidGroupException</code>
     */
	public InvalidGroupException(String message) {
		super(message);
	}

    /**
     * Constructor with cause
     * @param message Cause for this <code>InvalidGroupException</code>
     */
	public InvalidGroupException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with message and cause
     * @param message Message for this <code>InvalidGroupException</code>
	 * @param cause Cause for this <code>InvalidGroupException</code>
	 */
	public InvalidGroupException(String message, Throwable cause) {
		super(message, cause);
	}

}
