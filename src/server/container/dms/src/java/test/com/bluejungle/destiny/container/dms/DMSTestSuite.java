/*
 * Created on Dev 15, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dms;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the test suite for DMS
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/tests/DMSTestSuite.java#4 $
 */
public class DMSTestSuite {

    /**
     * @return the DMS test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("DMS Component");

        // Agent service test suite:
        suite.addTest(getAgentServiceTestSuite());

        // Profile Service Test
        suite.addTest(getProfileServiceTestSuite());

        // Component service tests
        suite.addTest(getComponentServiceTestSuite());
        
        suite.addTestSuite(UserCreationTest.class);
        return suite;
    }

    /**
     * Returns the test suite for the Agent service
     * 
     * @return the test suite for the Agent service
     */
    public static Test getAgentServiceTestSuite() {
        return AgentServiceTest.getTestSuite();
    }

    /**
     * Returns the test suite for the profile service
     * 
     * @return the test suite for the profile service
     */
    public static Test getProfileServiceTestSuite() {
        return ProfileServiceTest.getTestSuite();
    }

    /**
     * Returns the test suite for the component service
     * 
     * @return the test suite for the component service
     */
    public static Test getComponentServiceTestSuite() {
        return new TestSuite(ComponentServiceTest.class, "Component Service Test");
    }
}