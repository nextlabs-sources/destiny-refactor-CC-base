/*
 * Created on Apr 02, 2014
 *
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/framework/plugins/rmi/socketfactory/ClientSocketFactory.java#1 $:
 */

package com.nextlabs.framework.plugins.rmi.socketfactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;

/**
 * An implementation of the SslRMIClientSocketFactory that allows the
 * keystore and truststore to be defined by arguments rather than read
 * from properties. This allows different RMI plug-ins to have their
 * own key/trust definitions.
 *
 * @see RMIService#Constructor(int, RMIClientSocketFactory, RMIServerSocketFactory)
 */
public class ClientSocketFactory extends SslRMIClientSocketFactory {
    private String keyStoreFileName = null;
    private char[] keyStorePassword = null;
    private String trustStoreFileName = null;
    private char[] trustStorePassword = null;
    private SSLSocketFactory clientSocketFactory = null;

    public ClientSocketFactory(String keyStoreFileName, String keyStorePassword, String trustStoreFileName, String trustStorePassword) {
        super();
        this.keyStoreFileName = keyStoreFileName;
        this.keyStorePassword = keyStorePassword == null ? null : keyStorePassword.toCharArray();
        this.trustStoreFileName = trustStoreFileName;
        this.trustStorePassword = trustStorePassword == null ? null : trustStorePassword.toCharArray();
    }

    synchronized private SSLSocketFactory getClientSocketFactory() throws SocketFactoryException {
        if (clientSocketFactory == null) {
            try {
                KeyManager[] keyManagers = null;
                
                if (keyStoreFileName != null) {
                    // init keystore
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    FileInputStream keyStoreFile = new FileInputStream(keyStoreFileName);
                    keyStore.load(keyStoreFile, keyStorePassword);
                    keyStoreFile.close();

                    // Key Manager
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    keyManagerFactory.init(keyStore, keyStorePassword);
                    keyManagers = keyManagerFactory.getKeyManagers();
                }

                TrustManager[] trustManagers = null;

                if (trustStoreFileName != null) {
                    // init truststore
                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    FileInputStream trustStoreFile = new FileInputStream(trustStoreFileName);
                    trustStore.load(trustStoreFile, trustStorePassword);
                    trustStoreFile.close();
                    
                    // Trust Manager
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(trustStore);
                    trustManagers = trustManagerFactory.getTrustManagers();
                }

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(keyManagers, trustManagers, new SecureRandom());

                clientSocketFactory = sslContext.getSocketFactory();
            } catch (FileNotFoundException e) {
                throw new SocketFactoryException(e);
            } catch (IOException e) {
                throw new SocketFactoryException(e);
            } catch (KeyStoreException e) {
                throw new SocketFactoryException(e);
            } catch (KeyManagementException e) {
                throw new SocketFactoryException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new SocketFactoryException(e);
            } catch (UnrecoverableKeyException e) {
                throw new SocketFactoryException(e);
            } catch (CertificateException e) {
                throw new SocketFactoryException(e);
            }
        }

        return clientSocketFactory;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        try {
            return getClientSocketFactory().createSocket(host, port);
        } catch (SocketFactoryException e) {
            throw new IOException(e);
        }
    }
}
