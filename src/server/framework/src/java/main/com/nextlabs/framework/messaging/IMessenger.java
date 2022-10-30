package com.nextlabs.framework.messaging;

/**
 * This is a façade/proxy to the messaging API providing the following functionality:
 * 
 * 1.  An easy means for a client to send a message
 * 2.  An implementation specific means of controlling how messages are sent within 
 * Destiny (e.g. asynchronously)
 * 
 * In Destiny 1.0, there will exist a single implementation of this interface which 
 * will send messages asynchronously, allowing the product to continue to function 
 * in the case of a failure in a particular message handler.
 * 
 * In future versions, the interface could be extended to provide a means of sending 
 * multiple messages through a single method.
 */
public interface IMessenger {
 
    /**
     * Send a message using the specified message handler
     * @param messageToSend
     * @param handlerInstructions
     * @param messageHandlerName
     */
	void sendMessage(
	        IMessage messageToSend, 
	        IMessageHandlerInstructions handlerInstructions, 
	        String messageHandlerName);
	
	void sendMessage(
            IMessage messageToSend, 
            IMessageHandlerInstructions handlerInstructions, 
            Class<IMessageHandler> messageHandlerName);
}
 
