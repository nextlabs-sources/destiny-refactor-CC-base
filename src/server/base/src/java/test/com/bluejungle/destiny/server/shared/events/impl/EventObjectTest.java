/*
 * Created on Feb 26, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import junit.framework.TestCase;

/**
 * This is the test class for the event object
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/test/com/bluejungle/destiny/server/shared/events/impl/EventObjectTest.java#1 $
 */

public class EventObjectTest extends TestCase {

    /**
     * Constructor
     *  
     */
    public EventObjectTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public EventObjectTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the constructor behaves properly
     */
    public void testClassConstructor() {

        //Try correct constructor
        final String eventName = "abcd";
        DCCServerEventImpl event = new DCCServerEventImpl(eventName);
        assertEquals("Event name should be correct", eventName, event.getName());
        assertNotNull("No properties should be available by default", event.getProperties());
        assertEquals("No properties should be available by default", 0, event.getProperties().size());

        //Try incorrect constructor
        boolean exThrown = false;
        try {
            DCCServerEventImpl badevent = new DCCServerEventImpl(null);
            fail("The constructor should not accept null argument");
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue("The constructor should throw the correct exception", exThrown);

        exThrown = false;
        try {
            DCCServerEventImpl badevent = new DCCServerEventImpl("");
            fail("The constructor should not accept empty name");
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue("The constructor should throw the correct exception", exThrown);
    }

    /**
     * This test verifies the event properties
     */
    public void testClassProperties() {
        final String eventName = "abcd";
        DCCServerEventImpl event = new DCCServerEventImpl(eventName);
        event.getProperties().setProperty("prop1", "value1");
        event.getProperties().setProperty("prop2", "value2");
        assertEquals("The event should have the right number of properties", 2, event.getProperties().size());
        assertEquals("The event should have the right properties", "value1", event.getProperties().getProperty("prop1"));
        assertEquals("The event should have the right properties", "value2", event.getProperties().getProperty("prop2"));
        assertEquals("The event should have the right properties", "test", event.getProperties().getProperty("prop3", "test"));
    }
}
