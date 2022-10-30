/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.framework.datastore.hibernate.utils.TestMassDMLUtils;
import com.bluejungle.framework.locking.TestReaderWriterLock;
import com.bluejungle.framework.utils.TestMailHelper;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/test/com/bluejungle/framework/TestServerFrameworkSuite.java#1 $
 */

public class TestServerFrameworkSuite {

    /**
     * Main
     * 
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestServerFrameworkSuite.suite());
    }

    /**
     * Suite of server framework tests
     * 
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for the server framework");
        suite.addTestSuite(TestReaderWriterLock.class);
        suite.addTestSuite(TestMailHelper.class);
        suite.addTestSuite(TestMassDMLUtils.class);
        return suite;
    }
}