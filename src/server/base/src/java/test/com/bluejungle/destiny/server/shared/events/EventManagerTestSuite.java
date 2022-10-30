/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.server.shared.events.impl.EventObjectTest;
import com.bluejungle.destiny.server.shared.events.impl.LocalEventManagerTestSuite;
import com.bluejungle.destiny.server.shared.events.impl.RemoteEventManagerTestSuite;

/**
 * This is a suite of test to test the event manager in the shared context
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/events/tests/EventManagerTestSuite.java#1 $:
 */

public class EventManagerTestSuite {

    /**
     * Returns the test suite for the event manager
     * 
     * @return the test suite for the event manager
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Shared event manager");
        TestSuite eventSuite = new TestSuite("Event object");
        eventSuite.addTestSuite(EventObjectTest.class);
        suite.addTest(eventSuite);
        suite.addTest(LocalEventManagerTestSuite.suite());
        suite.addTest(RemoteEventManagerTestSuite.suite());
        return (suite);
    }
}