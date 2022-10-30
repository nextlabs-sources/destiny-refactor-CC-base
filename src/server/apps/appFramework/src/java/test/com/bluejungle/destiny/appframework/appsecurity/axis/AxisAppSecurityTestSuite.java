/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.axis;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Unit test suite for app security axis handler tests
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/test/com/bluejungle/destiny/appframework/appsecurity/axis/AxisAppSecurityTestSuite.java#1 $
 */

public class AxisAppSecurityTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AxisAppSecurityTestSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.appsecurity.axis");
        //$JUnit-BEGIN$
        suite.addTestSuite(AuthenticationHandlerTest.class);
        //$JUnit-END$
        return suite;
    }
}
