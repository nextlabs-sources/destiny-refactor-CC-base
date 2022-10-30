/*
 * Created on Feb 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.comp;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * @author Sasha Vladimirov
 * 
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/comp/test/ComponentManagerImplTest.java#6 $
 */

public class ComponentManagerImplTest extends BaseDestinyTestCase {

    private IComponentManager manager;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ComponentManagerImplTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        manager = ComponentManagerFactory.getComponentManager();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for ComponentManagerImplTest.
     * 
     * @param arg0
     */
    public ComponentManagerImplTest(String arg0) {
        super(arg0);
    }

    /*
     * Class under test for Object getComponent(Class)
     */
    public final void testGetComponentClass() {
        TestComponent tc = (TestComponent) manager.getComponent(TestComponent.class);

        // check log
        Log log = tc.getLog();
        assertNotNull(log);

        // check manager
        IComponentManager mgr = tc.getManager();
        assertSame(mgr, manager);

        // check initialization
        assertTrue(tc.isInited());
        // check starting
        assertTrue(tc.isStarted());

        // check singleton-ness
        TestComponent tc2 = (TestComponent) manager.getComponent(TestComponent.class);
        tc2 = (TestComponent) manager.getComponent(TestComponent.class);
        tc2 = (TestComponent) manager.getComponent(TestComponent.class);
        tc2 = (TestComponent) manager.getComponent(TestComponent.class);
        tc2 = (TestComponent) manager.getComponent(TestComponent.class);
        assertSame(tc, tc2);
        assertEquals(1, TestComponent.count);

        // check getting by name
        TestComponent tc3 = (TestComponent) manager.getComponent(TestComponent.NAME);
        assertSame(tc2, tc3);

        String value = tc3.getValue();
        assertEquals(value, TestComponent.TEST_PROP_VALUE);

        // check shutdown
        mgr.shutdown();
        assertTrue(tc.isDisposed());
        assertTrue(tc.isStopped());
    }

    public final void testShutdown() {
        IComponentManager mgr = ComponentManagerFactory.getComponentManager();
        mgr.shutdown();
        assertTrue("Component Manager should be shut down", mgr.isShutdown());
        boolean shut = false;
        try {
        mgr.getComponent(TestComponent.class);
        } catch (RuntimeException e) {
            shut = true;
        }
        assertTrue("Shut down manager should not return value", shut);

        IComponentManager mgr2 = ComponentManagerFactory.getComponentManager();
        assertNotSame(mgr, mgr2);
    }

    public final void testGetComponentName() {
        //TODO Implement getComponentName().
    }

    public final void testReleaseComponet() {
        //TODO Implement releaseComponet().
    }

}