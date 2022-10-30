/*
 * Created on Feb 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.patterns;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/patterns/PatternsSuite.java#1 $:
 */

public class PatternsSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.framework.patterns.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(EnumBaseTest.class);        
        //$JUnit-END$
        return suite;
    }
}
