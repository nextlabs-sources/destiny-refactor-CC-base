package com.nextlabs.framework.messaging;

/**
 * IMessageHandlerManager is a manager used for retrieving and defining 
 * individual message handlers.  Message handlers can be defined either 
 * through the Destiny configuration file or at runtime by a client API 
 * with a specific need.  Destiny 1.0 will provide a single implementation 
 * of this interface which will create, for each defined message handler, 
 * one instance of the message handler class (another option is to create 
 * a pool of instances if the message handler class cannot be thread safe, 
 * but this shouldn’t be necessary).
 */
public interface IMessageHandlerManager {
    /**
     * Retrieve a message handler by name
     * @param name
     * @return
     */
	IMessageHandler getMessageHandler(String name);
	
	/**
	 * Define a message handler.
	 * @param senderClass
	 * @param handlerConfig
	 * @return
	 * @throws MessagingException
	 */
	IMessageHandler defineMessageHandler(
	        String name,
	        Class<? extends IMessageHandler> senderClass, 
	        IMessageHandlerConfig handlerConfig) 
	throws MessagingException;
}
 
