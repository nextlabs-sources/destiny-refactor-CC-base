/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.context.SharedContextTest;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.internal.IInternalEventManager;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.registration.MockDCSFComponent;
import com.bluejungle.destiny.server.shared.registration.MockRemoteEventFiredRequest;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/events/tests/RemoteEventManagerTest.java#1 $:
 */

public class RemoteEventManagerTest extends TestCase {

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public RemoteEventManagerTest(String testName) {
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
     * This test verifies that local event registrations that took place before
     * DCSF web application comes up are not lost.
     */
    public void testWaitForDCSFContainer() {

        SharedContextTest sharedContextTest = new SharedContextTest();
        IDestinySharedContext sharedContext = sharedContextTest.createValidSharedContextForRemote();

        IInternalEventManager eventManager = (IInternalEventManager) sharedContext.getEventManager();
        IDestinyRegistrationManager regManager = sharedContext.getRegistrationManager();

        MockDCSFComponent dcsfContainer = new MockDCSFComponent();
        MockEventListener listener1 = new MockEventListener();
        MockEventListener listener2 = new MockEventListener();
        MockEventListener listener3 = new MockEventListener();

        //register for a few events before DCSF web-app is up
        eventManager.registerForEvent("event1", listener1, true);
        eventManager.registerForEvent("event2", listener1, true);
        eventManager.registerForEvent("event2", listener2, true);

        assertNotNull(dcsfContainer.getEventRegistrationRequests());
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 0);

        //Now (at last!) register DCSF container
        regManager.registerComponent(dcsfContainer);
        //Make sure the events have not been lost
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 2);
        List eventNameList = dcsfContainer.getEventRegistrationRequests();
        assertEquals("event1", eventNameList.get(0));
        assertEquals("event2", eventNameList.get(1));
        dcsfContainer.reset();

