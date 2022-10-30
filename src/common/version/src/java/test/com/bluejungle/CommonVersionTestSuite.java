/*
 * Created on Jun 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle;

import com.bluejungle.version.VersionDefaultImplTest;
import com.bluejungle.versionfactory.VersionFactoryTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Common version test suite
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/version/src/java/test/com/bluejungle/CommonVersionTestSuite.java#1 $
 */

public class CommonVersionTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle");
        suite.addTestSuite(VersionDefaultImplTest.class);
        suite.addTestSuite(VersionFactoryTest.class);
        return suite;
    }
}
