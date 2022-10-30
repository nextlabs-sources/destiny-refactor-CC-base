/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.Calendar;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/HeartbeatRecorderImplTest.java#1 $
 */

public class HeartbeatRecorderImplTest extends BaseContainerSharedTestCase {

    private IHeartbeatRecorder heartbeatRecorderToTest;

    private static final Long TEST_HEARTBEAT_RECORD_PROCESS_INTERVAL = new Long(1);
    private static final Long TEST_HEARTBEAT_RECORD_EXPECTED_CLEANUP_TIME = new Long(5000); // Every 5 seconds

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HeartbeatRecorderImplTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration recorderConfig = new HashMapConfiguration();
        recorderConfig.setProperty(HeartbeatRecorderImpl.RECORD_PROCESS_TIME_INTERVAL_PROPERTY_NAME, TEST_HEARTBEAT_RECORD_PROCESS_INTERVAL);
        recorderConfig.setProperty(HeartbeatRecorderImpl.RECORD_CLEANUP_TIME_INTERVAL_PROPERTY_NAME, TEST_HEARTBEAT_RECORD_EXPECTED_CLEANUP_TIME);
        recorderConfig.setProperty(HeartbeatRecorderImpl.RECORD_EXPIRATION_TIME_PROPERTY_NAME, TEST_HEARTBEAT_RECORD_EXPECTED_CLEANUP_TIME);
        this.heartbeatRecorderToTest = compMgr.getComponent(HeartbeatRecorderImpl.COMP_INFO);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {        
        // Attempt to clean all heartbeat records
        try {
            HeartbeatRecorderTestingUtils.deleteAllHeartbeats();
        } catch (HibernateException exception) {
            // This may occur because our record cleanup time is so small.  Swallow it
        }
        
        super.tearDown();
    }
        
    /**
     * Constructor for HeartbeatRecorderImplTest.
     * 
     * @param testname
     */
    public HeartbeatRecorderImplTest(String testname) {
        super(testname);
    }

    public void testRecordHeartbeat() throws PersistenceException, InterruptedException {
        Calendar beginTime = Calendar.getInstance();
        beginTime.add(Calendar.DATE, -1);
        
        long numHeartbeatsInLastDay = this.heartbeatRecorderToTest.getNumHeartbeatsSinceTime(beginTime);
        assertEquals("Ensure heart beats are initially", 0, numHeartbeatsInLastDay);

        // Now, record heartbeat
        this.heartbeatRecorderToTest.recordHeartbeat(new TestAgentHeartbeatData());

        Thread.sleep(TEST_HEARTBEAT_RECORD_PROCESS_INTERVAL.longValue() + 2000);

        numHeartbeatsInLastDay = this.heartbeatRecorderToTest.getNumHeartbeatsSinceTime(beginTime);
        assertEquals("Ensure heart beats are now 1", 1, numHeartbeatsInLastDay);
    }

    public void testGetNumHeartbeatsInLastDay() throws PersistenceException, InterruptedException {
        Calendar beginTime = Calendar.getInstance();
        beginTime.add(Calendar.DATE, -1);
        
        long numHeartbeatsInLastDay = this.heartbeatRecorderToTest.getNumHeartbeatsSinceTime(beginTime);
        assertEquals("Ensure heart beats are initially", 0, numHeartbeatsInLastDay);

        // Now, record heartbeat
        this.heartbeatRecorderToTest.recordHeartbeat(new TestAgentHeartbeatData());

        Thread.sleep(TEST_HEARTBEAT_RECORD_PROCESS_INTERVAL.longValue() + 2000);

        numHeartbeatsInLastDay = this.heartbeatRecorderToTest.getNumHeartbeatsSinceTime(beginTime);
        assertEquals("Ensure heart beats are now 1", 1, numHeartbeatsInLastDay);
    }

    public void testNonMethodHeatbeatRecordDeletion() throws PersistenceException, InterruptedException {
       // First, make sure there are no heartbeat records in table
        Calendar beginningOfTime = Calendar.getInstance();
        beginningOfTime.setTimeInMillis(0);
        long currentRowCount = this.heartbeatRecorderToTest.getNumHeartbeatsSinceTime(beginningOfTime);
        assertEquals("Ensure no heartbeat records in table", 0, currentRowCount);
        
        // now, add a few row
        this.heartbeatRecorderToTest.recordHeartbeat(new TestAgentHeartbeatData());
        this.heartbeatRecorderToTest.recordHeartbeat(new TestAgentHeartbeatData());
        this.heartbeatRecorderToTest.recordHeartbeat(new TestAgentHeartbeatData());
        
        Thread.sleep(TEST_HEARTBEAT_RECORD_PROCESS_INTERVAL.longValue() + 2000);
        
        // Make sure it is equals to 3
        currentRowCount = this.heartbeatRecorderToTest.getNumHeartbeatsSinceTime(beginningOfTime);
        assertEquals("Ensure 3 heartbeat records in table", 3, currentRowCount);
        
        // Now, sleep for 2*expected cleanup time (and some change) 
        Thread.sleep(2*TEST_HEARTBEAT_RECORD_EXPECTED_CLEANUP_TIME.longValue() + 3000);
        
        // Make sure it is now 0 again
        currentRowCount = this.heartbeatRecorderToTest.getNumHeartbeatsSinceTime(beginningOfTime);
        assertEquals("Ensure no heartbeat records in table after cleanup", 0, currentRowCount);
        
    }
    
    private class TestAgentHeartbeatData implements IAgentHeartbeatData {

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData#getPolicyAssemblyStatus()
         */
        public IAgentPolicyAssemblyStatusData getPolicyAssemblyStatus() {
            return null;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData#getProfileStatus()
         */
        public IAgentProfileStatusData getProfileStatus() {
            return null;
        }

    }
}