        //Unregister DCSF container, register a new event, and register the
        // DCSF container again
        regManager.unregisterComponent(dcsfContainer);
        eventManager.registerForEvent("event3", listener3, true);
        regManager.registerComponent(dcsfContainer);
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 1);
        eventNameList = dcsfContainer.getEventRegistrationRequests();
        assertEquals("event3", eventNameList.get(0));
        dcsfContainer.reset();

        //Unregisters events while DCSF container is away. Make sure DCSF gets
        //notified when it comes back
        //Reset environment
        sharedContextTest = new SharedContextTest();
        sharedContext = sharedContextTest.createValidSharedContextForRemote();
        eventManager = (IInternalEventManager) sharedContext.getEventManager();
        regManager = sharedContext.getRegistrationManager();
        regManager.registerComponent(dcsfContainer);

        eventManager.registerForEvent("event1", listener1, true);
        eventManager.registerForEvent("event2", listener2, true);
        regManager.unregisterComponent(dcsfContainer);
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 2);
        eventManager.unregisterForEvent("event2", listener2, true);
        regManager.registerComponent(dcsfContainer);
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 2);
        assertEquals(dcsfContainer.getEventUnRegistrationRequests().size(), 1);
        dcsfContainer.reset();
    }

    /**
     * This test verifies that a local event registration is forwarded to the
     * DCSF application the first time only. Subsequent registrations for the
     * same event name remain local and should not involve DCSF.
     */
    public void testFirstLocalListenerRegistration() {
        //Register for new events once DCSF registration occured
        //This event was never registered, so it should be forwarded to DCSF
        SharedContextTest sharedContextTest = new SharedContextTest();
        IDestinySharedContext sharedContext = sharedContextTest.createValidSharedContextForRemote();

        IInternalEventManager eventManager = (IInternalEventManager) sharedContext.getEventManager();
        IDestinyRegistrationManager regManager = sharedContext.getRegistrationManager();
        MockDCSFComponent dcsfContainer = new MockDCSFComponent();
        MockEventListener listener1 = new MockEventListener();
        MockEventListener listener2 = new MockEventListener();
        regManager.registerComponent(dcsfContainer);

        //Register events
        eventManager.registerForEvent("event1", listener1, true);
        eventManager.registerForEvent("event2", listener1, true);
        eventManager.registerForEvent("event2", listener2, true);

        assertNotNull(dcsfContainer.getEventRegistrationRequests());
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 2);
        List eventNameList = dcsfContainer.getEventRegistrationRequests();
        assertEquals("event1", eventNameList.get(0));
        assertEquals("event2", eventNameList.get(1));
        dcsfContainer.reset();
    }

    /**
     * This test verifies that when the last local listener unregisters for an
     * event, the event manager tells DCSF to stop forwarding event
     * notifications to this JVM instance.
     */
    public void testLastLocalListenerUnregistration() {
        SharedContextTest sharedContextTest = new SharedContextTest();
        IDestinySharedContext sharedContext = sharedContextTest.createValidSharedContextForRemote();

        IInternalEventManager eventManager = (IInternalEventManager) sharedContext.getEventManager();
        IDestinyRegistrationManager regManager = sharedContext.getRegistrationManager();
        MockDCSFComponent dcsfContainer = new MockDCSFComponent();
        MockEventListener listener = new MockEventListener();
        regManager.registerComponent(dcsfContainer);

        //Register events
        eventManager.registerForEvent("event1", listener, true);
        eventManager.registerForEvent("event2", listener, true);
        //Now unregister for some event
        eventManager.unregisterForEvent("event1", listener, true);
        assertNotNull(dcsfContainer.getEventUnRegistrationRequests());
        assertEquals(dcsfContainer.getEventUnRegistrationRequests().size(), 1);
        List eventNameList = dcsfContainer.getEventUnRegistrationRequests();
        assertEquals("event1", eventNameList.get(0));
        dcsfContainer.reset();
        eventManager.unregisterForEvent("event2", listener, true);
        assertNotNull(dcsfContainer.getEventUnRegistrationRequests());
        assertEquals(dcsfContainer.getEventUnRegistrationRequests().size(), 1);
        eventNameList = dcsfContainer.getEventUnRegistrationRequests();
        assertEquals("event2", eventNameList.get(0));
        dcsfContainer.reset();
    }

    /**
     * This test verifies that if an event is registered in the queue and the
     * same event gets queued for unregistration, then DCSF is not notified at
     * all when it comes up.
     */
    public void testRegistrationCancellation() {
        SharedContextTest sharedContextTest = new SharedContextTest();
        IDestinySharedContext sharedContext = sharedContextTest.createValidSharedContextForRemote();
        IInternalEventManager eventManager = (IInternalEventManager) sharedContext.getEventManager();
        IDestinyRegistrationManager regManager = sharedContext.getRegistrationManager();
        MockDCSFComponent dcsfContainer = new MockDCSFComponent();
        MockEventListener listener = new MockEventListener();
        MockEventListener listener2 = new MockEventListener();

        //Two events registered, but one gets unregistered by the same listener
        eventManager.registerForEvent("event1", listener, true);
        eventManager.registerForEvent("event2", listener2, true);
        eventManager.unregisterForEvent("event2", listener2, true);
        regManager.registerComponent(dcsfContainer);
        assertNotNull(dcsfContainer.getEventRegistrationRequests());
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 1);
        List eventNameList = dcsfContainer.getEventRegistrationRequests();
        assertEquals("event1", eventNameList.get(0));
        assertEquals(dcsfContainer.getEventUnRegistrationRequests().size(), 0);
        dcsfContainer.reset();

        //Resets the setup
        sharedContextTest = new SharedContextTest();
        sharedContext = sharedContextTest.createValidSharedContextForRemote();
        eventManager = (IInternalEventManager) sharedContext.getEventManager();
        regManager = sharedContext.getRegistrationManager();
        dcsfContainer = new MockDCSFComponent();

        //1 event registered by two listeners, but 1 gets unregistered
        eventManager.registerForEvent("event1", listener, true);
        eventManager.registerForEvent("event1", listener2, true);
        eventManager.unregisterForEvent("event1", listener2, true);
        regManager.registerComponent(dcsfContainer);
        assertNotNull(dcsfContainer.getEventUnRegistrationRequests());
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 1);
        eventNameList = dcsfContainer.getEventRegistrationRequests();
        assertEquals("event1", eventNameList.get(0));
        assertEquals(dcsfContainer.getEventUnRegistrationRequests().size(), 0);
        dcsfContainer.reset();

        //Resets the setup
        sharedContextTest = new SharedContextTest();
        sharedContext = sharedContextTest.createValidSharedContextForRemote();
        eventManager = (IInternalEventManager) sharedContext.getEventManager();
        regManager = sharedContext.getRegistrationManager();
        dcsfContainer = new MockDCSFComponent();

        //1 event registered by two listeners that unregister immediately
        eventManager.registerForEvent("event1", listener, true);
        eventManager.registerForEvent("event1", listener2, true);
        eventManager.unregisterForEvent("event1", listener, true);
        eventManager.unregisterForEvent("event1", listener2, true);
        regManager.registerComponent(dcsfContainer);
        assertNotNull(dcsfContainer.getEventUnRegistrationRequests());
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 0);
        assertEquals(dcsfContainer.getEventUnRegistrationRequests().size(), 0);
        dcsfContainer.reset();

        //Resets the setup
        sharedContextTest = new SharedContextTest();
        sharedContext = sharedContextTest.createValidSharedContextForRemote();
        eventManager = (IInternalEventManager) sharedContext.getEventManager();
        regManager = sharedContext.getRegistrationManager();
        dcsfContainer = new MockDCSFComponent();

        //DCSF is here, 1 event registered by two listeners, DCSF goes away,
        // the events is unregistered, then registered again, DCSF comes back =>
        // nothing should happen
        regManager.registerComponent(dcsfContainer);
        eventManager.registerForEvent("event1", listener, true);
        eventManager.registerForEvent("event1", listener2, true);
        dcsfContainer.reset();
        regManager.unregisterComponent(dcsfContainer);
        eventManager.unregisterForEvent("event1", listener, true);
        eventManager.unregisterForEvent("event1", listener2, true);
        eventManager.registerForEvent("event1", listener, true);
        eventManager.registerForEvent("event1", listener2, true);
        regManager.registerComponent(dcsfContainer);
        assertEquals(dcsfContainer.getEventRegistrationRequests().size(), 0);
        assertEquals(dcsfContainer.getEventUnRegistrationRequests().size(), 0);
        dcsfContainer.reset();

    }

    /**
     * This test verifies that when an event is registered from a remote
     * instance, the DCSF container gets notified properly through the remote
     * event dispatch when that event fires locally.
     */
    public void testRemoteEventDispatcher() {

        SharedContextTest sharedContextTest = new SharedContextTest();
        IDestinySharedContext sharedContext = sharedContextTest.createValidSharedContextForRemote();
        IInternalEventManager eventManager = (IInternalEventManager) sharedContext.getEventManager();
        IDestinyRegistrationManager regManager = sharedContext.getRegistrationManager();
        MockDCSFComponent dcsfContainer = new MockDCSFComponent();

        URL remoteListenerURL = null;
        try {
            remoteListenerURL = new URL("http://mySpecialURI.com");
        } catch (MalformedURLException ex) {
            fail("Error when creating URL");
            return;
        }

        //Registers the event as remote
        IDestinyEventListener remoteListener = new DestinyRemoteEventDispatcher(remoteListenerURL, dcsfContainer);
        regManager.registerComponent(dcsfContainer);
        eventManager.registerForEvent("event1", remoteListener, false);
        IDCCServerEvent event = new DCCServerEventImpl("event1");
        //Fires that event locally and make sure the DCSF container gets it
        eventManager.fireEvent(event, true);
        sleep(1000);
        List events = dcsfContainer.getRemoteEventsFired();
        assertEquals(events.size(), 1);
        MockRemoteEventFiredRequest request = (MockRemoteEventFiredRequest) events.get(0);
        assertEquals(request.getEvent(), event);
        assertEquals(request.getLocation(), remoteListenerURL);
        dcsfContainer.reset();
    }

    /**
     * This test verifies that when an event is listened remotely and locally,
     * the appropriate listeners (locally and remotely) get the right
     * notifications
     */
    public void testRemoteAndLocalListeners() {
        SharedContextTest sharedContextTest = new SharedContextTest();
        IDestinySharedContext sharedContext = sharedContextTest.createValidSharedContextForRemote();
        IInternalEventManager eventManager = (IInternalEventManager) sharedContext.getEventManager();
        IDestinyRegistrationManager regManager = sharedContext.getRegistrationManager();
        MockDCSFComponent dcsfContainer = new MockDCSFComponent();

        URL remoteListenerURL = null;
        try {
            remoteListenerURL = new URL("http://mySpecialURI.com");
        } catch (MalformedURLException ex) {
            fail("Error when creating URL");
            return;
        }

        //Set 2 local and 2 remote listener on two events
        IDestinyEventListener remoteListener1 = new DestinyRemoteEventDispatcher(remoteListenerURL, dcsfContainer);
        IDestinyEventListener remoteListener2 = new DestinyRemoteEventDispatcher(remoteListenerURL, dcsfContainer);
        MockEventListener localListener1 = new MockEventListener();
        MockEventListener localListener2 = new MockEventListener();
        regManager.registerComponent(dcsfContainer);
        eventManager.registerForEvent("event1", remoteListener1, false);
        eventManager.registerForEvent("event1", localListener1, true);
        eventManager.registerForEvent("event2", remoteListener2, false);
        eventManager.registerForEvent("event2", localListener2, true);
        final IDCCServerEvent event1 = new DCCServerEventImpl("event1");
        final IDCCServerEvent event2 = new DCCServerEventImpl("event2");

        //Fires event 1 and event 2 locally
        eventManager.fireEvent(event1, true);
        eventManager.fireEvent(event2, true);
        sleep(1000);
        List events = dcsfContainer.getRemoteEventsFired();
        assertEquals(events.size(), 2);
        MockRemoteEventFiredRequest request = (MockRemoteEventFiredRequest) events.get(0);
        assertEquals(request.getEvent(), event1);
        assertEquals(request.getLocation(), remoteListenerURL);
        request = (MockRemoteEventFiredRequest) events.get(1);
        assertEquals(request.getEvent(), event2);
        assertEquals(request.getLocation(), remoteListenerURL);
        assertTrue(localListener1.isEventReceived());
        assertEquals(localListener1.getEventCount(), 1);
        assertEquals(localListener1.getEventList().get(0), event1);
        assertTrue(localListener2.isEventReceived());
        assertEquals(localListener2.getEventCount(), 1);
        assertEquals(localListener2.getEventList().get(0), event2);
        localListener1.reset();
        localListener2.reset();
        dcsfContainer.reset();

        //Fires event from a remote instance
        eventManager.fireEvent(event1, false);
        sleep(1000);
        events = dcsfContainer.getRemoteEventsFired();
        assertEquals(events.size(), 0);
        assertTrue(localListener1.isEventReceived());
        assertEquals(localListener1.getEventCount(), 1);
        assertEquals(localListener1.getEventList().get(0), event1);
        localListener1.reset();
        dcsfContainer.reset();

        //Fires a non registered event from a remote instance
        final IDCCServerEvent unknownEvent = new DCCServerEventImpl("unknown");
        eventManager.fireEvent(unknownEvent, false);
        sleep(1000);
        events = dcsfContainer.getRemoteEventsFired();
        assertEquals(events.size(), 0);
        assertFalse(localListener1.isEventReceived());
        assertFalse(localListener2.isEventReceived());
        events = dcsfContainer.getRemoteEventsFired();
        assertEquals(events.size(), 0);
        localListener1.reset();
        dcsfContainer.reset();
    }
}
