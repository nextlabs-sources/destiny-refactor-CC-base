/*
 * Created on Nov 15, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dabs;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the test suite for DABS
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_112/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/DABSTestSuite.java#1 $
 */
public class DABSTestSuite {

    /**
     * Returns the test suite to be run
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("DABS Component");
        suite.addTest(getAgentDeploymentServiceTestSuite());
        suite.addTest(getPolicyDeploymentServiceTestSuite());
        suite.addTest(new TestSuite(LogServiceTest.class, "DABS Log Service v1"));
        suite.addTest(new TestSuite(TestDABSHeartbeatMgrImpl.class, "DABS heartbeat manager"));
        return suite;
    }

    /**
     * Returns the test suite for the DABS Agent Deployment Service
     * 
     * @return
     */
    public static Test getAgentDeploymentServiceTestSuite() {
        TestSuite suite = new TestSuite("Agent Deployment Service");
        suite.addTest(new AgentDeploymentServiceTest("testAgentDeploy"));
        suite.addTest(new AgentDeploymentServiceTest("testAgentUndeploy"));
        return (suite);
    }

    /**
     * Returns the test suite for the Policy Deployment Service
     * 
     * @return
     */
    public static Test getPolicyDeploymentServiceTestSuite() {
        TestSuite suite = new TestSuite("Policy Deployment Service");
        suite.addTest(new PolicyDeploymentServiceTest("testInvalidatePolicy"));
        suite.addTest(new PolicyDeploymentServiceTest("testPushPolicy"));
        return (suite);
    }
}
