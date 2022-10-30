package com.nextlabs.framework.messaging;

import com.bluejungle.framework.comp.PropertyKey;

/**
 * To send a particular message, the message handler must be provided with 
 * additional instructions or properties that may be specific to the handler 
 * being utilized.  For instance, in the case of the Email message handler, 
 * one of the properties required is the e-mail address of the recipient of 
 * the message.  Having this interface defined separately from the IMessage 
 * interface allows a single message to be sent multiple times with separate 
 * instructions.
 * 
 * In Destiny 1.0, the properties for specific handlers will be well known. 
 * In future versions, however, the IMessageHandler interface will be augmented 
 * to allow a handler implementation class to describe the properties it requires.
 */
public interface IMessageHandlerInstructions {
    
    /**
     * Retrieve a configuration property by name
     * @param propertyName
     * @return
     */
	<T> T getProperty(PropertyKey<T> propertyName);
	
	<T> T getProperty(PropertyKey<T> propertyName, T defaultValue);
	
	boolean contains(PropertyKey<?> propertyName);
}
 
