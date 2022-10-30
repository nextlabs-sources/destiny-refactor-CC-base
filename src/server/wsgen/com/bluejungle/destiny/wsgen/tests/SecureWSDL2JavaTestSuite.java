/*
 * Created on Dec 20, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.wsgen.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the test suite for the secure WSDLToJava generator
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/wsgen/com/bluejungle/destiny/wsgen/tests/SecureWSDL2JavaTestSuite.java#1 $:
 */

public class SecureWSDL2JavaTestSuite {

    /**
     * Main function
     * 
     * @param args
     *            standart main arguments
     */
    public static void main(String[] args) {
        junit.swingui.TestRunner.run(SecureWSDL2JavaTestSuite.class);
    }

    /**
     * Returns the test suite for the secure WSDL to Java generator
     * 
     * @return the test suite for the secure WSDL to Java generator
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Custom WSDL2Java generator");
        suite.addTest(new SecureWSDL2JavaTest("testTrustedCallersList"));
        suite.addTest(new SecureWSDL2JavaTest("testCustomWSDLWriter"));
        suite.addTest(new SecureWSDL2JavaTest("testParameterCompatibility"));
        suite.addTest(new SecureWSDL2JavaTest("testAccessListParsing"));
        suite.addTest(new SecureWSDL2JavaTest("testAPIAuthAccessList"));
        return (suite);
    }
}