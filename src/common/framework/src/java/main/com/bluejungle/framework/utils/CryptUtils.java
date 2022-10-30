/*
 * Created on Sep 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Various encryption-related utilities
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/CryptUtils.java#1 $:
 */

public final class CryptUtils {

    public static void main(String[] args) {
        Random random = new SecureRandom();
        System.out.println(random.nextInt());
    }
    
    /**
     * creates a digest of the given input, with the given algorith, using a randomly-generated salt of specified size
     * 
     * 
     * @param input String to digest
     * @param algName name of the algorithm, supports all the ones supported by java.security.MessageDigest
     * @param saltBytes number of bytes to use for the salt
     * @return digested (input + salt) concatenated with salt
     * @throws NoSuchAlgorithmException if specified algorithm is not supported
     */
    public final static byte[] digest(String input, String algName, int saltBytes) throws NoSuchAlgorithmException {
        byte[] pBytes = input.getBytes();
        MessageDigest md = MessageDigest.getInstance(algName);
        md.update(pBytes);
        byte[] salt = new byte[0];

        if (saltBytes > 0) {
            char[] chars = input.toCharArray();

            long seed = 0;
            for (int i = chars.length - 1; i >= 0; i--) {
                seed ^= chars[i];
            }
            seed ^= System.currentTimeMillis();
            
            Random random = new SecureRandom(new byte[] {
                    (byte) ((seed >> 56) & 0xff),
                    (byte) ((seed >> 48) & 0xff),
                    (byte) ((seed >> 40) & 0xff),
                    (byte) ((seed >> 32) & 0xff),
                    (byte) ((seed >> 24) & 0xff),
                    (byte) ((seed >> 16) & 0xff),
                    (byte) ((seed >> 8) & 0xff),
                    (byte) ((seed >> 0) & 0xff)
            });
            salt = new byte[saltBytes];
            random.nextBytes(salt);
            md.update(salt);
        }
        byte[] digest = md.digest();
        return ArrayUtils.concatenate(new byte[][] { digest, salt });
    }
}
