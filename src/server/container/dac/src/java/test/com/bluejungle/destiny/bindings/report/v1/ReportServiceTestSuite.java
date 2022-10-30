/*
 * Created on Apr 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the test suite for the report web service.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ReportServiceTestSuite.java#3 $
 */

public class ReportServiceTestSuite extends TestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Report Web Service");
        suite.addTest(ComponentLookupIFImplTestSuite.suite());
        suite.addTest(new TestSuite(ReportExecutionIFBindingImplTest.class, "Report Execution interface"));
        suite.addTest(new TestSuite(ReportLibraryIFBindingImplTest.class, "Report library interface"));
        return suite;
    }
}