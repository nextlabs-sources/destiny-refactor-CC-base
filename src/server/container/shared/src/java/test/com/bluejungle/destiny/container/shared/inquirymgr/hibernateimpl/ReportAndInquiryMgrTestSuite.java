/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportAndInquiryMgrTestSuite.java#1 $
 */

public class ReportAndInquiryMgrTestSuite {

 
    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Report and Inquiry Mgr");
        suite.addTest(new TestSuite(InquiryManagerTest.class, "Inquiry Manager"));
        suite.addTest(new TestSuite(PersistentInquiryManagerTest.class, "Persistent Inquiry Manager"));
        suite.addTest(new TestSuite(ReportManagerTest.class, "Report Manager"));
        suite.addTest(new TestSuite(PersistentReportManagerTest.class, "Persistent Report Manager"));
        return suite;
    }
}