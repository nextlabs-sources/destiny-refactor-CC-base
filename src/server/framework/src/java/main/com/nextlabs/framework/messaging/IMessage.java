package com.nextlabs.framework.messaging;

import java.util.Calendar;

/**
 * Defines an individual message to be sent. 
 * Instances are provided by clients of the Messaging API.
 *
 */
public interface IMessage {
 
    /**
     * Retrieve the subject of the message
     * @return the subject of the message
     */
	String getMessageSubject();
	
	/**
	 * Retrieve the text of the message
	 * @return the text of the message
	 */
	String getMessageText();
	
	/**
	 * Retrieve the timestamp of the message
	 * @return the timestamp of the message
	 */
	Calendar getMessageTimestamp();
	
//	B getType();
}
 
