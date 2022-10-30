/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/securesession/hibernate/SecureSessionKeyKeeperTest.java#1 $
 */

public class SecureSessionKeyKeeperTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SecureSessionKeyKeeperTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for SecureSessionKeyKeeperTest.
     * @param arg0
     */
    public SecureSessionKeyKeeperTest(String arg0) {
        super(arg0);
    }

    public void testGenerateKeyString() {
        Long id = new Long(55234);
        Long expirationTime = new Long(System.currentTimeMillis());
        Long endOfLifeTime = new Long(System.currentTimeMillis() * 2);
        SecureSessionKey sessionKey = new SecureSessionKey(id, expirationTime, endOfLifeTime);
        String generatedString = SecureSessionKeyKeeper.generateKeyString(sessionKey);
        assertNotNull("testGenerateKey - Ensure generated key is not null.", generatedString);
        assertTrue("testGenerateKey - Test basic encryption", ((generatedString.indexOf(id.toString()) < 0) && (generatedString.indexOf(expirationTime.toString()) < 0) && (generatedString.indexOf(endOfLifeTime.toString()) < 0)));
    }

    public void testExtractKey() {
        Long id = new Long(55234);
        Long expirationTime = new Long(System.currentTimeMillis());
        Long endOfLifeTime = new Long(System.currentTimeMillis() * 2);
        SecureSessionKey sessionKey = new SecureSessionKey(id, expirationTime, endOfLifeTime);
        String generatedString = SecureSessionKeyKeeper.generateKeyString(sessionKey);
        assertNotNull("testExtractKey - Ensure generated key is not null.", generatedString);
        
        SecureSessionKey extractedKey = SecureSessionKeyKeeper.extractKey(generatedString);
        assertEquals("testExtractKey - Ensure extracted key is equal to original", sessionKey, extractedKey);
    }

}
