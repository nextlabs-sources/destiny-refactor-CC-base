/*
 * Created on Mar 15, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.destiny.parser;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/destiny/parser/ParserSuite.java#1 $:
 */

public class ParserSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.pf.destiny.parser");
        //$JUnit-BEGIN$
        suite.addTest(TestTreeWalker.suite());
        suite.addTest(TestDomainObjectBuilder.suite());
        suite.addTest(TestParser.suite());
        //$JUnit-END$
        return suite;
    }
}
