/*
 * Created on Dec 12, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.security;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Unit test for {@link KeyManagerImpl}
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_1.5.1/main/src/common/framework/src/java/test/com/bluejungle/framework/security/TestKeyManagerImpl.java#2 $
 */

public class TestKeyManagerImpl extends TestCase {

    private static final String BASE_DIR_PROPERTY_NAME = "src.root.dir";

    private static final String RELATIVE_KEYSTORE_FILE_NAME = "/test_files/" + TestKeyManagerImpl.class.getPackage().getName().replaceAll("[.]", "/") + "/keystore.jks";
    private static final String RELATIVE_TRUSTSTORE_FILE_NAME = "/test_files/" + TestKeyManagerImpl.class.getPackage().getName().replaceAll("[.]", "/") + "/truststore.jks";
    private static final String RELATIVE_SECRET_KEYSTORE_FILE_NAME = "/test_files/" + TestKeyManagerImpl.class.getPackage().getName().replaceAll("[.]", "/") + "/secretkeystore.jks";
    private static final String RELATIVE_EMPTY_SECRET_KEYSTORE_FILE_NAME = "/test_files/" + TestKeyManagerImpl.class.getPackage().getName().replaceAll("[.]", "/") + "/emptysecretkeystore.jks";

    private KeyManagerImpl keyManagerToTest;
    private String baseDir;


    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        this.baseDir = System.getProperty(BASE_DIR_PROPERTY_NAME);
        if (this.baseDir == null) {
            throw new IllegalStateException(BASE_DIR_PROPERTY_NAME + " system property must be specified for unit test to function");
        }

