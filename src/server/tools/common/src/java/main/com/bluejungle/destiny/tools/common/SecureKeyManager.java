/*
 * Created on Mar 1, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;

import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/common/src/java/main/com/bluejungle/destiny/tools/common/SecureKeyManager.java#1 $
 */

class SecureKeyManager implements X509KeyManager {

    /**
     * Default constructor values
     */
    public static final String DEFAULT_ALGORITHM = "jks";
    public static final String DEFAULT_FACTORY_ALGORITHM = "SunX509";
    
    private String algorithm;
    private String factoryAlgorithm;
    private String keystore;
    private String password;
    private X509KeyManager realKeyManager = null;

    private Log log = LogFactory.getLog(SecureKeyManager.class.getName());

    /**
     * Constructor
     * 
     * @param algorithm
     *            algorithm to use
     * @param factoryAlg
     *            factory algorithm
     * @param password
     *            keystore password
     */
    public SecureKeyManager(String algorithm, String factoryAlg, String keystoreLocation, String password) {
        super();
        this.algorithm = algorithm;
        this.factoryAlgorithm = factoryAlg;
        this.keystore = keystoreLocation;
        this.password = password;
        init();
    }

    /**
     * @see javax.net.ssl.X509KeyManager#chooseClientAlias(java.lang.String[],
     *      java.security.Principal[], java.net.Socket)
     */
    public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
        return this.realKeyManager.chooseClientAlias(arg0, arg1, arg2);
    }

    /**
     * @see javax.net.ssl.X509KeyManager#chooseServerAlias(java.lang.String,
     *      java.security.Principal[], java.net.Socket)
     */
    public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
        return this.realKeyManager.chooseServerAlias(arg0, arg1, arg2);
    }

    /**
     * Returns the algorithm used
     * 
     * @return the algorithm used
     */
    protected String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Returns the factory algorithm
     * 
     * @return the factory algorithm
     */
    protected String getFactoryAlgorithm() {
        return this.factoryAlgorithm;
    }

    /**
     * @see javax.net.ssl.X509KeyManager#getCertificateChain(java.lang.String)
     */
    public X509Certificate[] getCertificateChain(String alias) {
        return this.realKeyManager.getCertificateChain(alias);
    }

    /**
     * @see javax.net.ssl.X509KeyManager#getClientAliases(java.lang.String,
     *      java.security.Principal[])
     */
    public String[] getClientAliases(String arg0, Principal[] arg1) {
        return this.realKeyManager.getClientAliases(arg0, arg1);
    }

    /**
     * Returns the keystore location
     * 
     * @return the keystore location
     */
    protected String getKeystoreLocation() {
        return this.keystore;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return this.log;
    }

    /**
     * Return the password
     * 
     * @return the password
     */
    protected String getPassword() {
        return this.password;
    }

    /**
     * @see javax.net.ssl.X509KeyManager#getPrivateKey(java.lang.String)
     */
    public PrivateKey getPrivateKey(String alias) {
        return this.realKeyManager.getPrivateKey(alias);
    }

    /**
     * @see javax.net.ssl.X509KeyManager#getServerAliases(java.lang.String,
     *      java.security.Principal[])
     */
    public String[] getServerAliases(String arg0, Principal[] arg1) {
        return this.realKeyManager.getServerAliases(arg0, arg1);
    }

    protected void init() {
        KeyManagerFactory kmFact = null;
        try {
            //Get the key manager
            kmFact = KeyManagerFactory.getInstance(getFactoryAlgorithm());
            // Next, set up the KeyStore to use. We need to load the file
            // into
            // a KeyStore instance.
            KeyStore ks = KeyStore.getInstance(getAlgorithm());
            InputStream input = new FileInputStream(getKeystoreLocation());
            //InputStream input = getClass().getClassLoader().getResourceAsStream(getKeystoreLocation());
            try {
                ks.load(input, getPassword().toCharArray());
            } finally {
                input.close();
            }
            kmFact.init(ks, getPassword().toCharArray());
        } catch (KeyStoreException e2) {
            getLog().error("Key store cannot be loaded. Cannot connect to server.", e2);
        } catch (NoSuchAlgorithmException e3) {
            getLog().error("Key store cannot be loaded (No Such Algorithm). Cannot connect to server.", e3);
        } catch (CertificateException e3) {
            getLog().error("Key store cannot be loaded. Cannot connect to server.", e3);
        } catch (IOException e3) {
            getLog().error("Key store cannot be loaded. Cannot connect to server.", e3);
        } catch (UnrecoverableKeyException e) {
            getLog().error("Key store cannot be loaded. Cannot connect to server.", e);
        }

        if (kmFact != null) {
            // And now get the KeyManager
            KeyManager[] kms = kmFact.getKeyManagers();
            this.realKeyManager = (X509KeyManager) kms[0];
        }
    }
}
