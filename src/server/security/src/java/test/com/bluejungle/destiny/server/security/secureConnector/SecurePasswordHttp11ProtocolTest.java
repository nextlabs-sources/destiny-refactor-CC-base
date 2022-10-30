/*
 * Created on Feb 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.security.secureConnector;

import junit.framework.TestCase;

/**
 * This is the test class for the secure connector
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/security/src/java/test/com/bluejungle/destiny/server/security/secureConnector/SecurePasswordHttp11ProtocolTest.java#1 $
 */

public class SecurePasswordHttp11ProtocolTest extends TestCase {

    private final String ENCRYPTED_VALUE = "4b5971583b4b385902712b58285f315e0c7e3155";
    private final String REAL_VALUE = "password";

    /**
     * Constructor
     */
    public SecurePasswordHttp11ProtocolTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public SecurePasswordHttp11ProtocolTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the keyStore password is properly set.
     */
    public void testKeyStorePass() {
        SecurePasswordHttp11Protocol protocol = new SecurePasswordHttp11Protocol();
        protocol.setKeypass(ENCRYPTED_VALUE);
        String decryptedValue = protocol.getKeypass();
        assertEquals("Keystore password should be decrypted", REAL_VALUE, decryptedValue);
    }

    /**
     * This test verifies that the keyStore password is properly set.
     */
    public void testTrustStorePass() {
        SecurePasswordHttp11Protocol protocol = new SecurePasswordHttp11Protocol();
        protocol.setProperty("truststorePass", ENCRYPTED_VALUE);
        String decryptedValue = (String) protocol.getProperty("truststorePass");
        assertEquals("Truststore password should be decrypted", REAL_VALUE, decryptedValue);
    }
}