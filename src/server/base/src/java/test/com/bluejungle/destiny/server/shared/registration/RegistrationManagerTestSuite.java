/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import com.bluejungle.destiny.server.shared.registration.impl.RegistrationManagerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the test suite for the registration manager
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/registration/tests/RegistrationManagerTestSuite.java#1 $:
 */

public class RegistrationManagerTestSuite {

    /**
     * Returns the test suite for the registration manager
     * 
     * @return the test suite for the registration manager
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Registration manager");
        suite.addTestSuite(RegistrationManagerTest.class);
        return (suite);
    }

}