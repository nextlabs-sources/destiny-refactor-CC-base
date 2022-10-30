/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import junit.framework.TestCase;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/events/tests/LocalEventManagerTest.java#1 $:
 */

public class LocalEventManagerTest extends TestCase {

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public LocalEventManagerTest(String testName) {
        super(testName);
    }

    /**
     * Utility function to sleep so that event processing can take place.
     * 
     * @param timeMs
     *            time to sleep in milliseconds
     */
    private synchronized void sleep(long timeMs) {
        try {
            this.wait(timeMs);
        } catch (InterruptedException e) {
            fail();
        }
    }

    /**
     * This test verifies that an event can be registered and fired properly
     * based on its name
     */
    public void testLocalEventRegistration() {

        IDestinyEventManager eventManager = new DestinyEventManagerLocalImpl();
        MockEventListener listener1 = new MockEventListener();
        MockEventListener listener2 = new MockEventListener();
        final IDCCServerEvent event1 = new DCCServerEventImpl("event1");
        final IDCCServerEvent event2 = new DCCServerEventImpl("event2");
        final IDCCServerEvent event3 = new DCCServerEventImpl("event3");
        eventManager.registerForEvent("event1", listener1);
        eventManager.registerForEvent("event1-1", listener1);
        eventManager.registerForEvent("event2", listener2);

        assertFalse("Listener 1 should not have received any event", listener1.isEventReceived());
        assertFalse("Listener 2 should not have received any event", listener2.isEventReceived());

        eventManager.fireEvent(event1);
        sleep(1500);
        assertTrue("Listener 1 should have received an event", listener1.isEventReceived());
        assertFalse("Listener 2 should not have received any event", listener2.isEventReceived());
        listener1.reset();

        eventManager.fireEvent(event2);
        sleep(1500);
        assertFalse("Listener 1 should not have received any event", listener1.isEventReceived());
        assertTrue("Listener 2 should have received any event", listener2.isEventReceived());
        listener2.reset();

        eventManager.fireEvent(event3);
        sleep(1500);
        assertFalse("Listener 1 should not have received any event", listener1.isEventReceived());
        assertFalse("Listener 2 should not have received any event", listener2.isEventReceived());
    }

    /**
     * This test verifies that after a listener no longer listens to an event,
     * the event is no longer fired to that listener.
     */
    public void testLocalEventUnRegistration() {
        IDestinyEventManager eventManager = new DestinyEventManagerLocalImpl();
        MockEventListener listener = new MockEventListener();
        MockEventListener listener2 = new MockEventListener();
        final IDCCServerEvent event1 = new DCCServerEventImpl("event1");
        final IDCCServerEvent event2 = new DCCServerEventImpl("event2");
        eventManager.registerForEvent("event1", listener);
        eventManager.registerForEvent("event1", listener2);
        assertFalse(listener.isEventReceived());
        assertFalse(listener2.isEventReceived());

        eventManager.fireEvent(event1);
        sleep(1500);
        assertTrue(listener.isEventReceived());
        assertTrue(listener2.isEventReceived());
        listener.reset();
        listener2.reset();

        eventManager.registerForEvent("event1", listener);
        eventManager.registerForEvent("event2", listener);
        eventManager.fireEvent(event1);
        eventManager.fireEvent(event2);
        sleep(1500);
        assertTrue(listener.isEventReceived());
        assertEquals(2, listener.getEventCount());
        eventManager.unregisterForEvent("event2", listener);
        listener.reset();
        eventManager.fireEvent(event1);
        eventManager.fireEvent(event2);
        sleep(1500);
        assertTrue(listener.isEventReceived());
        assertEquals(1, listener.getEventCount());

        assertTrue(listener2.isEventReceived());
        assertEquals(2, listener2.getEventCount());
        listener.reset();
        listener2.reset();
    }

    /**
     * This test verifies that when the event processing thread is busy handling
     * one event, other events are properly queue up and processed.
     */
    public void testMultipleConcurrentEvents() {
        IDestinyEventManager eventManager = new DestinyEventManagerLocalImpl();
        final SlowListener slowListener = new SlowListener();
        final MockEventListener listener1 = new MockEventListener();
        final MockEventListener listener2 = new MockEventListener();
        final MockEventListener listener3 = new MockEventListener();

        final IDCCServerEvent event1 = new DCCServerEventImpl("event1");
        final IDCCServerEvent event2 = new DCCServerEventImpl("event2");
        final IDCCServerEvent event3 = new DCCServerEventImpl("event3");

        eventManager.registerForEvent("event1", slowListener);
        eventManager.registerForEvent("event2", listener2);
        eventManager.registerForEvent("event2", listener3);
        eventManager.registerForEvent("event3", listener3);

        final int nbEvents = 10;
        eventManager.fireEvent(event1);
        for (int i = 0; i < nbEvents; i++) {
            eventManager.fireEvent(event2);
            eventManager.fireEvent(event3);
        }
        sleep(slowListener.getSleepTime() + 1000);
        assertTrue(slowListener.isEventReceived());
        assertEquals(1, slowListener.getEventCount());
        assertTrue(listener2.isEventReceived());
        assertEquals(nbEvents, listener2.getEventCount());
        assertTrue(listener3.isEventReceived());
        assertEquals(2 * nbEvents, listener3.getEventCount());
    }

    /**
     * This test verifies that a given listener cannot register more than once
     * for the same event.
     */
    public void testNoDoubleRegistration() {
        IDestinyEventManager eventManager = new DestinyEventManagerLocalImpl();
        MockEventListener listener = new MockEventListener();
        MockEventListener listener2 = new MockEventListener();
        final IDCCServerEvent event1 = new DCCServerEventImpl("event1");
        final IDCCServerEvent event2 = new DCCServerEventImpl("event2");
        eventManager.registerForEvent("event1", listener);
        eventManager.registerForEvent("event1", listener);
        eventManager.registerForEvent("event1", listener2);
        assertFalse(listener.isEventReceived());
        assertFalse(listener2.isEventReceived());

        eventManager.fireEvent(event1);
        sleep(1500);
        assertTrue(listener.isEventReceived());
        assertEquals(1, listener.getEventCount());
        assertTrue(listener2.isEventReceived());
        assertEquals(1, listener2.getEventCount());
        listener.reset();
        listener2.reset();

        eventManager.registerForEvent("event2", listener);
        eventManager.registerForEvent("event2", listener);
        eventManager.fireEvent(event1);
        eventManager.fireEvent(event2);
        sleep(1500);
        assertTrue(listener.isEventReceived());
        assertEquals(2, listener.getEventCount());
        assertEquals(event1, listener.getEventList().get(0));
        assertEquals(event2, listener.getEventList().get(1));
    }

    /**
     * This slow listener class takes 10 seconds to process. It will hold the
     * event processing thread long enough so that other events have to be
     * queued up.
     * 
     * @author ihanen
     */
    protected class SlowListener extends MockEventListener implements IDestinyEventListener {

        private final long sleepTime = 10000;

        /**
         * Returns the time that the slow listener should sleep.
         * 
         * @return the time that the slow listener should sleep.
         */
        public long getSleepTime() {
            return this.sleepTime;
        }

        /**
         * 
         * @see com.bluejungle.destiny.server.shared.events.IDestinyEventListener#onDestinyEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
         */
        public void onDestinyEvent(IDCCServerEvent event) {
            super.onDestinyEvent(event);
            sleep(getSleepTime());
        }
    }
}