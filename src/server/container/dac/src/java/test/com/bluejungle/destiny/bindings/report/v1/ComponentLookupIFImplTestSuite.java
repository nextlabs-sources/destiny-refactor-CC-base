/*
 * Created on Nov 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the component lookup web service implementation test class
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ComponentLookupIFImplTestSuite.java#1 $
 */

public class ComponentLookupIFImplTestSuite extends TestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Component Lookup Interface");
        suite.addTest(new TestSuite(ComponentLookupHostTest.class, "Host API"));
        suite.addTest(new TestSuite(ComponentLookupPolicyTest.class, "Policy API"));
        suite.addTest(new TestSuite(ComponentLookupUserTest.class, "User API"));
        suite.addTest(new TestSuite(ComponentLookupUserClassTest.class, "User class API"));
        return suite;
    }
}
