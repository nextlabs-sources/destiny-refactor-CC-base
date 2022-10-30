/*
 * Created on Jul 16, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.framework.messaging.impl;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.bluejungle.framework.comp.PropertyKey;
import com.nextlabs.framework.messaging.IMessageHandlerInstructions;
import com.nextlabs.framework.messaging.MessagingException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/framework/messaging/impl/EmailMessageHandlerInstructions.java#1 $
 */

public class EmailMessageHandlerInstructions extends MapMessageHandlerInstructions implements
        IMessageHandlerInstructions {
    
    public static final PropertyKey<Address> FROM = 
        new PropertyKey<Address>(EmailMessageHandlerInstructions.class, "from");

    public static final PropertyKey<Map<Message.RecipientType, Address[]>> TO = 
        new PropertyKey<Map<Message.RecipientType, Address[]>>(EmailMessageHandlerInstructions.class, "to");

    public void setFrom(String fromAddress) throws MessagingException{
        Address fromAddr;
        try {
            fromAddr = new InternetAddress(fromAddress);
        } catch (AddressException e) {
            throw new MessagingException(MessagingException.Type.COMPOSE_MESSAGE, e);
        }
        setProperty(FROM, fromAddr);
    }
    
    protected void addTo(Message.RecipientType type, String address) throws MessagingException{
        Address addr;
        try {
            addr = new InternetAddress(address);
        } catch (AddressException e) {
            throw new MessagingException(MessagingException.Type.COMPOSE_MESSAGE, e);
        }
        
        Map<Message.RecipientType, Address[]> map = this.get(TO);
        if (map == null) {
            map = new HashMap<Message.RecipientType, Address[]>();
            this.setProperty(TO, map);
        }
        
        Address[] existingAddresses = map.get(type);
        
        final Address[] newAddresses;
        if (existingAddresses == null) {
            newAddresses = new Address[] { addr };
        } else {
            int totalLength = existingAddresses.length;
            newAddresses = new Address[totalLength + 1];
            System.arraycopy(existingAddresses, 0, newAddresses, 0, totalLength);
            newAddresses[newAddresses.length - 1] = addr;
        }
        map.put(type, newAddresses);
    }
    
    public void addTo(String toAddress) throws MessagingException{
        addTo(Message.RecipientType.TO, toAddress);
    }
    
    public void addToCC(String toCCAddress) throws MessagingException{
        addTo(Message.RecipientType.CC, toCCAddress);
    }
    
    public void addToBCC(String toBCCAddress) throws MessagingException{
        addTo(Message.RecipientType.BCC, toBCCAddress);
    }
}
