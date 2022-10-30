/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/HQLBuilderTestSuite.java#1 $
 */

public class HQLBuilderTestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("HQL Builder");
        suite.addTest(new TestSuite(QueryElementTest.class, "HQL Builder - Query Element"));
        suite.addTest(new TestSuite(QueryTest.class, "HQL Builder - Query"));
        return suite;
    }
}