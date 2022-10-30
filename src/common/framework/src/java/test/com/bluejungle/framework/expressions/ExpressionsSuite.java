/*
 * Created on Feb 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.expressions;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/expressions/ExpressionsSuite.java#1 $:
 */

public class ExpressionsSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.framework.expressions.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(BooleanOpTest.class);
        suite.addTestSuite(EvalValueTest.class);
        suite.addTestSuite(RelationTest.class);
        //$JUnit-END$
        return suite;
    }
}
