/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import java.security.SecureRandom;
import java.util.Random;


/**
 * Class with utilities that might be helpful for testing, particularly,
 * for generating random data
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/TestUtils.java#1 $
 *
 */
public class TestUtils {
    
    // unnecessary to use SecureRandom but hope veracode will be happier if we fix more.
    public static final Random rand = new SecureRandom();
    
    public static final String filenameString = "abcdefghijklmnopqrstuvwxyz \\ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.-_~!";
    public static final char[] filenameChars = filenameString.toCharArray();
    



    public static final String genRandomString(int length) {
        char[] rv = new char[length];
        for (int i = 0; i < length; i++) {
            rv[i] = TestUtils.filenameChars[rand.nextInt(TestUtils.filenameChars.length)];
        }
        return new String(rv);
    }
    
    public static final String getRandomIPAddress() {
        StringBuffer rv = new StringBuffer(14);
        for (int i = 0; i < 4; i++) {
            int bits = rand.nextInt(256);
            rv.append(bits);
            if (i < 3) {
                rv.append('.');
            }
        }
        return rv.toString();
    }
    
    public static final String genRandomNumericString(int size) {
        StringBuffer rv = new StringBuffer(size);
        for (int i = 0; i < size; i++) {
            rv.append(rand.nextInt(10));
        }
        return rv.toString();
    }


}
