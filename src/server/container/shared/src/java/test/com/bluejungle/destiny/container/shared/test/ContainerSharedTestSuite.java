/*
 * Created on Feb 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDOTest;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManagerTest;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.HeartbeatRecorderImplTest;
import com.bluejungle.destiny.container.shared.applicationusers.ApplicationUserComponentTestSuite;
import com.bluejungle.destiny.container.shared.pf.TestPolicyEditorService;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateActivityJournalingSettingsManagerTest;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManagerTest;
import com.bluejungle.destiny.container.shared.securesession.hibernate.SecureSessionTestSuite;
import com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.StoredResultsTestSuite;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/test/ContainerSharedTestSuite.java#1 $
 */

public class ContainerSharedTestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for the DCC shared modules");
        suite.addTest(new TestSuite(HibernateProfileManagerTest.class, "Hibernate Profile Manager"));
        suite.addTest(new TestSuite(HibernateActivityJournalingSettingsManagerTest.class, "Hibernate Activity Journaling Settings Manager"));
        suite.addTest(new TestSuite(HeartbeatRecorderImplTest.class, "Heartbeat Recorder Test"));
        suite.addTest(StoredResultsTestSuite.suite());
        suite.addTest(new TestSuite(AgentManagerTest.class, "Agent Manager"));
        suite.addTest(new TestSuite(AgentDOTest.class, "Agent DO"));
        suite.addTest(SecureSessionTestSuite.suite());
        suite.addTest(ApplicationUserComponentTestSuite.suite());
        suite.addTestSuite(TestPolicyEditorService.class);
        return suite;
    }
}
