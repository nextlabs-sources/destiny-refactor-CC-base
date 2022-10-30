package com.bluejungle.framework.crypt;

/**
 * This is the encryption interface.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/crypt/IEncryptor.java#1 $
 */

public interface IEncryptor {

    /**
     * Encrypts a message
     * 
     * @param original
     *            original message
     * @return an encrypted version of the message
     */
    String encrypt(String original);
    
    /**
     * 
     * @param original
     * @param algorithm case-insensitive
     * @return
     */
    String encrypt(String original, String algorithm);
}