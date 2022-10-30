/*
 * Created on Dec 1, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */

package com.bluejungle.destiny.agent.controlmanager.test;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.controlmanager.ControlMngr;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.controlmanager.SCMEvent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author hfriedland
 */
public class ControlMngrTest extends TestCase {

    private ControlMngr controlMngr = null;

    /**
     * @param name
     *            name of test method
     */
    public ControlMngrTest(String name) {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ComponentManagerFactory.getComponentManager().getComponent(TestEvaluationEngine.class);
        ComponentInfo<ControlMngr> info = new ComponentInfo<ControlMngr>(
        		IControlManager.NAME, 
        		ControlMngr.class, 
        		IControlManager.class,
        		LifestyleType.SINGLETON_TYPE);
        this.controlMngr = ComponentManagerFactory.getComponentManager().getComponent(info);
        this.controlMngr.init();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        this.controlMngr = null;
    }

    public void testControlMngr() {
        assertNotNull("controlMngr is null", this.controlMngr);
        this.controlMngr.init();
        this.controlMngr.uninit();
    }

    public void testControlMngrEvent() {
        assertNotNull("controlMngr is null", this.controlMngr);
        this.controlMngr.init();
        this.controlMngr.handleSCMEvent(new SCMEvent(SCMEvent.SERVICE_STOPPED));
    }

}