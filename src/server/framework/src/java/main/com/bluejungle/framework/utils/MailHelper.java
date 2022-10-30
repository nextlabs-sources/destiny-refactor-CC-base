/*
 * Created on Apr 18, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.LifestyleType;
import com.sun.mail.smtp.SMTPMessage;
import java.util.Date;

/**
 * A wrapper around java mail that provides a simple API for sending simple messages
 *
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/utils/MailHelper.java#1 $
 */

public class MailHelper implements IMailHelper, IConfigurable, IInitializable {

    public static final ComponentInfo<MailHelper> COMP_INFO =
        new ComponentInfo<MailHelper>(COMP_NAME, 
                MailHelper.class, 
                IMailHelper.class, 
                LifestyleType.SINGLETON_TYPE);
    
    private static final int DEFAULT_PORT = 25;

    private Session session;

    private IConfiguration config;
    private String host;
    private int port;
    private String userName;
    private String password;
    private String headerFrom;

    private static final Properties SESSION_PROPS = new Properties();

    /**
     * Sends an email message
     *
     * @param from from email address. If null, the from address
     * is defaulted to headerFrom.
     * @param to a collection of email addresses to send to
     * @param subject subject of the message
     * @param body body of the message
     * @throws MessagingException when the message cannot be sent.
     */
    public void sendMessage(String from,
            String to,
            String subject,
            String body) throws MessagingException {

        // If no address is provided, use the default address
        if ((from == null) || from.equals("")) {
            from = headerFrom;
        }
        Address fromAddr = new InternetAddress(from, false);
        Address[] toAddr = InternetAddress.parse(to, false);
        SMTPMessage message = new SMTPMessage(session);

        if (toAddr.length == 0) {
            throw new MessagingException("missing \"to addrress\"");
        }

        message.addHeader("Mime-Version", "1.0");
        message.setFrom(fromAddr);
        message.setRecipients(Message.RecipientType.TO, toAddr);
        message.setSubject(subject);
        message.setSentDate(new Date());
        message.setText(body);

        Transport transport = session.getTransport("smtp");
        transport.connect(host, port, userName, password);
        
        MessagingException firstException = null;
        try {
            transport.sendMessage(message, message.getAllRecipients());
        } catch (SendFailedException e) {
            // try sending to any valid addresses
            Address[] valids = e.getValidUnsentAddresses();
            if (valids != null && valids.length > 0) {
                transport.sendMessage(message, valids);
            }
        } catch (MessagingException me) {
            firstException = me;
        } finally {
            try {
                transport.close();
            } catch (MessagingException closingException) {
                if (firstException == null) {
                    firstException = closingException;
                }
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;

        host = config.get(SERVER_CFG_KEY);
        if (host == null || host.length() == 0) {
            throw new NullPointerException(SERVER_CFG_KEY.toString());
        }
        port = config.get(PORT_CFG_KEY, DEFAULT_PORT);

        userName = config.get(USER_CFG_KEY);
        password = config.get(PASSWORD_CFG_KEY);
        headerFrom = config.get(HEADER_FROM_CFG_KEY);
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return config;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        SESSION_PROPS.setProperty("mail.smtp.from", "");
        session = Session.getDefaultInstance(SESSION_PROPS);
    }

}
