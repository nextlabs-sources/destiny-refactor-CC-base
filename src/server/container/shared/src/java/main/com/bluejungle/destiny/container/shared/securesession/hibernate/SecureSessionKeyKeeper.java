/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import java.util.StringTokenizer;

import com.bluejungle.framework.crypt.ReversibleEncryptor;

/**
 * SecureSessionKeyKeeper is resposible for encrypting and decrypting secure
 * session keys
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/hibernate/SecureSessionKeyKeeper.java#3 $
 */

class SecureSessionKeyKeeper {

    private static final ReversibleEncryptor CIPHER = new ReversibleEncryptor();
    private static final String KEY_DELIMETER = "|";

    /**
     * Retrieve a secure session encrypted key string representing the provided
     * SecureSessionKey instance
     * 
     * @param keyData
     *            the SecureSessionKey instance from which the secure session
     *            encrypted key will be created
     * @return a secure session encrypted key string built from the provided
     *         SecureSessionKey instance
     */
    static String generateKeyString(SecureSessionKey keyData) {
        /*
         * Key format is: encrypted("session id|session expiration time")
         */
        StringBuffer keyBuffer = new StringBuffer();
        keyBuffer.append(keyData.getId());
        keyBuffer.append(KEY_DELIMETER);
        keyBuffer.append(keyData.getExpirationTime());
        keyBuffer.append(KEY_DELIMETER);
        keyBuffer.append(keyData.getEndOfLifeTime());

        return encrypt(keyBuffer.toString());
    }

    /**
     * Encrypt the provided secure session key
     * 
     * @param keyToEncrypt
     *            the key to encrypt
     * @return the encrypted value
     */
    private static String encrypt(String keyToEncrypt) {
        return CIPHER.encrypt(keyToEncrypt);
    }

    /**
     * Retrieve a SecureSessionKey instance from the secure session encrypted
     * key string
     * 
     * @param sessionKey
     *            the secure session key string to decrypt
     * @return a SecureSessionKey instance built from the provided secure
     *         session encrypted key string
     */
    static SecureSessionKey extractKey(String sessionKey) {
        String decryptedKey = decrypt(sessionKey);

        StringTokenizer tokenizer = new StringTokenizer(decryptedKey, KEY_DELIMETER);
        Long sessionID = Long.valueOf(tokenizer.nextToken());
        Long expirationTime = Long.valueOf(tokenizer.nextToken());
        Long endOfLife = Long.valueOf(tokenizer.nextToken());

        return new SecureSessionKey(sessionID, expirationTime, endOfLife);
    }

    /**
     * Decrypt the provided secure sesison key string
     * 
     * @param sessionKey
     *            the secure session key string to decrypt
     * @return the decrypted value
     */
    private static String decrypt(String sessionKey) {
        return CIPHER.decrypt(sessionKey);
    }
}