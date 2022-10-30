/*
 * Created on Dec 15, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.scheduling.tests;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.commandengine.tests.TestCommandExecutor;
import com.bluejungle.destiny.agent.communication.tests.TestControlManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.scheduling.Scheduler;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/scheduling/tests/SchedulerTest.java#2 $:
 */

public class SchedulerTest extends TestCase {

    private TestControlManager controlManager = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        //create control manager instance
        ComponentInfo info = new ComponentInfo(IControlManager.NAME, TestControlManager.class.getName(), IControlManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.controlManager = (TestControlManager) ComponentManagerFactory.getComponentManager().getComponent(info);
    }

    /**
     * Constructor for SchedulerTest.
     * 
     * @param name
     */
    public SchedulerTest(String name) {
        super(name);
    }

//    public void testScheduler() throws InterruptedException {
//        TestCommandExecutor commandExecutor = (TestCommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
//        HashMapConfiguration config = new HashMapConfiguration();
//        config.setProperty(IControlManager.NAME, this.controlManager);
//        Scheduler scheduler = (Scheduler) ComponentManagerFactory.getComponentManager().getComponent(Scheduler.class, config);

//        //Wait for one second.
//        Thread.sleep(1000);

//        scheduler.stop();

//        assertTrue("Fewer sendHeartbeat calls than expected", commandExecutor.getHeartbeatCount() > 7);
//        assertTrue("More sendHeartbeat calls than expected", commandExecutor.getHeartbeatCount() < 11);
//        assertTrue("Fewer uploadLogs calls than expected", commandExecutor.getUploadLogsCount() > 15);
//        assertTrue("More uploadLogs calls than expected", commandExecutor.getUploadLogsCount() < 21);

//        commandExecutor.setHeartbeatCount(new Integer(0));
//        commandExecutor.setUploadLogsCount(new Integer(0));

//        this.controlManager.changeConfig();
//        scheduler.init();

//        //Wait for one second.
//        Thread.sleep(1000);

//        scheduler.stop();

//        assertTrue("Fewer sendHeartbeat calls than expected, expecting in (15 -- 21), actual: " + commandExecutor.getHeartbeatCount(), commandExecutor.getHeartbeatCount() >= 15);
//        assertTrue("More sendHeartbeat calls than expected, expected in (15 -- 21 ),  actual" + commandExecutor.getHeartbeatCount(), commandExecutor.getHeartbeatCount() < 21);
//        assertTrue("Fewer uploadLogs calls than expected, expecting in (7 -- 11), actual:" + commandExecutor.getUploadLogsCount(), commandExecutor.getUploadLogsCount() > 7);
//        assertTrue("More uploadLogs calls than expected, expecting in (7 -- 11), actual:" + commandExecutor.getUploadLogsCount(), commandExecutor.getUploadLogsCount() < 11);

 //   }
}
