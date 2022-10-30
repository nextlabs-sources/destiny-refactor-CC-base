/*
 * Created on Nov 15, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.security;

import com.bluejungle.destiny.server.security.secureConnector.SecurePasswordHttp11ProtocolTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/security/src/java/test/com/bluejungle/destiny/server/security/ServerSecurityTestSuite.java#1 $
 */

public class ServerSecurityTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.server.security");
        //$JUnit-BEGIN$
        suite.addTestSuite(SecurePasswordHttp11ProtocolTest.class);
        suite.addTestSuite(APIAuthCheckerTest.class);
        suite.addTestSuite(CertificateCheckerTest.class);
        //$JUnit-END$
        return suite;
    }

}
