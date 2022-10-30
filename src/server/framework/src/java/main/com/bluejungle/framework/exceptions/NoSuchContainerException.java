/*
 * Created on Oct 20, 2004
 */
package com.bluejungle.framework.exceptions;

/**
 * @author ihanen This exception is thrown when container factory cannot find a
 *         particular container.
 */
public class NoSuchContainerException extends Exception {

    /**
     * Constructor
     * 
     * @param msg
     *            message associated with the exception
     */
    public NoSuchContainerException(String msg) {
        super(msg);
    }
}