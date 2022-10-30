/*
 * Created on Jul 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.utils;

import javax.mail.MessagingException;

import com.bluejungle.framework.comp.PropertyKey;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/utils/IMailHelper.java#1 $:
 */

public interface IMailHelper {

    String COMP_NAME = IMailHelper.class.getName();

    PropertyKey<String> SERVER_CFG_KEY      = new PropertyKey<String>("server");
    PropertyKey<Integer> PORT_CFG_KEY       = new PropertyKey<Integer>("port");
    PropertyKey<String> USER_CFG_KEY        = new PropertyKey<String>("user");
    PropertyKey<String> PASSWORD_CFG_KEY    = new PropertyKey<String>("password");
    PropertyKey<String> HEADER_FROM_CFG_KEY = new PropertyKey<String>("headerfrom");

    /**
     * Sends a simple mail message
     * 
     * @param from address
     * @param to comma-separated list of addresses
     * @param subject subject of the message
     * @param body message body
     * @throws MessagingException
     */
    public void sendMessage(String from, String to, String subject, String body)
            throws MessagingException;
}
