package com.nextlabs.framework.messaging;

/**
 * This interface contains a set of constants defining the names of handlers 
 * that are required for Destiny to function and must be configured for each 
 * installation.  In Destiny 1.0, it will contain a single constant specifying 
 * the name of the E-mail message handler. 
 *
 */
public interface ISystemMessageHandlers {
 
	IMessageHandler EMAIL_MESSAGE_HANDLER = null;
	 
}
 
