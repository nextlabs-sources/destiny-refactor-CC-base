package com.bluejungle.framework.security;

import javax.crypto.SecretKey;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface IKeyManager {

    public static final String COMPONENT_NAME = "IKeyManager";

    /**
     * Retrieve the private key with the specified alias
     * 
     * @param keyAlias
     *            the key's alias
     * @return the private key associate with the specified alias
     */
    public PrivateKey getPrivateKey(String keyAlias) throws KeyNotFoundException;

    /**
     * Retrieve the public key with the specified alias
     * 
     * @param keyAlias
     *            the key's alias
     * @return the public key associate with the specified alias
     */
    public PublicKey getPublicKey(String keyAlias) throws KeyNotFoundException;

    /**
     * Retrieve the secret key with the specified alias
     * 
     * @param keyAlias
     *            the key's alias
     * @return the secret key associate with the specified alias
     */
    public SecretKey getSecretKey(String keyAlias) throws KeyNotFoundException;

    /**
     * Retrieve an {@link X509KeyManager} for the keystore with the specified
     * name. Implementations of IKeyManager will provide a mechanism of mapping
     * a KeyStore to an name
     * 
     * @param keystoreName
     *            the name of the keystore
     * @return an X509KeyManager for the keystore with the specified name
     */
    public X509KeyManager getCertificateKeyManager(String keystoreName);

    /**
     * Retrieve an {@link X509TrustManager} for the keystore with the specified
     * name. Implementations of IKeyManager will provide a mechanism of mapping
     * a KeyStore to an name
     * 
     * @param keystoreName
     *            the name of the keystore
     * @return an X509TrustManager for the keystore with the specified name
     */
    public X509TrustManager getCertificateTrustManager(String truststoreName);

    /**
     * Determine if key store exists within this key manager
     * 
     * @param keystoreName
     *            the name of the keystore to validate
     * @return
     */
    public boolean containsKeystore(String keystoreName);

    /**
     * Determine if this key manager contains the public key with the specified
     * alias
     * 
     * @param keyAlias
     *            the alias of the key to validate
     * @return true if the public key exists; false otherwise
     */
    public boolean containsPublicKey(String keyAlias);

    /**
     * Determine if this key manager contains the secret key with the specified
     * alias
     * 
     * @param keyAlias
     *            the alias of the key to validate
     * @return true if the secret key exists; false otherwise
     */    
    public boolean containsSecretKey(String keyAlias);

    /**
     * Add the secret key to the specified keystore
     * 
     * @param keyAlias
     *            the alias of the key to add
     * @param secretKey
     *            the key to add
     * @param keystoreName
     *            the keystore to contain the key
     * @throws IOException 
     */
    public void addSecretKey(String keyAlias, SecretKey secretKey, String keystoreName) throws IOException;

}
