/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/DictionaryException.java#1 $
 */

package com.bluejungle.dictionary;

/**
 * This is the base exception for the Dictionary component.
 */
public class DictionaryException extends /*Runtime*/Exception {

    private static final long serialVersionUID = 1L;

    /**
     * The default constructor.
     */
    public DictionaryException() {
        super();
    }

    /**
     * Construct <code>DictionaryException</code> with the given message.
     * @param message the message for this <code>DictionaryException</code>.
     */
    public DictionaryException( String message ) {
        super( message );
    }

    /**
     * Construct <code>DictionaryException</code> with the given cause.
     * @param cause The cause of this <code>DictionaryException</code>.
     */
    public DictionaryException( Throwable cause ) {
        super( cause );
    }

    /**
     * Construct <code>DictionaryException</code> with the given message
     * and cause.
     * @param message the message for this <code>DictionaryException</code>.
     * @param cause The cause of this <code>DictionaryException</code>.
     */
    public DictionaryException( String message, Throwable cause ) {
        super( message, cause );
    }

}
