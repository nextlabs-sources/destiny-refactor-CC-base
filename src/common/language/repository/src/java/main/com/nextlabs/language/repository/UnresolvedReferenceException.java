package com.nextlabs.language.repository;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/repository/src/java/main/com/nextlabs/language/repository/UnresolvedReferenceException.java#1 $
 */

/**
 * An exception signaling that the reference cannot be resolved.
 *
 * @author Sergey Kalinichenko
 */
public class UnresolvedReferenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @see Exception#Exception()
     */
    public UnresolvedReferenceException() {
    }

    /**
     * @see Exception#Exception(String)
     */
    public UnresolvedReferenceException(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public UnresolvedReferenceException(Throwable cause) {
        super(cause);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public UnresolvedReferenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
