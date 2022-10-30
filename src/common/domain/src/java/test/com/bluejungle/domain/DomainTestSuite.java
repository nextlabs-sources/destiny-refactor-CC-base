/*
 * Created on <unknown>
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain;

import com.bluejungle.domain.log.PolicyActivityInfoTest;
import com.bluejungle.domain.log.PolicyActivityLogEntryTest;
import com.bluejungle.domain.log.ResourceInformationTest;
import com.bluejungle.domain.log.TrackingLogEntryTest;
import com.nextlabs.domain.action.ActionEnumTypeTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/test/com/bluejungle/domain/DomainTestSuite.java#1 $
 */

public class DomainTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DomainTestSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.domain");
        //$JUnit-BEGIN$
        suite.addTestSuite(ResourceInformationTest.class);
        suite.addTestSuite(PolicyActivityInfoTest.class);
        suite.addTestSuite(PolicyActivityLogEntryTest.class);
        suite.addTestSuite(TrackingLogEntryTest.class);
        suite.addTestSuite(ActionEnumTypeTest.class);
        //$JUnit-END$
        return suite;
    }

}
