/*
 * Created on Aug 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the test suite for the stored results
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredResultsTestSuite.java#1 $
 */

public class StoredResultsTestSuite {

     /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Stored results");
        suite.addTest(new TestSuite(StoredResultsTest.class, "Stored results execution"));
        suite.addTest(new TestSuite(StoredQueryMgrTest.class, "Stored query manager"));
        return suite;
    }
}