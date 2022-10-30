/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.util.Set;

import junit.framework.TestCase;

import org.apache.axis.types.URI;

import com.bluejungle.destiny.container.dcsf.IDMSRegistrationMgr;
import com.bluejungle.destiny.container.dcsf.IRemoteEventRegistrationMgr;
import com.bluejungle.destiny.container.dcsf.RemoteEventRegistrationMgrImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;

/**
 * This is the test case for the remote event registration manager.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/RemoteEventRegistrationMgrTest.java#1 $:
 */

public class RemoteEventRegistrationMgrTest extends TestCase {

    private URI dmsLocation;
    private URI dcsfLocation;

    /**
     * Constructor
     *  
     */
    public RemoteEventRegistrationMgrTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public RemoteEventRegistrationMgrTest(String testName) {
        super(testName);
    }

    /**
     * sets up the test
     * 
     * @throws URI.MalformedURIException
     *             if the URI setup fails
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws URI.MalformedURIException {
        this.dcsfLocation = new URI("http://dcsfLoc/dcsf");
        this.dmsLocation = new URI("http://dmsLoc/dms");
    }

    /**
     * This test verifies that the initialization of the module is done properly
     */
    public void testRemoteEventRegistrationMgrInit() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration eventMgrconfig = new HashMapConfiguration();
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsLocation);
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, this.dcsfLocation);
        eventMgrconfig.setProperty(IThreadPool.WORKER_CLASS_NAME, MockRemoteEventRegistrationWorkerImpl.class.getName());
        ComponentInfo evtRegMgrCompInfo = new ComponentInfo(IRemoteEventRegistrationMgr.COMP_NAME, RemoteEventRegistrationMgrImpl.class.getName(), IRemoteEventRegistrationMgr.class.getName(), LifestyleType.SINGLETON_TYPE, eventMgrconfig);
        IRemoteEventRegistrationMgr eventRegistrationMgr = (IRemoteEventRegistrationMgr) compMgr.getComponent(evtRegMgrCompInfo);
        assertNotNull(eventRegistrationMgr);

        Integer nbCount = MockRemoteEventRegistrationWorkerImpl.getWorkerCount();
        assertEquals("Number of worker threads", new Integer(1), nbCount);
        MockRemoteEventRegistrationWorkerImpl.reset();
    }

    /**
     * This test verifes that the registration requests are performed properly,
     * and that the proper error handling is performed is the web service call
     * fails.
     */
    public void testEventRegistrationRequestHandling() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration eventMgrconfig = new HashMapConfiguration();
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsLocation);
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, this.dcsfLocation);
        eventMgrconfig.setProperty(IThreadPool.WORKER_CLASS_NAME, MockRemoteEventRegistrationWorkerImpl.class.getName());
        ComponentInfo evtRegMgrCompInfo = new ComponentInfo(IRemoteEventRegistrationMgr.COMP_NAME, RemoteEventRegistrationMgrImpl.class.getName(), IRemoteEventRegistrationMgr.class.getName(), LifestyleType.SINGLETON_TYPE, eventMgrconfig);
        IRemoteEventRegistrationMgr eventRegistrationMgr = (IRemoteEventRegistrationMgr) compMgr.getComponent(evtRegMgrCompInfo);
        assertNotNull(eventRegistrationMgr);

        MockComponentServiceStub.setSucceedEvents(new Boolean(false));
        eventRegistrationMgr.registerForRemoteEvent("event1");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            assertTrue("Test waited for 500 ms", false);
        }
        Set registerList = MockRemoteEventRegistrationWorkerImpl.getRegisterList();
        assertNotNull("The registration list is kept when failing", registerList);
        assertEquals("The registration list is kept when failing", 1, registerList.size());
        assertTrue("The event registration is not lost after failure", registerList.contains("event1"));

        //Now, make it pass
        MockComponentServiceStub.setSucceedEvents(new Boolean(true));
        eventRegistrationMgr.registerForRemoteEvent("event1");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            assertTrue("Test waited for 500 ms", false);
        }
        registerList = MockRemoteEventRegistrationWorkerImpl.getRegisterList();
        assertNotNull("The registration list is kept when failing", registerList);
        assertEquals("The event is taken off the list after successful registration", 0, registerList.size());
    }

    /**
     * This test verifes that the unregistration requests are performed
     * properly, and that the proper error handling is performed is the web
     * service call fails.
     */
    public void testEventUnregistrationRequestHandling() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration eventMgrconfig = new HashMapConfiguration();
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsLocation);
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, this.dcsfLocation);
        eventMgrconfig.setProperty(IThreadPool.WORKER_CLASS_NAME, MockRemoteEventRegistrationWorkerImpl.class.getName());
        ComponentInfo evtRegMgrCompInfo = new ComponentInfo(IRemoteEventRegistrationMgr.COMP_NAME, RemoteEventRegistrationMgrImpl.class.getName(), IRemoteEventRegistrationMgr.class.getName(), LifestyleType.SINGLETON_TYPE, eventMgrconfig);
        IRemoteEventRegistrationMgr eventRegistrationMgr = (IRemoteEventRegistrationMgr) compMgr.getComponent(evtRegMgrCompInfo);
        assertNotNull(eventRegistrationMgr);

        MockComponentServiceStub.setSucceedEvents(new Boolean(true));
        eventRegistrationMgr.registerForRemoteEvent("event1");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            assertTrue("Test waited for 500 ms", false);
        }

        MockRemoteEventRegistrationWorkerImpl.reset();
        MockComponentServiceStub.setSucceedEvents(new Boolean(false));
        eventRegistrationMgr.unregisterForRemoteEvent("event1");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            assertTrue("Test waited for 500 ms", false);
        }

        Set unregisterList = MockRemoteEventRegistrationWorkerImpl.getUnregisterList();
        assertNotNull("The unregistration list is kept when failing", unregisterList);
        assertEquals("The unregistration list is kept when failing", 1, unregisterList.size());
        assertTrue("The event unregistration is not lost after failure", unregisterList.contains("event1"));

        //Now, make it pass
        MockComponentServiceStub.setSucceedEvents(new Boolean(true));
        eventRegistrationMgr.unregisterForRemoteEvent("event1");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            assertTrue("Test waited for 500 ms", false);
        }
        unregisterList = MockRemoteEventRegistrationWorkerImpl.getRegisterList();
        assertNotNull("The registration list is kept when failing", unregisterList);
        assertEquals("The event is taken off the list after successful registration", 0, unregisterList.size());
        MockRemoteEventRegistrationWorkerImpl.reset();
    }

    /**
     * This test verifies that if an event failed to be registered and if the
     * same event is unregistered, the request never makes it the remote DMS.
     *  
     */
    public void testEventCancellation() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration eventMgrconfig = new HashMapConfiguration();
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsLocation);
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, this.dcsfLocation);
        eventMgrconfig.setProperty(IThreadPool.WORKER_CLASS_NAME, MockRemoteEventRegistrationWorkerImpl.class.getName());
        ComponentInfo evtRegMgrCompInfo = new ComponentInfo(IRemoteEventRegistrationMgr.COMP_NAME, RemoteEventRegistrationMgrImpl.class.getName(), IRemoteEventRegistrationMgr.class.getName(), LifestyleType.SINGLETON_TYPE, eventMgrconfig);
        IRemoteEventRegistrationMgr eventRegistrationMgr = (IRemoteEventRegistrationMgr) compMgr.getComponent(evtRegMgrCompInfo);
        assertNotNull(eventRegistrationMgr);

        MockComponentServiceStub.setSucceedEvents(new Boolean(false));
        eventRegistrationMgr.registerForRemoteEvent("event1");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            assertTrue("Test waited for 500 ms", false);
        }

        MockRemoteEventRegistrationWorkerImpl.reset();

        eventRegistrationMgr.unregisterForRemoteEvent("event1");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            assertTrue("Test waited for 500 ms", false);
        }

        Set registerList = MockRemoteEventRegistrationWorkerImpl.getRegisterList();
        assertNotNull(registerList);
        assertEquals("Event registration has been cancelled", 0, registerList.size());
        MockRemoteEventRegistrationWorkerImpl.reset();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() {
        // Shutdown the component manager:
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        compMgr.shutdown();
    }
}