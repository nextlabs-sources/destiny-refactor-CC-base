/*
 * Created on Apr 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.hibernateimpl.HostMgrTest;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.hibernateimpl.PolicyMgrTest;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.memoryimpl.ResourceClassMgrTest;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.hibernateimpl.UserMgrTest;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.hibernateimpl.UserReportMgrImplTest;

/**
 * This is the DAC component test suite. This test suite tests the various DAC
 * components.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/container/dac/DACComponentsTestSuite.java#1 $
 */

public class DACComponentsTestSuite extends TestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("DAC Components");
        suite.addTest(new TestSuite(PolicyMgrTest.class, "Policy Manager component"));
        suite.addTest(new TestSuite(HostMgrTest.class, "Host Manager component"));
        suite.addTest(new TestSuite(ResourceClassMgrTest.class, "Resource Class Manager component"));
        suite.addTest(new TestSuite(UserMgrTest.class, "User Manager component"));
        suite.addTest(new TestSuite(UserReportMgrImplTest.class, "User Report Manager component"));
        return suite;
    }
}