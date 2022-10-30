package com.nextlabs.framework.messaging.handlers;

import com.nextlabs.framework.messaging.IMessage;
import com.nextlabs.framework.messaging.IMessageHandler;
import com.nextlabs.framework.messaging.IMessageHandlerConfig;
import com.nextlabs.framework.messaging.IMessageHandlerInstructions;
import com.nextlabs.framework.messaging.MessagingException;

public class LogMessageHandler extends BaseMessageHandler implements IMessageHandler {

    @Override
    protected void init(IMessageHandlerConfig config) throws MessagingException {
        throw new UnsupportedOperationException();
    }
    
    public void sendMessage(IMessage message, IMessageHandlerInstructions messageHandlerInstructions)
            throws MessagingException {
        throw new UnsupportedOperationException();
    }
}
 
