/*
 * Created on Oct 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.utils.ldap;

import junit.framework.TestCase;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/utils/ldap/TestLDAPQueryUtils.java#1 $
 */

public class TestLDAPQueryUtils extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestLDAPQueryUtils.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testBinaryToHexStringConversion() {
        byte[] byteArrayToConvert = new byte[] { -88, -47, -10, -47, 58, 123, 28, 76, -85, 76, -32, 83, 78, 34, 125, -8 };
        String convertedString = LDAPQueryUtils.convertBinaryToHexString(byteArrayToConvert);
        assertNotNull("converted string should not be null", convertedString);
    }

    public void testBinaryToOctalStringConversion() {
        byte[] byteArrayToConvert = new byte[] { -88, -47, -10, -47, 58, 123, 28, 76, -85, 76, -32, 83, 78, 34, 125, -8 };
        String convertedString = LDAPQueryUtils.convertBinaryToOctalString(byteArrayToConvert);
        assertNotNull("converted string should not be null", convertedString);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for TestLDAPQueryUtils.
     * 
     * @param arg0
     */
    public TestLDAPQueryUtils(String arg0) {
        super(arg0);
    }

}