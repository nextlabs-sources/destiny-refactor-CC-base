/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/securesession/hibernate/SecureSessionTestSuite.java#1 $
 */

public class SecureSessionTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SecureSessionTestSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.container.shared.securesession");
        //$JUnit-BEGIN$
        suite.addTestSuite(HibernateSecureSessionManagerTest.class);
        suite.addTestSuite(SecureSessionKeyKeeperTest.class);
        suite.addTestSuite(SecureSessionDOTest.class);
        suite.addTestSuite(SecureSessionImplTest.class);
        suite.addTestSuite(SecureSessionKeyTest.class);
        
        //$JUnit-END$
        return suite;
    }
}
