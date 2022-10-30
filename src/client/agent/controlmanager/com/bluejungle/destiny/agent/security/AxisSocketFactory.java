/*
 * Created on Feb 4, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.security;

import com.bluejungle.destiny.agent.controlmanager.IAgentKeyManagerComponentBuilder;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.security.IKeyManager;

import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.SecureSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class AxisSocketFactory implements SecureSocketFactory, ILogEnabled {

    public static final String NAME = AxisSocketFactory.class.getName();
    public static final ComponentInfo<AxisSocketFactory> COMP_INFO =
        new ComponentInfo<AxisSocketFactory>(
            AxisSocketFactory.NAME, 
            AxisSocketFactory.class,
            LifestyleType.SINGLETON_TYPE);

    private SSLSocketFactory ssf = null;
    private Log log = null;

    private static AxisSocketFactory instance = null;

    /**
     * Constructor
     *  
     */
    public AxisSocketFactory(Hashtable params) {
        this.log = LogFactory.getLog(AxisSocketFactory.class.getName());

        try {
            setupSocketFactory();
        } catch (KeyManagementException e) {
            this.log.error("Unable to initialize Socket Factory. Connection to server could not be established.", e);
        } catch (NoSuchAlgorithmException e) {
            this.log.error("Unable to initialize Socket Factory. Connection to server could not be established.", e);
        }

        AxisSocketFactory.instance = this;
    }

    /**
     * @see org.apache.axis.components.net.SocketFactory#create(java.lang.String,
     *      int, java.lang.StringBuffer,
     *      org.apache.axis.components.net.BooleanHolder)
     */
    public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception {
        return this.ssf.createSocket(host, port);
    }

    /**
     * Calls key manager and trust manager to load the new keys.
     */
    public void reloadKeyStore() {
        try {
            setupSocketFactory(); // this is needed for the new keys to take effect.
        } catch (KeyManagementException e) {
            this.log.error("Unable to initialize Socket Factory. Connection to server could not be established.", e);
        } catch (NoSuchAlgorithmException e) {
            this.log.error("Unable to initialize Socket Factory. Connection to server could not be established.", e);
        }
    }

    /**
     * sets up the socket factory. 
     * 
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private void setupSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IKeyManager keyManager = (IKeyManager) componentManager.getComponent(IKeyManager.COMPONENT_NAME);
        KeyManager[] kms = { keyManager.getCertificateKeyManager(IAgentKeyManagerComponentBuilder.KEYSTORE_NAME) };

        TrustManager trustManager = null;
        if (keyManager.containsKeystore(IAgentKeyManagerComponentBuilder.TRUSTSTORE_NAME)) {
            trustManager = keyManager.getCertificateTrustManager(IAgentKeyManagerComponentBuilder.TRUSTSTORE_NAME);
        } else {
            trustManager = new EmptyTrustManager();
        }
        //Place our own Trust Manager
        TrustManager[] tms = { trustManager };
        SSLContext context = null;
        context = SSLContext.getInstance("TLS");
        context.init(kms, tms, null);

        //Finally, we get a SocketFactory, and pass it to SimpleSSLClient.
        this.ssf = context.getSocketFactory();
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Returns the instance.
     * 
     * @return the instance.
     */
    public static AxisSocketFactory getInstance() {
        return AxisSocketFactory.instance;
    }
}
