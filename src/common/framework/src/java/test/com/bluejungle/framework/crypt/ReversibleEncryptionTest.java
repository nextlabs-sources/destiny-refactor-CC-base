/*
 * Created on Feb 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.crypt;

import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.nextlabs.random.RandomString;

/**
 * This is the test class for the reversible encryption test.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/src/java/main/com/bluejungle/framework/crypt/test/ReversibleEncryptionTest.java#1 $
 */

public class ReversibleEncryptionTest extends BaseDestinyTestCase {

    /**
     * Constructor
     */
    public ReversibleEncryptionTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public ReversibleEncryptionTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that null values are properly handled
     */
    public void testNullInputs() {
        ReversibleEncryptor cryptor = new ReversibleEncryptor();
        String encrypted = cryptor.encrypt(null);
        assertNull("A null string should be encrypted as null", encrypted);
        String decrypted = cryptor.decrypt(null);
        assertNull("A null string should be decrypted as null", decrypted);
    }

    /**
     * This function tests the random string class
     */
    public void testRandomStrings() {
        boolean exThrown = false;
        try {
            String string = RandomString.getRandomString(10, 5, 0, 127);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue("min and max size should be consistent", exThrown);

        exThrown = false;
        try {
            String string = RandomString.getRandomString(5, 10, 65, 20);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue("min and max range should be consistent", exThrown);

        final int minSize = 10;
        final int maxSize = 20;
        final int minRange = 66;
        final int maxRange = 110;
        int nbTest = 100;
        while (nbTest-- > 0) {
            String string = RandomString.getRandomString(minSize, maxSize, minRange, maxRange);
            assertTrue("String should not be higher than maxSize", string.length() <= maxSize);
            assertTrue("String should not be smaller than minSize", string.length() >= minSize);
            int size = string.length();
            for (int i = 0; i < size; i++) {
                char currentChar = string.charAt(i);
                assertTrue("Each character should be within the character range", (int) currentChar <= maxRange);
                assertTrue("Each character should be within the character range", (int) currentChar >= minRange);
            }
        }
    }

    /**
     * This test verifies that the encryption / decryption works fine.
     */
    public void testReversibleEncryption() {
        final String msgToEncrypt = RandomString.getRandomString(10, 20, 32, 127);
        ReversibleEncryptor cryptor = new ReversibleEncryptor();
        String cryptedMsg = cryptor.encrypt(msgToEncrypt);
        assertNotNull(cryptedMsg);
        assertNotSame(cryptedMsg, msgToEncrypt);
        String decryptedMsg = cryptor.decrypt(cryptedMsg);
        assertEquals(msgToEncrypt, decryptedMsg);
    }

    /**
     * This test specifically verifies that the non alpha numberic characters
     * are handled properly
     */
    public void testNonAlphaNumericCharacters() {
        ReversibleEncryptor cryptor = new ReversibleEncryptor();
        final String msgToEncrypt = "+-*/|?/>.<,;:'\"[{]}\\=_-)(*&^%$#@!~`";
        String cryptedMsg = cryptor.encrypt(msgToEncrypt);
        assertNotNull(cryptedMsg);
        assertNotSame(cryptedMsg, msgToEncrypt);
        String result = cryptor.decrypt(cryptedMsg);
        assertEquals(msgToEncrypt, result);
    }

    /**
     * This test simply checks that the 123blue! value works.
     */
    public void testDefaultPassword() {
        ReversibleEncryptor cryptor = new ReversibleEncryptor();
        final String msgToEncrypt = "123blue!";
        String cryptedMsg = cryptor.encrypt(msgToEncrypt);
        assertNotNull(cryptedMsg);
        assertNotSame(cryptedMsg, msgToEncrypt);
        String result = cryptor.decrypt(cryptedMsg);
        assertEquals(msgToEncrypt, result);
    }
}