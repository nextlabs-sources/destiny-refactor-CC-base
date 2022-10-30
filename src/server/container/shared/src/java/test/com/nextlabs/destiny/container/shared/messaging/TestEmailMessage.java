/*
 * Created on Jul 17, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.messaging;

import java.io.File;

import javax.mail.internet.InternetAddress;

import com.nextlabs.framework.messaging.handlers.EmailMessageHandler;
import com.nextlabs.framework.messaging.handlers.MimeMessageHandler;
import com.nextlabs.framework.messaging.impl.EmailMessageHandlerInstructions;
import com.nextlabs.framework.messaging.impl.IEmailMessageHandlerConfig;
import com.nextlabs.framework.messaging.impl.MapMessageHandlerConfig;
import com.nextlabs.framework.messaging.impl.MimeMessage;
import com.nextlabs.framework.messaging.impl.StringMessage;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/messaging/TestEmailMessage.java#1 $
 */

public class TestEmailMessage {
    
    public static void main(String[] args) throws Exception {
        sendMime();
    }
    
    public static void sendSmtp() throws Exception{
        StringMessage message = new StringMessage("testSubj3ct", "testC0nt3xt");
        
        EmailMessageHandler handler = new EmailMessageHandler();
        
        MapMessageHandlerConfig config = new MapMessageHandlerConfig();
        config.setProperty(IEmailMessageHandlerConfig.AUTH, false);
        config.setProperty(IEmailMessageHandlerConfig.SERVER, "nevis.bluejungle.com");
        config.setProperty(IEmailMessageHandlerConfig.DEFAULT_FROM, new InternetAddress(
                "nobody@nextlabs.com"));
        
        handler.init(config, "name");
        
        EmailMessageHandlerInstructions instructions = new EmailMessageHandlerInstructions();
        instructions.setFrom("ppp@nextlabs.com");
        instructions.addTo("hchan@nextlabs.com");
        instructions.addTo("fdasfasdfasdfsde@nextlabs.com");
        instructions.addTo("Hor-kan.Chan@nextlabs.com");
        
        
        handler.sendMessage(message, instructions);
    }
    
    public static void sendMime() throws Exception{
        MimeMessage message = new MimeMessage("testSubj3ct");
        StringBuffer sb = new StringBuffer();
        sb.append("<HTML>\n");
        sb.append("<HEAD>\n");
        sb.append("<TITLE>\n");
        sb.append("subj4ct" + "\n");
        sb.append("</TITLE>\n");
        sb.append("</HEAD>\n");

        sb.append("<BODY>\n");
        sb.append("<H1>" + "subj4ct" + "</H1>" + "\n");

        sb.append("something here is the context of the messagte");
        sb.append("\n");

        sb.append("</BODY>\n");
        sb.append("</HTML>\n");
        
        message.addHtml(sb.toString());
        
        message.addFile(new File("C:/temp/1.jpg"));
        
        
        
        MimeMessageHandler handler = new MimeMessageHandler();
        
        MapMessageHandlerConfig config = new MapMessageHandlerConfig();
        config.setProperty(IEmailMessageHandlerConfig.AUTH, false);
        config.setProperty(IEmailMessageHandlerConfig.SERVER, "nevis.bluejungle.com");
        config.setProperty(IEmailMessageHandlerConfig.DEFAULT_FROM, new InternetAddress(
                "nobody@nextlabs.com"));
        
        handler.init(config, "name");
        
        EmailMessageHandlerInstructions instructions = new EmailMessageHandlerInstructions();
        instructions.setFrom("ppp@nextlabs.com");
        instructions.addTo("hchan@nextlabs.com");
        instructions.addTo("fdasfasdfasdfsde@nextlabs.com");
        instructions.addTo("Hor-kan.Chan@nextlabs.com");
        
        handler.sendMessage(message, instructions);
    }

}
