package com.bluejungle.pf.destiny.services;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $ Id: $
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.SecureSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.bluejungle.framework.crypt.ReversibleEncryptor;

/**
 * This is a socket factory for the Policy Editor.
 */
public class PolicyEditorSocketFactory implements SecureSocketFactory {
	
    private static final char[] KEYSTORE_PASSWORD = "password".toCharArray();
    private static final String ENCRYPTED_PASSWORD_FILE_PATH = "/security/config.dat";
	
	private static final String DEFAULT_ALGORITHM = "jks";
    private static final String FACTORY_ALGORITHM = "SunX509";
    private static final String KEYSTORE_RESOURCE = "security/policyAuthor-keystore.jks";

    private SSLSocketFactory ssf;
    private Log log = LogFactory.getLog(PolicyEditorSocketFactory.class.getName());

    public PolicyEditorSocketFactory( Hashtable params ) {
        try {
            setupSocketFactory();
        } catch (KeyManagementException e) {
            this.log.error("Unable to initialize Socket Factory. Connection to server could not be established.");
        } catch (NoSuchAlgorithmException e) {
            this.log.error("Unable to initialize Socket Factory. Connection to server could not be established.");
        }
    }
 
    
    /**
     * Reads password from file.
     * 
     * @return password
     */
    private char[] getKeystorePassword() {
    	String eclipseLauncher = System.getProperty("eclipse.launcher");
    	if (eclipseLauncher == null)
    		return KEYSTORE_PASSWORD;
    	
    	String passwordFilePath = new File(eclipseLauncher).getParent() + ENCRYPTED_PASSWORD_FILE_PATH;       
        String password = null;
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(passwordFilePath));
            password = fileReader.readLine();

            ReversibleEncryptor decryptor = new ReversibleEncryptor();
            try {
                password = decryptor.decrypt(password);
            } catch (Exception e) {
            	return KEYSTORE_PASSWORD;
            }
        } catch (FileNotFoundException e) {
        	return KEYSTORE_PASSWORD;
        } catch (IOException e) {
        	return KEYSTORE_PASSWORD;
        }
        return password.toCharArray();
    }

    
    /**
     * @see org.apache.axis.components.net.SocketFactory#create(java.lang.String, int, java.lang.StringBuffer, org.apache.axis.components.net.BooleanHolder)
     */
    public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception {
        return (SSLSocket) this.ssf.createSocket(host, port);
    }

    /**
     * sets up the socket factory. 
     * 
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private void setupSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        KeyManager[] kms = { KEY_MANAGER };

        //Place our own Trust Manager
        TrustManager[] tms = { TRUST_MANAGER };
        SSLContext context = null;
        context = SSLContext.getInstance("TLS");
        context.init(kms, tms, null);

        //Finally, we get a SocketFactory, and pass it to SimpleSSLClient.
        this.ssf = context.getSocketFactory();
    }

    private static X509TrustManager TRUST_MANAGER = new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }
    };

    private X509KeyManager KEY_MANAGER = new X509KeyManager() {
        private X509KeyManager realKeyManager = null;

        {
            KeyManagerFactory kmFact = null;
            try {
                //Get the key manager
                kmFact = KeyManagerFactory.getInstance(FACTORY_ALGORITHM);
                // Next, set up the KeyStore to use. We need to load the file into
                // a KeyStore instance.
                KeyStore ks = KeyStore.getInstance(DEFAULT_ALGORITHM);
            	String eclipseLauncher = System.getProperty("eclipse.launcher");
            	InputStream input = null;
            	if (eclipseLauncher == null) {
            		// Fallback to original method, which looks in the class-path jar file.
            		input = getClass().getClassLoader().getResourceAsStream( KEYSTORE_RESOURCE );
            	} else {            	
            		String keystoreFilePath = new File(eclipseLauncher).getParent() + "/" + KEYSTORE_RESOURCE;    
            		input = new FileInputStream(keystoreFilePath);
            	}       
            	
                char[] keyStorePassword = getKeystorePassword();
                try {
                    ks.load(input, keyStorePassword );
                } finally {
                    input.close();
                }
                kmFact.init(ks, keyStorePassword );
            } catch (KeyStoreException e2) {
                log.error("Key store cannot be loaded. Cannot connect to server.", e2);
            } catch (NoSuchAlgorithmException e3) {
                log.error("Key store cannot be loaded (No Such Algorithm). Cannot connect to server.", e3);
            } catch (CertificateException e3) {
                log.error("Key store cannot be loaded. Cannot connect to server.", e3);
            } catch (IOException e3) {
                log.error("Key store cannot be loaded. Cannot connect to server.", e3);
            } catch (UnrecoverableKeyException e) {
                log.error("Key store cannot be loaded. Cannot connect to server.", e);
            }

            if ( kmFact != null ) {
                // And now get the KeyManager
                KeyManager[] kms = kmFact.getKeyManagers();
                this.realKeyManager = (X509KeyManager) kms[0];
            }
        }
        /**
         * @see javax.net.ssl.X509KeyManager#getPrivateKey(java.lang.String)
         */
        public synchronized PrivateKey getPrivateKey(String arg0) {
            return this.realKeyManager.getPrivateKey(arg0);
        }

        /**
         * @see javax.net.ssl.X509KeyManager#getCertificateChain(java.lang.String)
         */
        public synchronized X509Certificate[] getCertificateChain(String arg0) {
            return this.realKeyManager.getCertificateChain(arg0);
        }

        /**
         * @see javax.net.ssl.X509KeyManager#getClientAliases(java.lang.String,
         *      java.security.Principal[])
         */
        public synchronized String[] getClientAliases(String arg0, Principal[] arg1) {
            return this.realKeyManager.getClientAliases(arg0, arg1);
        }

        /**
         * @see javax.net.ssl.X509KeyManager#getServerAliases(java.lang.String,
         *      java.security.Principal[])
         */
        public synchronized String[] getServerAliases(String arg0, Principal[] arg1) {
            return this.realKeyManager.getServerAliases(arg0, arg1);
        }

        /**
         * @see javax.net.ssl.X509KeyManager#chooseServerAlias(java.lang.String,
         *      java.security.Principal[], java.net.Socket)
         */
        public synchronized String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
            return this.realKeyManager.chooseServerAlias(arg0, arg1, arg2);
        }

        /**
         * @see javax.net.ssl.X509KeyManager#chooseClientAlias(java.lang.String[],
         *      java.security.Principal[], java.net.Socket)
         */
        public synchronized String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
            return this.realKeyManager.chooseClientAlias(arg0, arg1, arg2);
        }

    };

}
