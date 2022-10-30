/*
 * Created on Jan 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This is the test class for the remote listener registration manager
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/RemoteListenerRegistrationMgrTest.java#1 $
 */

public class RemoteListenerRegistrationMgrTest extends TestCase {

    /**
     * Constructor
     */
    public RemoteListenerRegistrationMgrTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public RemoteListenerRegistrationMgrTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the component checks for valid configuration
     */
    public void testConfigurationValidation() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        ComponentInfo sharedCtxLocatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(sharedCtxLocatorInfo);

        //try with a good configuration
        HashMapConfiguration remoteListenerRegMgrConfig = new HashMapConfiguration();
        remoteListenerRegMgrConfig.setProperty(IRemoteListenerRegistrationMgr.PARENT_DCSF_COMPONENT_CONFIG_PARAM, new MockDCSFComponentImpl());
        ComponentInfo remoteListenerRegMgrCompInfo = new ComponentInfo("GoodRemoteListener", RemoteListenerRegistrationMgrImpl.class.getName(), IRemoteListenerRegistrationMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, remoteListenerRegMgrConfig);
        boolean exceptionThrown = false;
        try {
            IRemoteListenerRegistrationMgr remoteListenerRegMgr = (IRemoteListenerRegistrationMgr) compMgr.getComponent(remoteListenerRegMgrCompInfo);
        } catch (RuntimeException ex) {
            exceptionThrown = true;
        }
        assertFalse("Remote listener registration manager should be instantiated properly", exceptionThrown);

        //try with a bad configuration (missing DCSF component)
        remoteListenerRegMgrConfig = new HashMapConfiguration();
        final String className = RemoteListenerRegistrationMgrImpl.class.getName();
        remoteListenerRegMgrCompInfo = new ComponentInfo("BadRemoteListener", className, IRemoteListenerRegistrationMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, remoteListenerRegMgrConfig);
        exceptionThrown = false;
        try {
            IRemoteListenerRegistrationMgr remoteListenerRegMgr = (IRemoteListenerRegistrationMgr) compMgr.getComponent(remoteListenerRegMgrCompInfo);
        } catch (RuntimeException ex) {
            exceptionThrown = true;
        }
        assertTrue("Remote listener registration manager should refused configuration without DCSF component", exceptionThrown);
    }

    /**
     * This test verifies that duplicate listeners are handled properly
     */
    public void testIdenticalListeners() {
        String event1Name = "name1";
        URL callback1 = null;
        try {
            callback1 = new URL("http://callback1.com");
        } catch (MalformedURLException e) {
            fail("Error when creating URL");
        }
        String event2Name = "name2";
        URL callback2 = null;
        try {
            callback2 = new URL("http://callback2.com");
        } catch (MalformedURLException e) {
            fail("Error when creating URL");
        }

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo sharedCtxLocatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(sharedCtxLocatorInfo);
        HashMapConfiguration remoteListenerRegMgrConfig = new HashMapConfiguration();
        remoteListenerRegMgrConfig.setProperty(IRemoteListenerRegistrationMgr.PARENT_DCSF_COMPONENT_CONFIG_PARAM, new MockDCSFComponentImpl());
        ComponentInfo remoteListenerRegMgrCompInfo = new ComponentInfo(IRemoteListenerRegistrationMgr.COMP_NAME, MockRemoteListenerRegistrationMgr.class.getName(), IRemoteListenerRegistrationMgr.class.getName(), LifestyleType.TRANSIENT_TYPE,
                remoteListenerRegMgrConfig);
        MockRemoteListenerRegistrationMgr component = (MockRemoteListenerRegistrationMgr) compMgr.getComponent(remoteListenerRegMgrCompInfo);
        IDestinyEventListener listener1 = component.getListener(event1Name, callback1);
        IDestinyEventListener listener2 = component.getListener(event2Name, callback2);
        IDestinyEventListener sameListener1 = component.getListener(event1Name, callback1);
        assertEquals("Listeners from the same callback and same events are the same", sameListener1, listener1);
        assertNotSame("Listeners from different callback or events are not the same", listener2, listener1);
    }

    /**
     * This test verifies that the correct APIs are called when remote listeners
     * are registered or unregistered.
     *  
     */
    public void testComponentAPIs() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo sharedCtxLocatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(sharedCtxLocatorInfo);
        HashMapConfiguration remoteListenerRegMgrConfig = new HashMapConfiguration();
        remoteListenerRegMgrConfig.setProperty(IRemoteListenerRegistrationMgr.PARENT_DCSF_COMPONENT_CONFIG_PARAM, new MockDCSFComponentImpl());
        ComponentInfo remoteListenerRegMgrCompInfo = new ComponentInfo(IRemoteListenerRegistrationMgr.COMP_NAME, MockRemoteListenerRegistrationMgr.class.getName(), IRemoteListenerRegistrationMgr.class.getName(), LifestyleType.TRANSIENT_TYPE,
                remoteListenerRegMgrConfig);
        boolean exceptionThrown = false;
        MockRemoteListenerRegistrationMgr remoteListenerRegMgr = (MockRemoteListenerRegistrationMgr) compMgr.getComponent(remoteListenerRegMgrCompInfo);

        final String event1Name = "name1";
        URL callback1 = null;
        try {
            callback1 = new URL("http://callback1.com");
        } catch (MalformedURLException e) {
            fail("Error when creating URL");
        }
        final String event2Name = "name2";
        URL callback2 = null;
        try {
            callback2 = new URL("http://callback2.com");
        } catch (MalformedURLException e) {
            fail("Error when creating URL");
        }
        MockSharedEventManager eventManager = remoteListenerRegMgr.getMockEventManager();

        //Verify the registerRemoteListener API
        remoteListenerRegMgr.registerRemoteListener(event1Name, callback1);
        assertEquals(event1Name, eventManager.getLastEventName());
        IDestinyEventListener listener1 = remoteListenerRegMgr.getListener(event1Name, callback1);
        assertEquals(listener1, eventManager.getLastListener());
        assertFalse(eventManager.isLastLocal());
        eventManager.reset();

        //Verify the unRegisterRemoteListener API
        remoteListenerRegMgr.unregisterRemoteListener(event1Name, callback1);
        assertEquals(event1Name, eventManager.getLastEventName());
        IDestinyEventListener samelistener1 = remoteListenerRegMgr.getListener(event1Name, callback1);
        assertEquals(samelistener1, eventManager.getLastListener());
        assertEquals(samelistener1, listener1);
        assertFalse(eventManager.isLastLocal());
        eventManager.reset();
    }
}