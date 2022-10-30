/*
 * Created on Feb 2, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.bindings.log.v2;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.nextlabs.destiny.bindings.log.v2.LogServiceTest;

/**
 * This is the test suite for DABS log service v2
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/nextlabs/destiny/bindings/log/v2/DABSLogServiceTestSuite.java#1 $
 */

public class DABSLogServiceTestSuite {

    /**
     * Returns the test suite to be run
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("DABS Component Log Service v2");
        suite.addTest(new TestSuite(LogServiceTest.class, "DABS Log Service v2"));
        return suite;
    }
}
