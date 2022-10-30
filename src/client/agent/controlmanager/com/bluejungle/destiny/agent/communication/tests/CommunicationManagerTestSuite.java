/*
 * Created on Dec 15, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.communication.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/communication/tests/CommunicationManagerTestSuite.java#1 $:
 */

public class CommunicationManagerTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.agent.communication.tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(CommunicationManagerTest.class);
        //$JUnit-END$
        return suite;
    }
}
