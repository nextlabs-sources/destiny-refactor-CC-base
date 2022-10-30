/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/DCSFWebAppTestSuite.java#3 $:
 */

public class DCSFWebAppTestSuite {

    /**
     * Returns the test suite for the DCSF shared module
     * 
     * @return the test suite for the DCSF shared module
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("DCSF Web Application");
        suite.addTest(getDMSRegistrationManagerTestSuite());
        suite.addTest(getRemoteEventDispatchMgrTestSuite());
        suite.addTest(getRemoteEventRegistrationMgrTestSuite());
        suite.addTest(getRemoteListenerRegistrationMgrTestSuite());
        suite.addTest(getDCSFServiceTestSuite());
        suite.addTest(getHeartbeatMgrTestSuite());
        return (suite);
    }

    /**
     * Returns the test suite for the DMS registration manager
     * 
     * @return the test suite for the DMS registration manager
     */
    protected static Test getDMSRegistrationManagerTestSuite() {
        TestSuite suite = new TestSuite("DMS Registration Manager");
        suite.addTest(new DMSRegistrationMgrTest("testInitialization"));
        suite.addTest(new DMSRegistrationMgrTest("testInitialArguments"));
        suite.addTest(new DMSRegistrationMgrTest("testDMSRegistrationRequest"));
        suite.addTest(new DMSRegistrationMgrTest("testCallbackOnRegistration"));
        return (suite);
    }

    /**
     * Returns the test suite for the DMS registration manager
     * 
     * @return the test suite for the remote event dispatch manager
     */
    protected static Test getRemoteEventDispatchMgrTestSuite() {
        TestSuite suite = new TestSuite("Remote Event Dispatch Manager");
        suite.addTest(new RemoteEventDispatchMgrTest("testRemoteEventDispatchMgrInit"));
        suite.addTest(new RemoteEventDispatchMgrTest("testRemoteEventDispatchMgrEventFiring"));
        suite.addTest(new RemoteEventDispatchMgrTest("testEventDispatchWorkerThread"));
        suite.addTest(new RemoteEventDispatchMgrTest("testEventDispatchWorkerThreadArguments"));
        return (suite);
    }

    /**
     * Returns the test suite for the remote event registration manager
     * 
     * @return the test suite for the remote event registration manager
     */
    protected static Test getRemoteEventRegistrationMgrTestSuite() {
        TestSuite suite = new TestSuite("Remote Event Registration Manager");
        suite.addTest(new RemoteEventRegistrationMgrTest("testRemoteEventRegistrationMgrInit"));
        suite.addTest(new RemoteEventRegistrationMgrTest("testEventRegistrationRequestHandling"));
        suite.addTest(new RemoteEventRegistrationMgrTest("testEventUnregistrationRequestHandling"));
        suite.addTest(new RemoteEventRegistrationMgrTest("testEventCancellation"));
        return (suite);
    }
    
    /**
     * Returns the test suite for the remote listener registration manager
     * 
     * @return the test suite for the remote listener registration manager
     */
    protected static Test getRemoteListenerRegistrationMgrTestSuite() {
        TestSuite suite = new TestSuite("Remote Listener Registration Manager");
        suite.addTest(new RemoteListenerRegistrationMgrTest ("testConfigurationValidation"));
        suite.addTest(new RemoteListenerRegistrationMgrTest ("testIdenticalListeners"));
        suite.addTest(new RemoteListenerRegistrationMgrTest ("testComponentAPIs"));
        return (suite);
    }

    /**
     * Returns the test suite for the remote event registration manager
     * 
     * @return the test suite for the remote event registration manager
     */
    protected static Test getDCSFServiceTestSuite() {
        TestSuite suite = new TestSuite("DCSF Web Service -- standalone");
        suite.addTest(new DCSFServiceTest("testBasicWebServiceImplementation"));
        suite.addTest(new DCSFServiceTest("testNotifyEventAPI"));
        return (suite);
    }
    
    /**
     * Returns the test suite for the heartbeat manager
     * 
     * @return the test suite for the heartbeat manager
     */
    protected static Test getHeartbeatMgrTestSuite() {
        TestSuite suite = new TestSuite("Heartbeat Manager");
        suite.addTest(new HeartbeatMgrTest("testInitialization"));
        suite.addTest(new HeartbeatMgrTest("testHeartbeatCycle"));
        return (suite);
    }
}