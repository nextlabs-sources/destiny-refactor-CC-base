/*
 * Created on Jul 20, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.framework.messaging.handlers;

import javax.mail.internet.MimeMessage;

import com.nextlabs.framework.messaging.IMessage;
import com.nextlabs.framework.messaging.IMessageHandlerInstructions;
import com.nextlabs.framework.messaging.MessagingException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/framework/messaging/handlers/MimeMessageHandler.java#1 $
 */

public class MimeMessageHandler extends EmailMessageHandler{
    
    @Override
    public void sendMessage(IMessage message,
            IMessageHandlerInstructions messageHandlerInstructions) throws MessagingException {
        if (!(message instanceof com.nextlabs.framework.messaging.impl.MimeMessage)) {
            //detour if the message is not MimeMessage
            super.sendMessage(message, messageHandlerInstructions);
            return;
        }
        
        MimeMessage mimeMessage = new MimeMessage(session);
        
        try {
            setAddressers(mimeMessage, messageHandlerInstructions);
            
            mimeMessage.setSubject(message.getMessageSubject());
            mimeMessage.setSentDate(message.getMessageTimestamp().getTime());
           
            mimeMessage.setContent((
                    (com.nextlabs.framework.messaging.impl.MimeMessage) message).getBody()
            );
      
        } catch (javax.mail.MessagingException e) {
            throw MessagingException.sending(message, e);
        }
        
        try {
            sendMessage(mimeMessage);
        } catch (javax.mail.MessagingException e) {
            throw MessagingException.sending(message, e);
        }
    }
}
