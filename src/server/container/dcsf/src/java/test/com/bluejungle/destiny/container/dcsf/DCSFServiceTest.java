/*
 * Created on Dec 10, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import javax.xml.rpc.server.ServiceLifecycle;

import junit.framework.TestCase;

import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.dcsf.DCSFServiceIF;
import com.bluejungle.destiny.services.dcsf.types.DestinyEvent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * This is the test class for the DCSF Web Service
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/DCSFServiceTest.java#1 $:
 */

public class DCSFServiceTest extends TestCase {

    /**
     * Constructor
     */
    public DCSFServiceTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public DCSFServiceTest(String testName) {
        super(testName);
    }

    /**
     * This test performs a few basic verification of the web service class
     */
    public void testBasicWebServiceImplementation() {
        MockDCSFServiceImpl service = new MockDCSFServiceImpl();
        assertTrue("Service implements web service interface", service instanceof DCSFServiceIF);
        assertTrue("Service implements web service lifecycle", service instanceof ServiceLifecycle);

        //checks whether the web service checks if the component is ready
        DestinyEvent event = new DestinyEvent();
        boolean exceptionThrown = false;
        try {
            service.notifyEvent(event);
        } catch (ServiceNotReadyFault e) {
            exceptionThrown = true;
        }
        assertTrue("DCC Component ready check did happen", exceptionThrown);
        exceptionThrown = false;
    }

    /**
     * This test verifies that the correct API is called on the shared event
     * manager, with the right arguments
     */
    public void testNotifyEventAPI() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo compInfo = new ComponentInfo("JUNIT", MockDCSFComponentImpl.class.getName(), LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(compInfo);

        MockDCSFServiceImpl service = new MockDCSFServiceImpl();
        MockSharedEventManager eventManager = service.getMockEventManager();
        eventManager.reset();

        //Now, let's check whether an event notification calls the right API
        final DestinyEvent event = new DestinyEvent();
        event.setEventName("fooEvent");
        try {
            service.notifyEvent(event);
        } catch (ServiceNotReadyFault e1) {
            assertFalse(true);
        }
        assertEquals(event.getEventName(), eventManager.getLastEvent().getName());
        assertFalse(eventManager.isLastLocal());
        eventManager.reset();
    }
}
