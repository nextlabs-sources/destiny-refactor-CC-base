/*
 * Created on Oct 29, 2004
 */
package com.bluejungle.destiny.server.shared.exceptions;

/**
 * @author ihanen This exception is thrown when one factory cannot be
 *         initialized properly
 */
public class FactoryInitException extends Exception {

    /**
     * Constructor
     */
    public FactoryInitException() {
        super();
    }

    /**
     * Contructor
     * 
     * @param message
     *            message to associate with the exception
     */
    public FactoryInitException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param message
     *            message to associate with the exception
     * @param cause
     *            cause to associate with the exception
     */
    public FactoryInitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param cause
     *            cause to associate with the exception
     */
    public FactoryInitException(Throwable cause) {
        super(cause);
    }
}