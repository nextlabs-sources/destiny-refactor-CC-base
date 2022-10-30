package com.nextlabs.framework.messaging.handlers;

import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.framework.messaging.IMessage;
import com.nextlabs.framework.messaging.IMessageHandlerConfig;
import com.nextlabs.framework.messaging.IMessageHandlerInstructions;
import com.nextlabs.framework.messaging.MessagingException;
import com.nextlabs.framework.messaging.impl.EmailMessageHandlerInstructions;
import com.nextlabs.framework.messaging.impl.IEmailMessageHandlerConfig;
import com.nextlabs.framework.messaging.impl.MapMessageHandlerConfig;
import com.sun.mail.smtp.SMTPMessage;

/**
 * using SMTP
 * @author hchan
 *
 */
public class EmailMessageHandler extends BaseMessageHandler {
    
    public static final String DEFAULT_HANDLER_NAME = "DefaultEmailHandler";
    
    protected Session session;
    protected boolean auth;
    protected String host;
    protected int port;
    protected String userName;
    protected String password;
    protected Address defaultFrom;
    protected Address[] defaultTo;
    protected Address[] defaultCC;
    
    /**
     * @throws MessagingException 
     */
    @Override
    public void init(IMessageHandlerConfig config) throws MessagingException {
        Properties sessionProperties = new Properties();
//        sessionProperties.setProperty("mail.smtp.from", "");
        
        userName = config.getProperty(IEmailMessageHandlerConfig.USER);
        
        auth = config.getProperty(IEmailMessageHandlerConfig.AUTH, userName != null);
        if (auth) {
            password = getMustProperty(IEmailMessageHandlerConfig.PASSWORD, config);
            sessionProperties.put("mail.smtp.auth", "true");

        }
        session = Session.getDefaultInstance(sessionProperties);
        
        host = getMustProperty(IEmailMessageHandlerConfig.SERVER, config);
        port = config.getProperty(IEmailMessageHandlerConfig.PORT, 25);
        
        defaultFrom = config.getProperty(IEmailMessageHandlerConfig.DEFAULT_FROM);
        defaultTo = config.getProperty(IEmailMessageHandlerConfig.DEFAULT_TO);
        defaultCC = config.getProperty(IEmailMessageHandlerConfig.DEFAULT_CC);
    }
    
    @Override
    protected IMessageHandlerConfig filter(IMessageHandlerConfig config) throws MessagingException {
        MapMessageHandlerConfig newConfig = new MapMessageHandlerConfig(config); 
        
        newConfig.setProperty(IEmailMessageHandlerConfig.AUTH, 
                getBoolean(IEmailMessageHandlerConfig.AUTH, config));

        newConfig.setProperty(IEmailMessageHandlerConfig.SERVER, 
                config.getProperty(IEmailMessageHandlerConfig.SERVER));
        
        newConfig.setProperty(IEmailMessageHandlerConfig.PORT, 
                getInteger(IEmailMessageHandlerConfig.PORT, config));
        
        newConfig.setProperty(IEmailMessageHandlerConfig.USER, 
                config.getProperty(IEmailMessageHandlerConfig.USER));
        
        String encryptedPassword = config.getProperty(IEmailMessageHandlerConfig.PASSWORD);
        String password;
        if (encryptedPassword != null && encryptedPassword.length() > 0) {
            try {
                password = new ReversibleEncryptor().decrypt(encryptedPassword);
            } catch (RuntimeException e) {
                throw new MessagingException(MessagingException.Type.INIT, e);
            }
        } else {
            password = encryptedPassword;
        }
        newConfig.setProperty(IEmailMessageHandlerConfig.PASSWORD, password);
        
        newConfig.setProperty(IEmailMessageHandlerConfig.DEFAULT_FROM, 
                getAddress(IEmailMessageHandlerConfig.DEFAULT_FROM, config));
        
        newConfig.setProperty(IEmailMessageHandlerConfig.DEFAULT_TO, 
                getAddresses(IEmailMessageHandlerConfig.DEFAULT_TO, config));
        
        newConfig.setProperty(IEmailMessageHandlerConfig.DEFAULT_CC, 
                getAddresses(IEmailMessageHandlerConfig.DEFAULT_CC, config));

        return newConfig;
    }
    