        /*
         * This code is used to create the secret keystore test file. Needs to
         * only be run once. Refactor this to be a little bit nicer in the
         * future KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
         * keyGenerator.init(128); SecretKey secretKey =
         * keyGenerator.generateKey();
         * 
         * KeyStore keyStore = KeyStore.getInstance("JCEKS"); keyStore.load(new
         * FileInputStream(baseDir + RELATIVE_KEYSTORE_FILE_NAME),
         * "password".toCharArray()); keyStore.setKeyEntry("testSecret",
         * secretKey, "password".toCharArray(), null); keyStore.store(new
         * FileOutputStream(baseDir + RELATIVE_SECRET_KEYSTORE_FILE_NAME),
         * "password".toCharArray());
         */

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration configuration = new HashMapConfiguration();
        Set<KeyManagerImpl.KeystoreFileInfo> keyStoreFileInfo = new HashSet<KeyManagerImpl.KeystoreFileInfo>();
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("privateKeyStore", this.baseDir + RELATIVE_KEYSTORE_FILE_NAME, "jks", "password"));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("publicTrustStore", this.baseDir + RELATIVE_TRUSTSTORE_FILE_NAME, "jks", "password"));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("secretKeyStore", this.baseDir + RELATIVE_SECRET_KEYSTORE_FILE_NAME, "jceks", "password"));
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("emptySecretKeyStore", this.baseDir + RELATIVE_EMPTY_SECRET_KEYSTORE_FILE_NAME, "jceks", "password"));
        configuration.setProperty(KeyManagerImpl.KEYSTORE_FILE_INFO_PROPERTY_NAME, keyStoreFileInfo);
        ComponentInfo<KeyManagerImpl> componentInfo = 
            new ComponentInfo<KeyManagerImpl>(
                IKeyManager.COMPONENT_NAME, 
                KeyManagerImpl.class, 
                IKeyManager.class, 
                LifestyleType.SINGLETON_TYPE, 
                configuration);

        this.keyManagerToTest = componentManager.getComponent(componentInfo);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();

        File emptySecretKeyStoreFile = new File(this.baseDir + RELATIVE_EMPTY_SECRET_KEYSTORE_FILE_NAME);
        emptySecretKeyStoreFile.delete();
    }

    /**
     * Test method for
     * {@link com.bluejungle.framework.security.KeyManagerImpl#getConfiguration()}
     * and
     * {@link com.bluejungle.framework.security.KeyManagerImpl#setConfiguration(com.bluejungle.framework.comp.IConfiguration)}.
     */
    public void testGetSetConfiguration() {
        assertNotNull("testGetSetConfiguration - Ensure configuration is not initially null", this.keyManagerToTest.getConfiguration());
        HashMapConfiguration configuration = new HashMapConfiguration();
        this.keyManagerToTest.setConfiguration(configuration);
        assertEquals("testGetSetConfiguration - Ensure configuration set is that retrieved", configuration, this.keyManagerToTest.getConfiguration());
    }

    /**
     * Test method for
     * {@link com.bluejungle.framework.security.KeyManagerImpl#getPrivateKey(java.lang.String)}.
     */
    public void testGetPrivateKey() {
        // First, try with a key that exists
        PrivateKey testKey = this.keyManagerToTest.getPrivateKey("testDSA");
        assertNotNull("testGetPrivateKey - Ensure private key can be retrieved.", testKey);

        // Now try with key that does not exist
        try {
            this.keyManagerToTest.getPrivateKey("foobar");
            fail("Should throw KeyNotFoundException for invalid key alias");
        } catch (KeyNotFoundException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.framework.security.KeyManagerImpl#getPublicKey(java.lang.String)}.
     */
    public void testGetPublicKey() {
        // First, try with a key that exists
        PublicKey testKey = this.keyManagerToTest.getPublicKey("testDSA");
        assertNotNull("testGetPublicKey - Ensure public key can be retrieved.", testKey);

        // Now try with key that does not exist
        try {
            this.keyManagerToTest.getPublicKey("foobar");
            fail("Should throw KeyNotFoundException for invalid key alias");
        } catch (KeyNotFoundException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.framework.security.KeyManagerImpl#getSecretKey(java.lang.String)}.
     */
    public void testGetSecretKey() {
        // First, try with a key that exists
        SecretKey testKey = this.keyManagerToTest.getSecretKey("testSecret");
        assertNotNull("testGetSecretKey - Ensure secret key can be retrieved.", testKey);

        // Now try with key that does not exist
        try {
            this.keyManagerToTest.getSecretKey("foobar");
            fail("Should throw KeyNotFoundException for invalid key alias");
        } catch (KeyNotFoundException exception) {

        }
    }

    /**
     * Test method for {@link KeyManagerImpl#getCertificateTrustManager(String)}
     * 
     */
    public void testGetCertificateTrustManager() {
        X509TrustManager trustManager = this.keyManagerToTest.getCertificateTrustManager("publicTrustStore");
        assertNotNull("testGetCertificateTrustManager - Ensure trust manager is not null");

        try {
            this.keyManagerToTest.getCertificateTrustManager(null);
            fail("testGetCertificateTrustManager - Should throw NPE for null argument");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for {@link KeyManagerImpl#getCertificateKeyManager(String)}
     * 
     */
    public void testGetCertificateKeyManager() {
        X509KeyManager keyManager = this.keyManagerToTest.getCertificateKeyManager("privateKeyStore");
        assertNotNull("testGetCertificateKeyManager - Ensure key manager is not null");

        try {
            this.keyManagerToTest.getCertificateKeyManager(null);
            fail("testGetCertificateKeyManager - Should throw NPE for null argument");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for {@link KeyManagerImpl#containsKeystore(String)}
     * 
     */
    public void testContainsKeystore() {
        assertTrue("testContainsKeystore - Ensure it contains private key store", this.keyManagerToTest.containsKeystore("privateKeyStore"));
        assertFalse("testContainsKeystore - Ensure it doesn't contain unknown key store", this.keyManagerToTest.containsKeystore("foo"));

        try {
            this.keyManagerToTest.containsKeystore(null);
            fail("testContainsKeyStore - Should throw NPE for null argument");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for {@link KeyManagerImpl#containsPublicKey(String)}
     * 
     */
    public void testContainsPublicKey() {
        assertTrue("testContainsPublicKey - Ensure it contains public key", this.keyManagerToTest.containsPublicKey("testDSA"));
        assertFalse("testContainsPublicKey - Ensure it doesn't contains bogus public key", this.keyManagerToTest.containsPublicKey("foo"));

        try {
            this.keyManagerToTest.containsPublicKey(null);
            fail("testContainsPublicKey - Should throw NPE for null argument");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for {@link KeyManagerImpl#containsPublicKey(String)}
     * 
     */
    public void testContainsSecretKey() {
        assertTrue("testContainsSecretKey - Ensure it contains secret key", this.keyManagerToTest.containsSecretKey("testSecret"));
        assertFalse("testContainsSecretKey - Ensure it doesn't contains bogus secret key", this.keyManagerToTest.containsSecretKey("foo"));

        try {
            this.keyManagerToTest.containsSecretKey(null);
            fail("testContainsSecretKey - Should throw NPE for null argument");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link KeyManagerImpl#addSecretKey(String, SecretKey, String)}
     */
    public void testAddSecretKey() throws NoSuchAlgorithmException, IOException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();

        String keyAlias = "emptySecretKeyAlias";
        this.keyManagerToTest.addSecretKey(keyAlias, secretKey, "emptySecretKeyStore");
        assertNotNull("testAddSecretKey - Ensure key was added to empty key store", this.keyManagerToTest.getSecretKey(keyAlias));

        // Test errors
        try {
            this.keyManagerToTest.addSecretKey(null, secretKey, "secretKeyStore");
            fail("testAddSecretKey - Should throw NPE for null key alias");
        } catch (NullPointerException exception) {
        }

        try {
            this.keyManagerToTest.addSecretKey(keyAlias, null, "secretKeyStore");
            fail("testAddSecretKey - Should throw NPE for null key");
        } catch (NullPointerException exception) {
        }

        try {
            this.keyManagerToTest.addSecretKey(keyAlias, secretKey, null);
            fail("testAddSecretKey - Should throw NPE for null key store name");
        } catch (NullPointerException exception) {
        }

        try {
            this.keyManagerToTest.addSecretKey(keyAlias, secretKey, "bogus");
            fail("testAddSecretKey - Should throw IllegalArgumentException for invalid keystore name");
        } catch (IllegalArgumentException exception) {
        }
    }
}
