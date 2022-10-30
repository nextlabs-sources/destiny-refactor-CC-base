/*
 * Created on Jan 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.container.dms.components.compmgr.DCCRegistrationBrokerTest;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.LicenseAuditorTestSuite;
import com.bluejungle.destiny.container.dms.components.sharedfolder.defaultimpl.SharedFolderSourceTest;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.ConfigurationTestSuite;

/**
 * Test suite for standalone DMS component tests
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class StandaloneDMSComponentTests {

    /**
     * Test suite for standalone DMS component tests
     * 
     * @return the DMS test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("DMS Components");
        suite.addTest(new TestSuite(DCCRegistrationBrokerTest.class, "Component and event manager facade"));
        suite.addTest(ConfigurationTestSuite.suite());
        suite.addTest(LicenseAuditorTestSuite.suite());
        suite.addTest(new TestSuite(SharedFolderSourceTest.class, "Shared folder interface"));
        return suite;
    }
}