    protected Address getAddress(PropertyKey<Address> key, IMessageHandlerConfig config)
            throws MessagingException {
        Object obj = config.getProperty(key);
        if(obj == null){
            return null;
        }
        Address a;
        if (obj instanceof Address) {
            a = (Address) obj;
        } else if (obj instanceof String) {
            try {
                a = new InternetAddress((String)obj);
            } catch (AddressException e) {
                throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Address"),
                        MessagingException.Type.CONFIG, e);
            }
        } else {
            throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Address"),
                    MessagingException.Type.CONFIG);
        }
        return a;
    }
    
    protected Address[] getAddresses(PropertyKey<Address[]> key, IMessageHandlerConfig config)
            throws MessagingException {
        Object obj = config.getProperty(key);
        if (obj == null) {
            return null;
        }
        Address[] a;
        if (obj instanceof Address) {
            a = new Address[]{(Address) obj};
        } else if (obj instanceof Address[]){
            a = (Address[]) obj;
        } else if (obj instanceof String){
            String s = (String)obj;
            String[] ss = s.split("[,;]");
            a = new Address[ss.length];
            try {
                for (int i = 0; i < ss.length; i++) {
                    a[i] = new InternetAddress(ss[i]);
                }
            } catch (AddressException e) {
                throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Address[]"),
                        MessagingException.Type.CONFIG, e);
            }
        } else if (obj instanceof String[]){
            String[] ss = (String[])obj;
            a = new Address[ss.length];
            try {
                for (int i = 0; i < ss.length; i++) {
                    a[i] = new InternetAddress(ss[i]);
                }
            } catch (AddressException e) {
                throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Address[]"),
                        MessagingException.Type.CONFIG, e);
            }
        } else {
            throw new MessagingException(String.format(INVALID_FORMAT, key, obj, "Address[]"),
                    MessagingException.Type.CONFIG);
        }
        return a;
    }
    
    protected void setAddressers(MimeMessage message,
            IMessageHandlerInstructions messageHandlerInstructions)
            throws javax.mail.MessagingException, MessagingException {
        if (messageHandlerInstructions == null) {
            messageHandlerInstructions = new EmailMessageHandlerInstructions();
        }
        message.setFrom(messageHandlerInstructions.getProperty(
                EmailMessageHandlerInstructions.FROM, defaultFrom));

        Map<Message.RecipientType, Address[]> toAddresses =
                messageHandlerInstructions.getProperty(EmailMessageHandlerInstructions.TO);
        if (toAddresses == null || toAddresses.isEmpty()) {
            if(defaultTo == null || defaultTo.length == 0){
                throw new MessagingException("The value of property \""
                        + EmailMessageHandlerInstructions.TO + "\", " + " is missing.",
                        MessagingException.Type.COMPOSE_MESSAGE);
            }
            //set the default address
            message.setRecipients(Message.RecipientType.TO, defaultTo);
            message.setRecipients(Message.RecipientType.CC, defaultCC);
        }else{
            for (Map.Entry<Message.RecipientType, Address[]> toAddress : toAddresses.entrySet()) {
                message.setRecipients(toAddress.getKey(), toAddress.getValue());
            }
        }
        
        Address[] relyToAddress;
        if((relyToAddress = messageHandlerInstructions.getProperty(
                IEmailMessageHandlerConfig.REPLY_TO)) != null){
            message.setReplyTo(relyToAddress);
        }
        
    }
    
    /**
     * @throws MessagingException 
     * @see com.nextlabs.framework.messaging.IMessageHandler#sendMessage(com.nextlabs.framework.messaging.IMessage, com.nextlabs.framework.messaging.IMessageHandlerInstructions)
     */
    public void sendMessage(IMessage message,
            IMessageHandlerInstructions messageHandlerInstructions)
            throws MessagingException {
        SMTPMessage smtpMessage = new SMTPMessage(session);
        
        try {
            setAddressers(smtpMessage, messageHandlerInstructions);
            
            smtpMessage.setSubject(message.getMessageSubject());
            smtpMessage.setSentDate(message.getMessageTimestamp().getTime());
            smtpMessage.setText(message.getMessageText());
            smtpMessage.setSendPartial(true);
        } catch (javax.mail.MessagingException e) {
            throw MessagingException.sending(message, e);
        }
        
        try {
            sendMessage(smtpMessage);
        } catch (javax.mail.MessagingException e) {
            throw MessagingException.sending(message, e);
        }
    }
    
    protected void sendMessage(Message message) throws javax.mail.MessagingException {
        javax.mail.MessagingException firstException = null;
        Transport transport = session.getTransport("smtp");;
        try {
            transport.connect(host, port, userName, password);
            transport.sendMessage(message, message.getAllRecipients());
        } catch(javax.mail.SendFailedException e){
            Address[] valids = e.getValidUnsentAddresses();
            if (valids != null && valids.length > 0) {
                transport.sendMessage(message, valids);
            }
            throw e;
        } catch(javax.mail.MessagingException e){
            firstException = e;
        } finally{
            try {
                transport.close();
            } catch (javax.mail.MessagingException e) {
                if(firstException == null){
                    firstException = e;
                }
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }
}
 
