/*
 * Created on Jan 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.engine.destiny;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/engine/destiny/EvaluationEngineSuite.java#1 $:
 */

public class EvaluationEngineSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(EvaluationEngineSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.pf.engine.destiny");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestEvaluationEngine.class);
        suite.addTestSuite(ClientInformationManagerTests.class);
        //$JUnit-END$
        return suite;
    }
}
