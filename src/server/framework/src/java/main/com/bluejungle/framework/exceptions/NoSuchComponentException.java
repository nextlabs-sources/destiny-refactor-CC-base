/*
 * Created on Oct 20, 2004
 */
package com.bluejungle.framework.exceptions;

/**
 * @author ihanen This exception is thrown when a component factory cannot find
 *         a particular component
 */
public class NoSuchComponentException extends Exception {

    /**
     * Constructor
     * 
     * @param msg
     *            message associated with this exception
     */
    public NoSuchComponentException(String msg) {
        super(msg);
    }
}