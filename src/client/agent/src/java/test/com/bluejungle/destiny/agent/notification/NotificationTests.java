/*
 * Created on Apr 19, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.notification;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/src/java/test/com/bluejungle/destiny/agent/notification/NotificationTests.java#1 $:
 */

public class NotificationTests {

    public static void main(String[] args) {
        junit.swingui.TestRunner.run(NotificationTests.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.agent.notification");
        //$JUnit-BEGIN$
        suite.addTestSuite(NotificationTest1.class);
        suite.addTestSuite(NotificationTest2.class);
        //$JUnit-END$
        return suite;
    }
}
