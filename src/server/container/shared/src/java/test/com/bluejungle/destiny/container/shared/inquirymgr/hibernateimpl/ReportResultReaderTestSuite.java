/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the test suite for the report result reader classes
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportResultReaderTestSuite.java#1 $
 */

public class ReportResultReaderTestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Result Readers");
        suite.addTest(new TestSuite(ReportResultReaderStatefulImplTest.class, "Result reader - Stateful"));
        suite.addTest(new TestSuite(ReportResultReaderStatelessImplTest.class, "Result reader - Stateless"));
        suite.addTest(new TestSuite(ReportResultReaderEmptyImplTest.class, "Result reader - Count Only"));
        return suite;
    }
}