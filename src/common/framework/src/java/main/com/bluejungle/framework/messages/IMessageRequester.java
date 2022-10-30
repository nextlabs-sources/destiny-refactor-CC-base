/*
 * Created on Mar 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.messages;

/**
 * This interface represents an object containing enough information to identify
 * the message that needs to be retrieved, and the parameters that are required
 * to format the message. Knowing the correct parameters required to format the
 * requested message is the respnosibility of this object.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/messages/IMessageRequester.java#1 $
 */

public interface IMessageRequester {

    /**
     * Return the key of the message being requested
     * 
     * @return key of the message
     */
    public String getMessageKey();

    /**
     * Return an array of objects representing the values to be plugged into the
     * message placeholders.
     * 
     * @return array of placeholder values (as Object types)
     */
    public Object[] getIndexedValues();
}