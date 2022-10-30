// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * JUnit Test Suite to test IPC framework
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 *  
 */
public class IPCTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(IPCTestSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.agent.ipc.tests");
        //$JUnit-BEGIN$
        suite.addTest(new IPCOSWrapperTest("testOSWrapper"));
        suite.addTest(new IPCOSWrapperTest("testGetLoggedInUsers"));
        suite.addTest(new IPCRequestHandlerTest("testRequestHandler"));
        suite.addTest(new IPCRequestHandlerTest("testProxy"));

        //$JUnit-END$
        return suite;
    }
}