/*
 * Created on May 26, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/ssl/ConfigurableSSLSocketFactory.java#1 $:
 */

package com.nextlabs.framework.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ConfigurableSSLSocketFactory extends SSLSocketFactory {
    private SSLContext sslContext = null;
    private String keyStoreFileName = null;
    private char[] keyStorePassword = null;
    private String trustStoreFileName = null;
    private char[] trustStorePassword = null;
    
    public ConfigurableSSLSocketFactory() {
        useDefaultKeyStore();
        useDefaultTrustStore();
    }

    public void useDefaultKeyStore() {
        keyStoreFileName = System.getProperty("nextlabs.javax.net.ssl.keyStore");
        keyStorePassword = System.getProperty("nextlabs.javax.net.ssl.keyStorePassword").toCharArray();
    }

    public void setKeyStore(String keyStoreFileName, String keyStorePassword) {
        this.keyStoreFileName = keyStoreFileName;
        this.keyStorePassword = keyStorePassword.toCharArray();
    }
    
    public void useDefaultTrustStore() {
        trustStoreFileName = System.getProperty("nextlabs.javax.net.ssl.trustStore");
        trustStorePassword = System.getProperty("nextlabs.javax.net.ssl.trustStorePassword").toCharArray();
    }

    public void setTrustStore(String trustStoreFileName, String trustStorePassword) {
        this.trustStoreFileName = trustStoreFileName;
        this.trustStorePassword = trustStorePassword.toCharArray();
    }

    public void removeTrustStore() {
        this.trustStoreFileName = null;
        this.trustStorePassword = null;
    }
    
    public void init() throws IOException {
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(getKeyManagers(), getTrustManagers(), new SecureRandom());
        } catch (CertificateException e) {
            throw new IOException(e);
        } catch (KeyManagementException e) {
            throw new IOException(e);
        } catch (KeyStoreException e) {
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        } catch (UnrecoverableKeyException e) {
            throw new IOException(e);
        }
    }

    private KeyManager[] getKeyManagers() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream keyStoreFile = new FileInputStream(keyStoreFileName);
        keyStore.load(keyStoreFile, keyStorePassword);
        keyStoreFile.close();

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword);
        return keyManagerFactory.getKeyManagers();
    }
    
    private TrustManager[] getTrustManagers() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        if (trustStoreFileName == null) {
            return getPermissiveTrustManagers();
        } else {
            return getRegularTrustManagers(trustStoreFileName, trustStorePassword);
        }
        
    }
    
    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return sslContext.getSocketFactory().createSocket(s, host, port, autoClose);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int port) throws IOException {
        return sslContext.getSocketFactory().createSocket(inetAddress, port);
    }
    
    @Override
    public Socket createSocket(InetAddress inetAddress, int port, InetAddress localAddress, int localPort) throws IOException {
        return sslContext.getSocketFactory().createSocket(inetAddress, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
        return sslContext.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws UnknownHostException, IOException {
        return sslContext.getSocketFactory().createSocket(host, port, localHost, localPort);
    }
    
    @Override
    public String[] getDefaultCipherSuites() {
        return sslContext.getSocketFactory().getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslContext.getSocketFactory().getSupportedCipherSuites();
    }
    
    private TrustManager[] getPermissiveTrustManagers() {
        TrustManager permissiveTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        return new TrustManager[] { permissiveTrustManager };
    }

    private TrustManager[] getRegularTrustManagers(String trustStoreFileName, char[] trustStorePassword) throws NoSuchAlgorithmException, KeyStoreException, FileNotFoundException, IOException, CertificateException {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        FileInputStream trustStoreFile = new FileInputStream(trustStoreFileName);
        trustStore.load(trustStoreFile, trustStorePassword);
        trustStoreFile.close();
        
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }
}
