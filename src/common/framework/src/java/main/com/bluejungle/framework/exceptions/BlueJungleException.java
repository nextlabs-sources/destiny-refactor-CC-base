/*
 * Created on Dec 24, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.exceptions;


/**
 * Base Exception class for Blue Jungle exceptions.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public abstract class BlueJungleException extends Exception {

    /**
     * Constructor
     */
    public BlueJungleException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param cause -
     *            Throwable object that is to be wrapped inside this exception.
     */
    public BlueJungleException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the NON-localized message for this exception instance.
     * 
     * @return Message
     * @see java.lang.Throwable#getMessage()
     */
    public abstract String getMessage();

    /**
     * Returns the localized message for this exception instance.
     * 
     * @return Message
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    public abstract String getLocalizedMessage();
}