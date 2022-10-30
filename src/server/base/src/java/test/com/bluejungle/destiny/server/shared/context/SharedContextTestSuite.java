/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.context;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is a suite of test to test the Destiny shared context
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/context/tests/SharedContextTestSuite.java#2 $:
 */

public class SharedContextTestSuite {

    /**
     * Returns the test suite for the shared context
     * 
     * @return the test suite for the shared context
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Shared context");
        suite.addTest(new SharedContextTest("testSharedModulesInstantiation"));
        suite.addTest(new SharedContextTest("testInterfaceImplementation"));
        suite.addTest(new SharedContextTest("testInstantiationExceptions"));
        suite.addTest(new SharedContextTest("testNoDoubleInits"));
        suite.addTest(new SharedContextTest("testDestinyDCSFRegistrationSubscription"));
        return (suite);
    }
}