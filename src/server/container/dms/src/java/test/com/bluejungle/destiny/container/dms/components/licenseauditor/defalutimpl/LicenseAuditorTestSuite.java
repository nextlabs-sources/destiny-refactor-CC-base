/*
 * Created on May 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All tests for license auditor
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/test/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/LicenseAuditorTestSuite.java#1 $
 */

public class LicenseAuditorTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LicenseAuditorTestSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(LicenseAuditorImplTest.class);
        //$JUnit-END$
        return suite;
    }
}