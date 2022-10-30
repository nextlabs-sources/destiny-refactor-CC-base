/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.context;

import java.util.List;

import junit.framework.TestCase;

import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.events.impl.DestinyEventManagerLocalImpl;
import com.bluejungle.destiny.server.shared.events.impl.DestinyEventManagerRemoteImpl;
import com.bluejungle.destiny.server.shared.exceptions.FactoryInitException;
import com.bluejungle.destiny.server.shared.internal.IInternalEventManager;
import com.bluejungle.destiny.server.shared.internal.IInternalSharedContext;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.registration.impl.DestinyRegistrationManagerImpl;

/**
 * This test exercises the factory functions (object instantiation)
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/context/tests/SharedContextTest.java#1 $:
 */

public class SharedContextTest extends TestCase {

    private final String eventMgrClassName = DestinyEventManagerLocalImpl.class.getName();
    private final String eventMgrRemoteClassName = DestinyEventManagerRemoteImpl.class.getName();
    private final String regMgrClassName = DestinyRegistrationManagerImpl.class.getName();
    private final String mockRegMgrClassName = MockRegistrationMgr.class.getName();

    /**
     * Constructor
     */
    public SharedContextTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param name
     *            test name
     */
    public SharedContextTest(String name) {
        super(name);
    }

    /**
     * This test checks that the correct classes gets instantiated.
     */
    public void testSharedModulesInstantiation() {

        IDestinySharedContext goodSharedContext = createValidSharedContext();

        //Check that we got the right objects
        IDestinyEventManager eventMgr = goodSharedContext.getEventManager();
        assertEquals(this.eventMgrClassName, eventMgr.getClass().getName());

        IDestinyRegistrationManager regMgr = goodSharedContext.getRegistrationManager();
        assertEquals(this.regMgrClassName, regMgr.getClass().getName());
    }

    /**
     * This test verifies that that the shared context objects implement the
     * correct interfaces
     */
    public void testInterfaceImplementation() {
        IDestinySharedContext goodSharedContext = createValidSharedContext();
        assertTrue("Shared Context implements IDestinySharedContext", goodSharedContext instanceof IDestinySharedContext);
        assertTrue("Shared Context implements IInternalSharedContext", goodSharedContext instanceof IInternalSharedContext);

        IDestinyEventManager eventMgr = goodSharedContext.getEventManager();
        assertTrue("Event Manager implements IDestinyEventManager", eventMgr instanceof IDestinyEventManager);
        assertTrue("Event Manager implements IInternalEventManager", eventMgr instanceof IInternalEventManager);
    }

    /**
     * This test verifies that proper exceptions are thrown when bad class names
     * are given to the Destiny shared context
     */
    public void testInstantiationExceptions() {

        DestinySharedContextImpl badSharedContext = new DestinySharedContextImpl();
        boolean exceptionThrown = false;

        try {
            badSharedContext.init(this.eventMgrClassName, "baaad Name for a class");
        } catch (FactoryInitException e) {
            exceptionThrown = true;
        }
        //The factory should be unhappy
        assertTrue(exceptionThrown);
    }

    /**
     * This test verifies that the factory does not double init components
     *  
     */
    public void testNoDoubleInits() {
        IInternalSharedContext initializedSharedContext = (IInternalSharedContext) createValidSharedContext();

        //Create a bad class name and try to init again
        boolean exceptionThrown = false;
        try {
            //Since it is already initialized, it should work fine
            initializedSharedContext.init("bad.class.name", this.regMgrClassName);
        } catch (FactoryInitException e) {
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);
    }

    /**
     * This test ensures that Destiny components are properly registered with
     * DCSF registration event
     */
    public void testDestinyDCSFRegistrationSubscription() {
        IDestinySharedContext sharedContext = createSharedContext(this.eventMgrClassName, this.mockRegMgrClassName);

        //With the local implementation, nobody should be listening to DCSF
        // registration
        MockRegistrationMgr mockRegMgr = (MockRegistrationMgr) sharedContext.getRegistrationManager();
        List listeners = mockRegMgr.getDcsfRegistrationListeners();
        assertNotNull(listeners);
        assertEquals(listeners.size(), 0);

        //With the remote implementation, the event manager should be listening
        // to DCSF registration
        sharedContext = createSharedContext(this.eventMgrRemoteClassName, this.mockRegMgrClassName);
        mockRegMgr = (MockRegistrationMgr) sharedContext.getRegistrationManager();
        listeners = mockRegMgr.getDcsfRegistrationListeners();
        assertNotNull(listeners);
        assertEquals(listeners.size(), 1);
        assertEquals(listeners.get(0), sharedContext.getEventManager());
    }

    /**
     * Returns a shared context instance based on init params
     * 
     * @param eventMgr
     *            event manager class name
     * @param logMgr
     *            log manager class name
     * @param regMgr
     *            registration manager class name
     * @return shared context instance
     */
    private IDestinySharedContext createSharedContext(String eventMgr, String regMgr) {
        DestinySharedContextImpl goodSharedContext = new DestinySharedContextImpl();
        boolean exceptionThrown = false;
        try {
            goodSharedContext.init(eventMgr, regMgr);
        } catch (FactoryInitException e) {
            exceptionThrown = true;
        }

        //Everything should work fine
        assertFalse(exceptionThrown);
        return (goodSharedContext);

    }

    /**
     * Returns a valid shared context, already initialized
     * 
     * @return a valid shared context instance
     */
    private IDestinySharedContext createValidSharedContext() {
        return (createSharedContext(this.eventMgrClassName, this.regMgrClassName));
    }

    /**
     * Returns a valid shared context instance for distributed installation
     * 
     * @return a valid shared context instance for distributed installation
     */
    public IDestinySharedContext createValidSharedContextForRemote() {
        return (createSharedContext(this.eventMgrRemoteClassName, this.regMgrClassName));
    }
}