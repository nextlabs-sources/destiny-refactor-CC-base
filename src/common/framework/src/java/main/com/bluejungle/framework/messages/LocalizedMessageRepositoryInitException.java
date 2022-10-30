/*
 * Created on Dec 27, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.messages;

/**
 * Exception class to indicate that the LocalizedMessageRepository could not be
 * initialized.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class LocalizedMessageRepositoryInitException extends Exception {

    /**
     * Constructor
     *  
     */
    public LocalizedMessageRepositoryInitException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param message
     */
    public LocalizedMessageRepositoryInitException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public LocalizedMessageRepositoryInitException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     * 
     * @param message
     * @param cause
     */
    public LocalizedMessageRepositoryInitException(String message, Throwable cause) {
        super(message, cause);
    }

}