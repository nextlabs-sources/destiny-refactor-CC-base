/*
 * Created on Feb 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.QueryEngineTestSuite;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportAndInquiryMgrTestSuite;
import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl.ResourceCacheTestSuite;

/**
 * This is the test suite for the inquiry center back end.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/InquiryCenterTestSuite.java#1 $
 */

public class InquiryCenterTestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Inquiry Center");
        suite.addTest(ResourceCacheTestSuite.suite());
        suite.addTest(ReportAndInquiryMgrTestSuite.suite());
        suite.addTest(QueryEngineTestSuite.suite());
        return suite;
    }

}