/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This test suite verifies the remote event manager implementation
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_112/main/src/server/base/com/bluejungle/destiny/server/shared/events/tests/RemoteEventManagerTestSuite.java#1 $:
 */

public class RemoteEventManagerTestSuite {

    /**
     * Returns the test suite for the remote event manager
     * 
     * @return the test suite for the remote event manager
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Remote event manager");
        suite.addTest(new TestSuite(RemoteEventManagerTest.class));
        return (suite);
    }

}