/*
 * Created on Jan 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.resource;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/resource/ResourceDOSuite.java#1 $:
 */

public class ResourceDOSuite {
     
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.pf.domain.destiny.resource");
        suite.addTestSuite(TestResourceSpecNameCondLocal.class);
        // this test is very slow, put it back in only if necessary
        // suite.addTestSuite(TestResourceSpecNameCondRemote.class);        
        suite.addTestSuite(TestResourceSpecDateCond.class);
        suite.addTestSuite(TestResourceSpecGroup.class);
        return suite;
    }
}