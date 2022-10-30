/*
 * Created on Mar 1, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.common;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.commons.logging.Log;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/common/src/java/main/com/bluejungle/destiny/tools/common/SecureClientSocketFactory.java#1 $
 */
public class SecureClientSocketFactory implements ISecureClientSocketFactory {

    private static final String DEFAULT_PASSWORD = "password";
    private static final String SSL_CONTEXT_NAME = "TLS";
    private static final TrustManager TRUST_ALL_MANAGER = new TrustAllManager();
    private static final String DEFAULT_KEYSTORE_NAME = "keystore.jks";

    private SSLSocketFactory ssf;
    private Log log = LogFactory.getLog(SecureClientSocketFactory.class.getName());

    /**
     * Constructor
     * 
     * @param params
     *            hashtable of parameters
     */
    public SecureClientSocketFactory(Hashtable params) {
        try {
            setupSocketFactory();
        } catch (KeyManagementException e) {
            getLog().error("Unable to initialize Socket Factory. Connection to server could not be established.");
        } catch (NoSuchAlgorithmException e) {
            getLog().error("Unable to initialize Socket Factory. Connection to server could not be established.");
        }
    }

    /**
     * @see org.apache.axis.components.net.SocketFactory#create(java.lang.String,
     *      int, java.lang.StringBuffer,
     *      org.apache.axis.components.net.BooleanHolder)
     */
    public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception {
        return (SSLSocket) this.ssf.createSocket(host, port);
    }

    /**
     * Returns the keystore name
     * 
     * @return the keystore name
     */
    public String getKeystoreName() {
        return DEFAULT_KEYSTORE_NAME;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Return the default password for the keystore
     * 
     * @return the default password for the keystore
     */
    public String getPassword() {
        return DEFAULT_PASSWORD;
    }

    /**
     * sets up the socket factory.
     * 
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private void setupSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        KeyManager[] kms = { new SecureKeyManager(SecureKeyManager.DEFAULT_ALGORITHM, SecureKeyManager.DEFAULT_FACTORY_ALGORITHM, getKeystoreName(), getPassword()) };

        //Place our own Trust Manager
        TrustManager[] tms = { TRUST_ALL_MANAGER };
        SSLContext context = null;
        context = SSLContext.getInstance(SSL_CONTEXT_NAME);
        context.init(kms, tms, null);

        //Finally, we get a SocketFactory, and pass it to SimpleSSLClient.
        this.ssf = context.getSocketFactory();
    }
}
