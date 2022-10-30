/*
 * Created on Jul 16, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.framework.messaging.impl;

import javax.mail.Address;

import com.bluejungle.framework.comp.PropertyKey;
import com.nextlabs.framework.messaging.IMessageHandlerConfig;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/framework/messaging/impl/IEmailMessageHandlerConfig.java#1 $
 */

public interface IEmailMessageHandlerConfig extends IMessageHandlerConfig{
    
    /**
     * Optional
     * If the value is not set, it will be based on the username. 
     * If the username is set, the auth will be true
     */
    PropertyKey<Boolean> AUTH         = new PropertyKey<Boolean>("auth");
    
    /**
     * Require
     */
    PropertyKey<String> SERVER        = new PropertyKey<String>("server");
    
    /**
     * Optional, default is 25
     */
    PropertyKey<Integer> PORT         = new PropertyKey<Integer>("port");
    
    /**
     * Require if AUTH is true
     */
    PropertyKey<String> USER          = new PropertyKey<String>("username");
    
    /**
     * Require if AUTH is true, the password must be encrypted.
     */
    PropertyKey<String> PASSWORD      = new PropertyKey<String>("password");
    
    /**
     * Optional
     */    
    PropertyKey<Address> DEFAULT_FROM = new PropertyKey<Address>("default_from");
    
    /**
     * Optional
     */
    PropertyKey<Address[]> DEFAULT_TO = new PropertyKey<Address[]>("default_to");
    
    /**
     * Optional
     */
    PropertyKey<Address[]> DEFAULT_CC = new PropertyKey<Address[]>("default_cc");
    
    /**
     * Optional
     */
    PropertyKey<Address[]> REPLY_TO   = new PropertyKey<Address[]>("reply_to");

}
