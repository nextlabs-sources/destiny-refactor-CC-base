/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.auth;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the test suite for the authentication manager
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/auth/AuthMgrTestSuite.java#1 $
 */

public class AuthMgrTestSuite {

    /**
     * Returns the testSuite for the callback handler.
     * 
     * @return the testSuite for the callback handler.
     */
    public static Test getCallBackHandlerTestSuite() {
        TestSuite suite = new TestSuite("Callback handler test");
        suite.addTest(new CallBackHandlerTest("testClass"));
        suite.addTest(new CallBackHandlerTest("testArgumentsValidation"));
        suite.addTest(new CallBackHandlerTest("testCallbackSetup"));
        return suite;
    }

    /**
     * Returns the testSuite for the Kerberos authentication manager.
     * 
     * @return the testSuite for the Kerberos authentication manager
     */
    public static Test getKerberosAuthTestSuite() {
        TestSuite suite = new TestSuite("Kerberos authentication manager");
        suite.addTest(new KrbAuthMgrTest("testClass"));
        suite.addTest(new KrbAuthMgrTest("testConfiguration"));
        suite.addTest(new KrbAuthMgrTest("testAuthentication"));
        return suite;
    }

    /**
     * Returns the set of tests to be run in the test suite
     * 
     * @return the set of tests to be run in the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Authentication Manager Test");
        suite.addTest(getCallBackHandlerTestSuite());
        suite.addTest(getKerberosAuthTestSuite());
        return suite;
    }
}