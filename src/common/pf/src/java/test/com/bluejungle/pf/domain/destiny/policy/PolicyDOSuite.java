/*
 * Created on Dec 27, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.policy;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/policy/PolicyDOSuite.java#1 $:
 */

public class PolicyDOSuite  {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for com.bluejungle.pf.domain.destiny.policy");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestPolicy.class);
        //$JUnit-END$
        return suite;
    }    

}
