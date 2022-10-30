package com.bluejungle.framework.comp;

import junit.framework.Test;
import junit.framework.TestSuite;

// Copyright Blue Jungle, Inc.

/*
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/comp/ComponentManagerSuite.java#1 $
 */

public class ComponentManagerSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for com.bluejungle.framework.comp.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(ComponentManagerImplTest.class);
        //$JUnit-END$
        return suite;
    }
}
