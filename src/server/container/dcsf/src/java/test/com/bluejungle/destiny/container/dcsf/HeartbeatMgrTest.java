/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.bluejungle.destiny.container.dcc.HeartbeatMgrImpl;
import com.bluejungle.destiny.container.dcc.IHeartbeatMgr;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This is the test class for the heartbeat manager
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/HeartbeatMgrTest.java#1 $:
 */

public class HeartbeatMgrTest extends TestCase {

    private static final String DUMMY_ID = "dummyId";

    /**
     * Constructor*
     */
    public HeartbeatMgrTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public HeartbeatMgrTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the initialization of the component is done
     * properly
     */
    public void testInitialization() {
        setupLocator();
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration heartbeatConfig = new HashMapConfiguration();
        //Try to initialize the component without a component ID
        Long iRate = new Long(10);
        heartbeatConfig.setProperty(IHeartbeatMgr.HEARTBEAT_RATE_CONFIG_PARAM, iRate);
        ComponentInfo heartBeatCompInfo = new ComponentInfo(IHeartbeatMgr.COMP_NAME, HeartbeatMgrImpl.class.getName(), IHeartbeatMgr.class.getName(), LifestyleType.SINGLETON_TYPE, heartbeatConfig);
        boolean exceptionThrown = false;
        try {
            IHeartbeatMgr heartBeatMgr = (IHeartbeatMgr) manager.getComponent(heartBeatCompInfo);
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }
        assertTrue("Heartbeat manager requires a component Id.", exceptionThrown);
        exceptionThrown = false;

        //Gives a component ID (no component type) and retry:
        heartbeatConfig.setProperty(IHeartbeatMgr.COMPONENT_ID_CONFIG_PARAM, DUMMY_ID);
        try {
            IHeartbeatMgr heartBeatMgr = (IHeartbeatMgr) manager.getComponent(heartBeatCompInfo);
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }
        assertTrue("Heartbeat manager created properly", exceptionThrown);
        exceptionThrown = false;

        //Gives a component ID and component Type and retry
        heartbeatConfig.setProperty(IHeartbeatMgr.COMPONENT_ID_CONFIG_PARAM, DUMMY_ID);
        heartbeatConfig.setProperty(IHeartbeatMgr.COMPONENT_TYPE_CONFIG_PARAM, ServerComponentType.DABS);
        try {
            IHeartbeatMgr heartBeatMgr = (IHeartbeatMgr) manager.getComponent(heartBeatCompInfo);
        } finally {            
            manager.shutdown();
        }
    }

    /**
     * This test verifies that the hearbeat is sent regularly according to the
     * heartbeat rate.
     */
    public void testHeartbeatCycle() {
        setupLocator();

        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration heartbeatConfig = new HashMapConfiguration();
        Long iRate = new Long(1);
        heartbeatConfig.setProperty(IHeartbeatMgr.HEARTBEAT_RATE_CONFIG_PARAM, iRate);
        heartbeatConfig.setProperty(IHeartbeatMgr.COMPONENT_ID_CONFIG_PARAM, DUMMY_ID);
        heartbeatConfig.setProperty(IHeartbeatMgr.COMPONENT_TYPE_CONFIG_PARAM, ServerComponentType.DABS);
        ComponentInfo heartBeatCompInfo = new ComponentInfo(IHeartbeatMgr.COMP_NAME, HeartbeatMgrImpl.class.getName(), IHeartbeatMgr.class.getName(), LifestyleType.SINGLETON_TYPE, heartbeatConfig);
        IHeartbeatMgr heartBeatMgr = (IHeartbeatMgr) manager.getComponent(heartBeatCompInfo);

        synchronized (this) {
            try {
                this.wait(6000);
            } catch (InterruptedException e1) {
                assertTrue("Test waited for 6000 ms", false);
            }
        }
        //This should stop the hearbeats
        heartBeatMgr.dispose();
        MockSharedContextLocator locator = (MockSharedContextLocator) manager.getComponent(IDestinySharedContextLocator.COMP_NAME);
        MockRegistrationManager regMgr = (MockRegistrationManager) locator.getSharedContext().getRegistrationManager();
        List requests = regMgr.getHeartbeatRequests();
        assertNotNull(requests);

        //typically, we should have enough time to fit 4 or 5 heartbeats
        assertTrue("Hearbeat manager sleeps the right amount between two heartbeats", requests.size() >= 4);
        Iterator it = requests.iterator();
        while (it.hasNext()) {
            IComponentHeartbeatInfo info = (IComponentHeartbeatInfo) it.next();
            assertEquals("HeartBeat contains the correct component Id", DUMMY_ID, info.getComponentName());
        }

        manager.shutdown();
    }

    /**
     * This function sets up the destiny shared context locator
     */
    private void setupLocator() {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        ComponentInfo info = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) manager.getComponent(info);
    }
}