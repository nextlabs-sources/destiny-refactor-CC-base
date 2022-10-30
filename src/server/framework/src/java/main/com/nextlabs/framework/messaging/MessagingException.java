/*
 * Created on Jul 16, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.framework.messaging;

import javax.mail.Address;
import javax.mail.SendFailedException;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.utils.ArrayUtils;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/framework/messaging/MessagingException.java#1 $
 */

public class MessagingException extends Exception {
    public enum Type{
        COMPOSE_MESSAGE,
        SENDING_MESSAGE,
        CONFIG,
        INIT,
        UNKNOWN,
    }
    
    private final Type type;
    
    public MessagingException(String message, Type type, Exception e){
        super(message, e);
        this.type = type;
    }
    
    public MessagingException(String message, Type type){
        super(message);
        this.type = type;
    }
    
    public static MessagingException sending(IMessage message, Exception e){
        final String N = ConsoleDisplayHelper.NEWLINE;
        StringBuilder sb = new StringBuilder();
        sb.append("Error when sending message").append(N)
          .append(message.toString());
        
        if(e instanceof SendFailedException){
            sb.append(N);
            Address[] invalidAddresses = ((SendFailedException)e).getInvalidAddresses();
            if(invalidAddresses!= null){
                sb.append("  to the following list of address:").append(N)
                  .append(ArrayUtils.asString(invalidAddresses, N));
            }
        }
        
        return new MessagingException(sb.toString(), Type.SENDING_MESSAGE, e);
    }
    
    public static MessagingException sending(IMessage message, Address[] invalid){
        StringBuilder sb = new StringBuilder();
        sb.append("Error when sending message \"").append(message.toString()).append("\"");
        sb.append(" to ").append(ArrayUtils.asString(invalid));
        
        return new MessagingException(sb.toString(), Type.SENDING_MESSAGE);
    }
    
    public static MessagingException composing(IMessage message, Exception e){
        StringBuilder sb = new StringBuilder();
        sb.append("Error when composing message \"").append(message.toString()).append("\"");
        
        return new MessagingException(sb.toString(), Type.COMPOSE_MESSAGE, e);
    }
    
    public MessagingException(PropertyKey<?> key, Exception e){
        this("Invalid configuration on \"" + key.toString() +"\"", Type.CONFIG, e);
    }
    
    public MessagingException(Type type, Exception e){
        this("Error during " + type.name(), type, e);
    }
    
//    public MessagingException(Exception e){
//        super(e);
//        this.type = Type.UNKNOWN;
//    }
//    
//    public MessagingException(String message, Exception e){
//        super(message, e);
//        this.type = Type.UNKNOWN;
//    }
}
