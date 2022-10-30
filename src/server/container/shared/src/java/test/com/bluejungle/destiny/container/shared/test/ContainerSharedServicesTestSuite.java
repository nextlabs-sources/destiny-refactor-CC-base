/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.container.shared.securesession.service.SecureSessionServiceTest;


/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/test/ContainerSharedServicesTestSuite.java#1 $
 */

public class ContainerSharedServicesTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ContainerSharedServicesTestSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test Container Shared with Services");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(SecureSessionServiceTest.class, "Secure Session Service"));        

        //$JUnit-END$
        return suite;
    }
}
