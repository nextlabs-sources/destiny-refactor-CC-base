package com.nextlabs.framework.messaging;

import com.bluejungle.framework.comp.PropertyKey;

/**
 * An instance of IMessageHandler provides functionality for sending a message 
 * using a particular delivery mechanism.  All implementations must provide a 
 * public default constructor.
 */
public interface IMessageHandler {
    PropertyKey<String> HANDLER_NAME = new PropertyKey<String>("handler_name");
    
    /**
     * Retrieve the name of the message handler.  Serves as a message handler identifier
     * @return the name of this message handler
     */
	String getName();

	/**
	 * Initialize the message handler with a static set of configuration parameters
	 * @param messageConfig
	 */
	void init(IMessageHandlerConfig config, String name) throws MessagingException;
	
	/**
	 * Send an individual message
	 * @param message
	 * @param messageHandlerInstructions
	 * @throws MessagingException
	 */
	void sendMessage(IMessage message, IMessageHandlerInstructions messageHandlerInstructions)
            throws MessagingException;
	
	/**
	 * 
	 * @param <T>
	 * @param key
	 * @return the value that is corresponding the key or null
	 */
	<T> T getProperty(PropertyKey<T> key);
}
 
