/*
 * Created on Apr 18, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import java.util.Properties;

import javax.mail.MessagingException;

import junit.framework.TestCase;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/test/com/bluejungle/framework/utils/TestMailHelper.java#1 $:
 */

public class TestMailHelper extends TestCase {

    private IMailHelper mh;

    private static final String DOMAIN = "@bluejungle.com";
    private static final String INVALID_DOMAIN = "@this.domain.is.invalid";

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        HashMapConfiguration cfg = new HashMapConfiguration();
        cfg.setProperty(IMailHelper.SERVER_CFG_KEY, "nevis.bluejungle.com");
        cfg.setProperty(IMailHelper.HEADER_FROM_CFG_KEY, "jimmy.carter@test.bluejungle.com");
        mh = (IMailHelper) ComponentManagerFactory.getComponentManager().getComponent(MailHelper.COMP_INFO, cfg);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        ComponentManagerFactory.getComponentManager().shutdown();
    }

    /**
     * Constructor for TestMailHelper.
     * @param arg0
     */
    public TestMailHelper(String arg0) {
        super(arg0);
    }

 //   public final void testSendMessage() {
 //       Properties props = System.getProperties();	
 //       String user = props.getProperty("user.name");
 //       String to = user + INVALID_DOMAIN + ", " + user + DOMAIN + ", " + user + INVALID_DOMAIN;
 //       String subject = "test email";
 //       String body = "Dear " + user + ",\n\n" +
 //       "You're getting this email because Junit Test Case " + TestMailHelper.class.getName()+"#testSendMessage succeeded";
        	
 //       try {
 //           mh.sendMessage("<CompliantEnterprise>", to, subject, body);
 //       } catch (MessagingException e) {
 //           fail(e.getMessage());
 //       }
  //  }

 //   public final void testSendMessageFromSpecificAddress() {

 //       Properties props = System.getProperties();
 //       String user = props.getProperty("user.name");
 //       String to = user + INVALID_DOMAIN + ", " + user + DOMAIN + ", " + user + INVALID_DOMAIN;
 //       String subject = "test email";
 //       String body = "Dear " + user + ",\n\n" +
 //       "You're getting this email because Junit Test Case " + TestMailHelper.class.getName()+"#testSendMessageFromSpecificAddress succeeded";

 //       try {
 //           mh.sendMessage(null, to, subject, body);
 //       } catch (MessagingException e) {
  //          fail(e.getMessage());
 //       }
 //   }

}
