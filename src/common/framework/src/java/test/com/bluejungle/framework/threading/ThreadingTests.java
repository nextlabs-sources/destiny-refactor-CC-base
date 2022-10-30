/*
 * Created on Dec 1, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.framework.threading;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/threading/ThreadingTests.java#1 $:
 */

public class ThreadingTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.framework.threading.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(ThreadPoolTest.class);
        //$JUnit-END$
        return suite;
    }
}
