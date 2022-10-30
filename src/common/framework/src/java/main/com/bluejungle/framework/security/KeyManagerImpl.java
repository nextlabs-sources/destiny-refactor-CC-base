package com.bluejungle.framework.security;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.PropertyKey;

import javax.crypto.SecretKey;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class KeyManagerImpl implements IKeyManager, IInitializable, IConfigurable {

    public static final PropertyKey<Set<KeyManagerImpl.KeystoreFileInfo>> KEYSTORE_FILE_INFO_PROPERTY_NAME =
            new PropertyKey<Set<KeystoreFileInfo>>("keystoreFileName");

    private IConfiguration configuration;
    private Map<String, KeyStore> keystoreMap = new HashMap<String, KeyStore>();
    private Map<String, KeyManagerImpl.KeystoreFileInfo> keystoreInfoMap = new HashMap<String, KeyManagerImpl.KeystoreFileInfo>();

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration configuration = this.getConfiguration();
        Set<KeyManagerImpl.KeystoreFileInfo> keystoreFileInfo = configuration.get(KEYSTORE_FILE_INFO_PROPERTY_NAME);
        if (keystoreFileInfo == null) {
            throw new IllegalStateException("Keystore File Info is null");
        }

        for (KeyManagerImpl.KeystoreFileInfo nextKeystoreFileInfo : keystoreFileInfo) {
            try {
                String keystoreName = nextKeystoreFileInfo.getName();
                KeyStore nextKeystore = KeyStore.getInstance(nextKeystoreFileInfo.getKeystoreType());
                this.keystoreMap.put(keystoreName, nextKeystore);
                this.keystoreInfoMap.put(keystoreName, nextKeystoreFileInfo);

                char[] keystorePassword = nextKeystoreFileInfo.getKeystoreFilePassword().toCharArray();
                File nextKeystoreFile = new File(nextKeystoreFileInfo.getKeystoreFilePath());
                if (nextKeystoreFile.exists()) {
                    FileInputStream nextKeystoreFileInputStream = new FileInputStream(nextKeystoreFile);
                    nextKeystore.load(nextKeystoreFileInputStream, keystorePassword);
                    nextKeystoreFileInputStream.close();
                } else {
                    nextKeystore.load(null, keystorePassword);
                }
            } catch (KeyStoreException exception) {
                throw new IllegalStateException("Failed to load Keystore: " + exception.getMessage());
            } catch (FileNotFoundException exception) {
                throw new IllegalStateException("Failed to read Keystore file, " + nextKeystoreFileInfo.getKeystoreFilePath());
            } catch (NoSuchAlgorithmException exception) {
                throw new IllegalStateException("Failed to load Keystore.  Algorithm not provided: " + exception.getMessage());
            } catch (CertificateException exception) {
                throw new IllegalStateException("Failed to load certificates from Keystore: " + exception.getMessage());
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load Keystore.  Error reading file or password incorrect.  Error detail: " + exception.getMessage());
            }
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException("configuration cannot be null.");
        }

        this.configuration = configuration;
    }

    /**
     * @see com.bluejungle.framework.security.IKeyManager#getPrivateKey(java.lang.String)
     */
    public PrivateKey getPrivateKey(String keyAlias) {
        if (keyAlias == null) {
            throw new NullPointerException("keyAlias cannot be null.");
        }

        PrivateKey keyToReturn = (PrivateKey) getKeyFromKeystore(keyAlias);

        if (keyToReturn == null) {
            StringBuffer errorMessage = new StringBuffer("Private key with the alias, ");
            errorMessage.append(keyAlias);
            errorMessage.append(", could not be found.");
            throw new KeyNotFoundException(errorMessage.toString());
        }
        return keyToReturn;
    }

    /**
     * 
     * @see com.bluejungle.framework.security.IKeyManager#getPublicKey(java.lang.String)
     */
    public PublicKey getPublicKey(String keyAlias) {
        if (keyAlias == null) {
            throw new NullPointerException("keyAlias cannot be null.");
        }

        Certificate publicKeyCertificate = getPublicKeyImpl(keyAlias);

        if (publicKeyCertificate == null) {
            StringBuffer errorMessage = new StringBuffer("Public key certificate with the alias, ");
            errorMessage.append(keyAlias);
            errorMessage.append(", could not be found.");
            throw new KeyNotFoundException(errorMessage.toString());
        }
        return publicKeyCertificate.getPublicKey();
    }

    /**
     * @see com.bluejungle.framework.security.IKeyManager#containsPublicKey(java.lang.String)
     */
    public boolean containsPublicKey(String keyAlias) {
        if (keyAlias == null) {
            throw new NullPointerException("keyAlias cannot be null.");
        }

        Certificate publicKeyCertificate = getPublicKeyImpl(keyAlias);
        return (publicKeyCertificate != null);
    }

    /**
     * 
     * @see com.bluejungle.framework.security.IKeyManager#getSecretKey(java.lang.String)
     */
    public SecretKey getSecretKey(String keyAlias) {
        if (keyAlias == null) {
            throw new NullPointerException("keyAlias cannot be null.");
        }

        SecretKey keyToReturn = (SecretKey) getKeyFromKeystore(keyAlias);

        if (keyToReturn == null) {
            StringBuffer errorMessage = new StringBuffer("Secret key with the alias, ");
            errorMessage.append(keyAlias);
            errorMessage.append(", could not be found.");
            throw new KeyNotFoundException(errorMessage.toString());
        }
        return keyToReturn;
    }

    
    /**
     * @see com.bluejungle.framework.security.IKeyManager#containsSecretKey(java.lang.String)
     */
    public boolean containsSecretKey(String keyAlias) {
        if (keyAlias == null) {
            throw new NullPointerException("keyAlias cannot be null.");
        }

        SecretKey keyToReturn = (SecretKey) getKeyFromKeystore(keyAlias);
        
        return (keyToReturn != null);        
    }

    /**
     * @throws IOException
     * @see com.bluejungle.framework.security.IKeyManager#addSecretKey(String,
     *      javax.crypto.SecretKey, java.lang.String)
     */
    public void addSecretKey(String keyAlias, SecretKey secretKey, String keystoreName) throws IOException {
        if (keyAlias == null) {
            throw new NullPointerException("keyAlias cannot be null.");
        }

        if (secretKey == null) {
            throw new NullPointerException("secretKey cannot be null.");
        }
        
        if (keystoreName == null) {
            throw new NullPointerException("keystoreName cannot be null.");
        }
        
        if (!this.keystoreMap.containsKey(keystoreName)) {
            throw new IllegalArgumentException("Invalid keystoreName: " + keystoreName);
        }

        KeyStore keyStore = this.keystoreMap.get(keystoreName);
        KeystoreFileInfo keystoreFileInfo = this.keystoreInfoMap.get(keystoreName);

        char[] keystorePassword = keystoreFileInfo.getKeystoreFilePassword().toCharArray();

        try {
            keyStore.setKeyEntry(keyAlias, secretKey, keystorePassword, null);
            File storeFile = new File(keystoreFileInfo.getKeystoreFilePath());
            if (!storeFile.exists()) {
                storeFile.createNewFile();
            }
            FileOutputStream storeOutputStream = new FileOutputStream(keystoreFileInfo.getKeystoreFilePath());
            keyStore.store(storeOutputStream, keystorePassword);
        } catch (KeyStoreException exception) {
            throw new IllegalStateException("Failed to add key to Keystore: " + exception.getMessage());
        } catch (FileNotFoundException exception) {
            throw new IllegalStateException("Failed to add key to Keystore.  Could not find keystore file: " + exception.getMessage());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Failed to store Keystore.  Algorithm not provided: " + exception.getMessage());
        } catch (CertificateException exception) {
            throw new IllegalStateException("Could not store certificates: " + exception.getMessage());
        }
    }

    /**
     * @see com.bluejungle.framework.security.IKeyManager#getCertificateKeyManager(java.lang.String)
     */
    public X509KeyManager getCertificateKeyManager(String keystoreName) {
        if (keystoreName == null) {
            throw new NullPointerException("truststoreName cannot be null.");
        }

        if (!this.keystoreMap.containsKey(keystoreName)) {
            throw new IllegalArgumentException("Invalid truststoreName: " + keystoreName);
        }

        KeyStore keyStore = this.keystoreMap.get(keystoreName);
        KeystoreFileInfo keyStoreInfo = this.keystoreInfoMap.get(keystoreName);

        X509KeyManager managerToReturn = null;
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStoreInfo.getKeystoreFilePassword().toCharArray());
            KeyManager[] keyStoreManagers = keyManagerFactory.getKeyManagers();
            managerToReturn = (X509KeyManager) keyStoreManagers[0];
        } catch (NoSuchAlgorithmException exception) {
            // Shouldn't happen, since we're using the Sun Provider
            throw new IllegalStateException("KeyManagerFactory algorithm not provided");
        } catch (KeyStoreException exception) {
            // FIX ME - Should this be thrown as a checked exception?
            throw new IllegalStateException("Failed to read Keystore: " + exception.getMessage());
        } catch (UnrecoverableKeyException exception) {
            // FIX ME - Should this be thrown as a checked exception?
            throw new IllegalStateException("Failed to read key from keystore: " + exception.getMessage());
        }

        return managerToReturn;
    }

    /**
     * @see com.bluejungle.framework.security.IKeyManager#getCertificateTrustManager(java.lang.String)
     */
    public X509TrustManager getCertificateTrustManager(String truststoreName) {
        if (truststoreName == null) {
            throw new NullPointerException("truststoreName cannot be null.");
        }

        if (!this.keystoreMap.containsKey(truststoreName)) {
            throw new IllegalArgumentException("Invalid truststoreName: " + truststoreName);
        }

        KeyStore keyStore = this.keystoreMap.get(truststoreName);

        X509TrustManager managerToReturn = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustStoreManagers = trustManagerFactory.getTrustManagers();
            managerToReturn = (X509TrustManager) trustStoreManagers[0];
        } catch (NoSuchAlgorithmException exception) {
            // Shouldn't happen, since we're using the Sun Provider
            throw new IllegalStateException("TrustManagerFactory algorithm not provided");
        } catch (KeyStoreException exception) {
            // FIX ME - Should this be thrown as a checked exception?
            throw new IllegalStateException("Failed to read Keystore: " + exception.getMessage());
        }

        return managerToReturn;
    }

    /**
     * @see com.bluejungle.framework.security.IKeyManager#containsKeystore(java.lang.String)
     */
    public boolean containsKeystore(String keystoreName) {
        if (keystoreName == null) {
            throw new NullPointerException("keystoreName cannot be null.");
        }

        return this.keystoreMap.containsKey(keystoreName);
    }

    /**
     * Retrieve the public key with the specified key alias
     * 
     * @param keyAlias
     * @return the public key with the specified alias or null if it doesn't
     *         exist
     */
    private Certificate getPublicKeyImpl(String keyAlias) {
        Certificate publicKeyCertificate = null;

        try {
            for (KeyStore keyStore : keystoreMap.values()) {
                Certificate cert = keyStore.getCertificate(keyAlias);
                if (cert != null) {
                    String issuerDN = ((X509Certificate)cert).getIssuerDN().getName();
                    
                    /*
                     * do not check for expiry if we issued the certificate. Our self-signed certificates have expired,
                     * but changing them is hard because of backwards compatibility concerns.
                     * 
                     * Our certs:
                     * Issuer: CN=Key Management Application, OU=CompliantEnterprise, O=BlueJungle, L=San Mateo, ST=CA, C=US
                     * Issuer: CN=CompliantEnterprise Server, OU=CompliantEnterprise, O=NextLabs, L=San Mateo, ST=CA, C=US
                     */
                    
                    if (!(issuerDN.contains("NextLabs") || issuerDN.contains("BlueJungle"))){
                        ((X509Certificate)cert).checkValidity();
                    }
                    publicKeyCertificate = cert;
                    break;
                }
            }
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Failed to read Key from keystore.  Keystore not loaded: " + e.getMessage());
        } catch (CertificateExpiredException e) {
            throw new IllegalStateException("Failed to load public key cert. Certificate expired: " + e.getMessage(), e);
        } catch (CertificateNotYetValidException e) {
            throw new IllegalStateException("Failed to load public key cert. Certificate not yet valid: " + e.getMessage(), e);
        }
        return publicKeyCertificate;
    }

    /**
     * Retrieve a key from the keystore
     * 
     * @param keyAlias
     *            the key alias
     * @param keyPassword
     *            the key password
     * @param keystore
     *            the keystore
     * @return the key from the keystore
     */
    private Key getKeyFromKeystore(String keyAlias) {
        if (keyAlias == null) {
            throw new NullPointerException("keyAlias cannot be null.");
        }

        Key keyToReturn = null;
        try {
            Iterator<Map.Entry<String, KeyStore>> keyStoreMapIterator = this.keystoreMap.entrySet().iterator();
            while ((keyStoreMapIterator.hasNext()) && (keyToReturn == null)) {
                Map.Entry<String, KeyStore> nextKeystoreEntry = keyStoreMapIterator.next();
                KeyStore nextKeystore = nextKeystoreEntry.getValue();
                KeystoreFileInfo nextKeystoreFileInfo = this.keystoreInfoMap.get(nextKeystoreEntry.getKey());
                // currently use the keystore password as the key password
                String nextKeystorePassword = nextKeystoreFileInfo.getKeystoreFilePassword();
                char[] nextKeystorePasswordCharArray = (nextKeystorePassword == null) ? null : nextKeystorePassword.toCharArray();
                keyToReturn = nextKeystore.getKey(keyAlias, nextKeystorePasswordCharArray);
            }
        } catch (KeyStoreException exception) {
            throw new IllegalStateException("Failed to read Key from keystore.  Keystore not loaded: " + exception.getMessage());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Failed to load Key from keystore.  Algorithm not provided: " + exception.getMessage());
        } catch (UnrecoverableKeyException exception) {
            throw new IllegalStateException("Failed to load Key from keystore.  Password may not be correct: " + exception.getMessage());
        }
        return keyToReturn;
    }

    /**
     * KeystoreFileInfo describes the characteristics of a key store file
     * 
     * @author sgoldstein
     */
    public static class KeystoreFileInfo {

        private String name;
        private String keystoreFilePath;
        private String keystoreType;
        private String keystoreFilePassword;

        /**
         * Create an instance of KeystoreFileInfo
         * 
         * @param name
         * @param keystoreFilePath
         * @param keystoreType
         * @param keystoreFilePassword
         */
        public KeystoreFileInfo(String name, String keystoreFilePath, String keystoreType, String keystoreFilePassword) {
            if (name == null) {
                throw new NullPointerException("name cannot be null.");
            }

            if (keystoreFilePath == null) {
                throw new NullPointerException("keystoreFilePath cannot be null.");
            }

            if (keystoreType == null) {
                throw new NullPointerException("keystoreType cannot be null.");
            }

            this.name = name;
            this.keystoreFilePath = keystoreFilePath;
            this.keystoreType = keystoreType;
            this.keystoreFilePassword = keystoreFilePassword;
        }

        /**
         * Retrieve the keystoreFilePassword.
         * 
         * @return the keystoreFilePassword.
         */
        public String getKeystoreFilePassword() {
            return this.keystoreFilePassword;
        }

        /**
         * Retrieve the keystoreFilePath.
         * 
         * @return the keystoreFilePath.
         */
        public String getKeystoreFilePath() {
            return this.keystoreFilePath;
        }

        /**
         * Retrieve the keystoreType.
         * 
         * @return the keystoreType.
         */
        public String getKeystoreType() {
            return this.keystoreType;
        }

        /**
         * Retrieve the name.
         * 
         * @return the name.
         */
        public String getName() {
            return this.name;
        }
    }
}
