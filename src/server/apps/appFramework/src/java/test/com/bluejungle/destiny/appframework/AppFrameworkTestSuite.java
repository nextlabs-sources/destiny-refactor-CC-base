/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework;

import com.bluejungle.destiny.appframework.appsecurity.axis.AxisAppSecurityTestSuite;
import com.bluejungle.destiny.appframework.appsecurity.i18n.OptionItemResourceListFactoryTest;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote.RemoteLoginManagerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test Suite for App Framework
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/apps/appFramework/src/java/test/com/bluejungle/destiny/appframework/AppFrameworkTestSuite.java#1 $
 */

public class AppFrameworkTestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Application framework");
        suite.addTest(AxisAppSecurityTestSuite.suite());
        suite.addTest(new TestSuite(RemoteLoginManagerTest.class, "Remote Login manager"));
        suite.addTest(new TestSuite(OptionItemResourceListFactoryTest.class, "Option Item Resource List Factory Test"));
        return suite;
    }
}