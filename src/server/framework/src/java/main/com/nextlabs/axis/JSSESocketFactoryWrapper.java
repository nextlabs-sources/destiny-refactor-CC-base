/*
 * Created on Apr 12, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.axis;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Hashtable;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.axis.components.net.JSSESocketFactory;

/**
 * warp the axis JSSEScoketFactory. So I can provide my only internal socket factory.
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/axis/JSSESocketFactoryWrapper.java#1 $
 */

public class JSSESocketFactoryWrapper extends JSSESocketFactory {

    public JSSESocketFactoryWrapper(Hashtable attributes) {
        super(attributes);
    }

    @Override
    protected void initFactory() throws IOException {
        try {
            // Open the key store
            String keyStoreFileName = System.getProperty("nextlabs.javax.net.ssl.keyStore");
            char[] keyStorePassword = System.getProperty("nextlabs.javax.net.ssl.keyStorePassword").toCharArray();
            
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream keyStoreFile = new FileInputStream(keyStoreFileName);
            keyStore.load(keyStoreFile, keyStorePassword);
            keyStoreFile.close();

            // Create the key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword);
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
        
            // Open the trust store
            String trustStoreFileName = System.getProperty("nextlabs.javax.net.ssl.trustStore");
            char[] trustStorePassword = System.getProperty("nextlabs.javax.net.ssl.trustStorePassword").toCharArray();
            
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream trustStoreFile = new FileInputStream(trustStoreFileName);
            trustStore.load(trustStoreFile, trustStorePassword);
            trustStoreFile.close();

            // Create the trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, new SecureRandom());

            sslFactory = new SSLSocketFactoryWrapper(sslContext.getSocketFactory());
        } catch (KeyStoreException e) {
            throw new IOException(e);
        } catch (KeyManagementException e) {
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        } catch (UnrecoverableKeyException e) {
            throw new IOException(e);
        } catch (CertificateException e) {
            throw new IOException(e);
        }
    }
}
