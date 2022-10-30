/*
 * Created on Sep 14, 2011
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.framework.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bluejungle.framework.crypt.IEncryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.random.RandomString;

/**
 * @author name
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test4/com/nextlabs/framework/test/ReversibleEncryptorTest.java#1 $
 */

public class ReversibleEncryptorTest {

	private static final String TEST_INPUT_FILE = "/com/nextlabs/framework/test/nextlabs-encryption.txt";
	
	private ReversibleEncryptor re;
	
	@Before
	public void setup() {
		re = new ReversibleEncryptor();
	}
	
	
	/**
	 * used to generate test file from previous Encryptor
	 * @throws IOException
	 */
	private void generate() throws IOException {
		
		File file = new File("nextlabs-encryption.txt");
		
		FileWriter writer = new FileWriter(file);
		
		final int[] lengthList = new int[] { 1, 2, 5, 8, 10, 20, 50, 100, 200, 500 };
		final int numberOfPassword = 50;
		
		for (int length : lengthList) {
			for (int i = 0; i < numberOfPassword; i++) {
				String original = RandomString.getRandomString(length, length, RandomString.PRINT);
				String encoded = re.encrypt(original);
				writer.append(original + "\t" + encoded + "\n");
			}
		}
		
		writer.close();
	}
	
	private List<String[]> readTestFile() throws IOException {
		List<String[]> list = new LinkedList<String[]>();
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new InputStreamReader(
            		ReversibleEncryptorTest.class.getResourceAsStream(TEST_INPUT_FILE)));
            
            String line;
            while((line = bf.readLine()) != null){
            	int seperatorIndex = line.indexOf('\t');
                list.add(new String[] { line.substring(0, seperatorIndex), line.substring(seperatorIndex + 1) });
            }
        } finally {
            if (bf != null) {
                bf.close();
            }
        }
        return list;
	}
	
	@Test
	public void decryptNextlabsString() throws IOException {
		List<String[]> data = readTestFile();
		assertFalse(data.isEmpty());
		for(String[] d : data) {
			assertEquals(d[0], re.decrypt(d[1]));
		}
	}
	
	@Test
    public void defaultIsAES128() {
	    String encrypted = re.encrypt("123");
	    assertTrue(encrypted.startsWith("s"));
    }
	
	@Test
	public void encryptThenDecrypt() {
	    final int[] lengthList = new int[] { 1, 2, 5, 8, 10, 20, 50, 100, 200, 500 };
        final int numberOfPassword = 50;
        
        for (int length : lengthList) {
            for (int i = 0; i < numberOfPassword; i++) {
                String original = RandomString.getRandomString(length, length, RandomString.PRINT);
                String encrypted = re.encrypt(original);
                String decrypted = re.decrypt(encrypted);
                assertEquals(original, decrypted);
            }
        }
    }
	
	@Test(expected = IllegalArgumentException.class)
    public void noSuchAlgorithm() {
	    re.encrypt("123", "somethingdontexist");
    }
	
	@Test
    public void algorithmIsCaseInsensitive() {
        re.encrypt("123", "n");
        re.encrypt("123", "N");
    }
	
	@Test
    public void nullValue() {
        assertNull(re.encrypt(null));
        assertNull(re.decrypt(null));
    }
	
	private void notSupported(String className, String algorithm) throws IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException,
            SecurityException, NoSuchMethodException, ClassNotFoundException {
	    Class<IEncryptor> clazz = (Class<IEncryptor>)Class.forName(className);
        Constructor<IEncryptor> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        IEncryptor encryptor = constructor.newInstance();
        encryptor.encrypt("1", algorithm);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void notSupportedByNextlabs() throws Exception {
	    notSupported("com.bluejungle.framework.crypt.NextlabsCrypto", "s");
	}
	
	@Test
    public void supportedByNextlabs() throws Exception {
        notSupported("com.bluejungle.framework.crypt.NextlabsCrypto", "n");
    }
	
	@Test(expected = UnsupportedOperationException.class)
    public void notSupportedByAES128() throws Exception {
	    notSupported("com.bluejungle.framework.crypt.AESCrypto", "n");
    }
	
	@Test
    public void supportedByAES128() throws Exception {
        notSupported("com.bluejungle.framework.crypt.AESCrypto", "s");
    }
	
}
