/*
 * Created on Sep 2, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.framework.messaging.impl;

import java.util.Calendar;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/framework/messaging/impl/StringMessage.java#1 $
 */

public class StringMessage extends BaseMessage {
    private final String text;
    
    public StringMessage(String subject, String content) {
        this(subject, content, Calendar.getInstance());
    }

    public StringMessage(String subject, String content, Calendar time) {
        super(subject, time);
        this.text = content;
    }

    public String getMessageText() {
        return text;
    }
}
