/*
 * Created on Jul 20, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.framework.messaging.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.bluejungle.framework.utils.StringUtils;
import com.nextlabs.framework.messaging.IMessage;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/framework/messaging/impl/BaseMessage.java#1 $
 */

public abstract class BaseMessage implements IMessage {
    protected static final SimpleDateFormat FORMAT = new SimpleDateFormat();
    
    private final String subject;
    private final Calendar time;
    
    public BaseMessage(String subject, Calendar time) {
        this.subject = subject;
        this.time = time != null ? time : Calendar.getInstance();
    }
    
    public String getMessageSubject() {
        return subject;
    }

    public Calendar getMessageTimestamp() {
        return time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("type: ").append(this.getClass().getSimpleName())
          .append(ConsoleDisplayHelper.NEWLINE)
          .append("subject: ").append(StringUtils.limitLength(subject, 20))
          .append(ConsoleDisplayHelper.NEWLINE)
          .append("time: ").append(FORMAT.format(time.getTime()));
        return sb.toString();
    }
}
