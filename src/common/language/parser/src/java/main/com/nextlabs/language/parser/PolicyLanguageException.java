package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/main/com/nextlabs/language/parser/PolicyLanguageException.java#1 $
 */

/**
 *
 * @author Sergey Kalinichenko
 */
public class PolicyLanguageException extends Exception {

    /**
     * This is the default serialization UID required for all Exceptions.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a PolicyLanguageException with the specified
     * message and the cause.
     *
     * @param message the message of this exception.
     * @param cause the cause of this exception.
     */
    public PolicyLanguageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a PolicyLanguageException with the specified cause.
     *
     * @param cause the cause of this exception.
     */
    public PolicyLanguageException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a PolicyLanguageException with the specified message.
     *
     * @param message the message of this exception.
     */
    public PolicyLanguageException(String message) {
        super(message);
    }

}
