/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.impl.DCCServerEventImpl;
import com.bluejungle.destiny.services.dcsf.types.DestinyEvent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.ITask;
import com.bluejungle.framework.threading.IThreadPool;

/**
 * This is the test class for the remote event dispatch manager (running within
 * the DCSF web application)
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/RemoteEventDispatchMgrTest.java#1 $:
 */

public class RemoteEventDispatchMgrTest extends TestCase {

    /**
     * Constructor
     *  
     */
    public RemoteEventDispatchMgrTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test to run
     */
    public RemoteEventDispatchMgrTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the remote event dispatch manager is initialized
     * properly
     */
    public void testRemoteEventDispatchMgrInit() {
        IComponentManager compManager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration eventDispatchMgrConfig = new HashMapConfiguration();
        eventDispatchMgrConfig.setProperty(IThreadPool.WORKER_CLASS_NAME, MockRemoteEventDispatchWorkerImpl.class.getName());
        ComponentInfo evtDispatchMgrCompInfo = new ComponentInfo("TestRemoteEventDispatchMgr", RemoteEventDispatchMgrImpl.class.getName(), IRemoteEventDispatchMgr.class.getName(), LifestyleType.SINGLETON_TYPE, eventDispatchMgrConfig);
        IRemoteEventDispatchMgr eventDispatchMgr = (IRemoteEventDispatchMgr) compManager.getComponent(evtDispatchMgrCompInfo);
        assertNotNull(eventDispatchMgr);

        Integer workerCount = MockRemoteEventDispatchWorkerImpl.getInitCount();
        //Thread pool size is set to 10 and does not change
        assertEquals("Thread pool size set to 3", new Integer(3), workerCount);
        MockRemoteEventDispatchWorkerImpl.setInitCount(new Integer(0));
    }

    /**
     * This test verifies that the event firing is working fine when the caller
     * calls the fireEvent API.
     */
    public void testRemoteEventDispatchMgrEventFiring() {
        IComponentManager compManager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration eventDispatchMgrConfig = new HashMapConfiguration();
        eventDispatchMgrConfig.setProperty(IThreadPool.WORKER_CLASS_NAME, MockRemoteEventDispatchWorkerImpl.class.getName());
        ComponentInfo evtDispatchMgrCompInfo = new ComponentInfo("TestRemoteEventDispatchMgr", RemoteEventDispatchMgrImpl.class.getName(), IRemoteEventDispatchMgr.class.getName(), LifestyleType.SINGLETON_TYPE, eventDispatchMgrConfig);
        IRemoteEventDispatchMgr eventDispatchMgr = (IRemoteEventDispatchMgr) compManager.getComponent(evtDispatchMgrCompInfo);
        assertNotNull(eventDispatchMgr);
        IDCCServerEvent firedEvent = new DCCServerEventImpl("testEvent");
        URL location = null;
        try {
            location = new URL("http://location1");
        } catch (MalformedURLException ex) {
            fail("Error when creating URL");
        }
        eventDispatchMgr.fireEvent(firedEvent, location);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            assertTrue(false);
        }

        MockDCSFServiceStub stub = MockDCSFServiceLocator.getServiceStub();
        assertNotNull(stub);
        List list = MockDCSFServiceStub.getEventNotificationList();
        assertNotNull(list);
        assertEquals(1, list.size());
        DestinyEvent event = (DestinyEvent) list.get(0);
        assertEquals(firedEvent.getName(), event.getEventName());
        assertEquals("Target DCSF service Address is set properly", location.toString(), MockDCSFServiceLocator.getAddress());
        list.clear();
    }

    /**
     * This test verifies if the remote event dispatch worker threads calls the
     * remote web service properly.
     */
    public void testEventDispatchWorkerThread() {
        IComponentManager compManager = ComponentManagerFactory.getComponentManager();

        //Setup an event dispatch worker object
        ComponentInfo info = new ComponentInfo("eventDispatch", MockRemoteEventDispatchWorkerImpl.class.getName(), IRemoteEventDispatchWorker.class.getName(), LifestyleType.TRANSIENT_TYPE);
        IRemoteEventDispatchWorker evtWorker = (IRemoteEventDispatchWorker) compManager.getComponent(info);
        assertNotNull(evtWorker);

        //Now, pass a few event notifications and check that they got sent
        // properly
        IDCCServerEvent event1 = new DCCServerEventImpl("Event1");
        URL location1 = null;
        try {
            location1 = new URL("http://location1");
        } catch (MalformedURLException ex) {
            fail("Error when creating URL");
        }

        IDCCServerEvent event2 = new DCCServerEventImpl("Event2");
        URL location2 = null;
        try {
            location2 = new URL("http://location2");
        } catch (MalformedURLException ex) {
            fail("Error when creating URL");
        }

        RemoteEventDispatchRequest request1 = new RemoteEventDispatchRequest(event1, location1);
        RemoteEventDispatchRequest request2 = new RemoteEventDispatchRequest(event2, location2);

        //Perform the event dispatch
        evtWorker.doWork(request1);
        evtWorker.doWork(request2);

        //Now, let's see what happened
        MockDCSFServiceStub stub = MockDCSFServiceLocator.getServiceStub();
        assertNotNull(stub);
        List list = MockDCSFServiceStub.getEventNotificationList();
        assertNotNull(list);
        assertEquals(2, list.size());
        DestinyEvent event = (DestinyEvent) list.get(0);
        assertEquals(event1.getName(), event.getEventName());
        event = (DestinyEvent) list.get(1);
        assertEquals(event2.getName(), event.getEventName());
        assertEquals("Target DCSF service Address is set properly", location2.toString(), MockDCSFServiceLocator.getAddress());
        list.clear();
    }

    /**
     * This test verifies that the event dispatch thread does not accept random
     * object types
     *  
     */
    public void testEventDispatchWorkerThreadArguments() {
        IComponentManager compManager = ComponentManagerFactory.getComponentManager();
        ComponentInfo info = new ComponentInfo("eventDispatch", MockRemoteEventDispatchWorkerImpl.class.getName(), IRemoteEventDispatchWorker.class.getName(), LifestyleType.TRANSIENT_TYPE);
        IRemoteEventDispatchWorker evtWorker = (IRemoteEventDispatchWorker) compManager.getComponent(info);
        assertNotNull(evtWorker);
        boolean exceptionCaught = false;
        try {
            evtWorker.doWork(null);
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
        exceptionCaught = false;

        try {
            evtWorker.doWork(new DummyTask());
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
        exceptionCaught = false;
    }

    private class DummyTask implements ITask {
    }
}