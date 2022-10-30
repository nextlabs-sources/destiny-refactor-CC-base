/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.server.shared.context.SharedContextTestSuite;
import com.bluejungle.destiny.server.shared.events.EventManagerTestSuite;
import com.bluejungle.destiny.server.shared.registration.RegistrationManagerTestSuite;
import com.bluejungle.destiny.server.shared.repository.ConnectionTrackingConnectionPoolWrapperTest;

/**
 * Master test suite for the DCSF shared module
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/tests/DCSFTestSuite.java#1 $:
 */

public class DCSFTestSuite {

    /**
     * Returns the test suite for the DCSF shared module
     * 
     * @return the test suite for the DCSF shared module
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("DCSF shared context");
        suite.addTest(SharedContextTestSuite.suite());
        suite.addTest(EventManagerTestSuite.suite());
        suite.addTest(RegistrationManagerTestSuite.suite());
        suite.addTest(new TestSuite(ConnectionTrackingConnectionPoolWrapperTest.class));
        return (suite);
    }

}