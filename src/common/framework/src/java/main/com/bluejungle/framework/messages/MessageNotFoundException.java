/*
 * Created on Mar 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.messages;

/**
 * Exception class indicating that a requested message was not found in the
 * message repository in question.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/messages/MessageNotFoundException.java#1 $
 */

public class MessageNotFoundException extends Exception {

    /**
     * Constructor
     *  
     */
    public MessageNotFoundException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public MessageNotFoundException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public MessageNotFoundException(Throwable arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * 
     * @param arg0
     * @param arg1
     */
    public MessageNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}