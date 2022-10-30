/*
 * Created on Jan 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.security;

import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.security.IKeyManager;
import com.bluejungle.framework.security.KeyManagerImpl;
import com.mockobjects.servlet.MockHttpServletRequest;

import org.apache.axis.MessageContext;

import javax.xml.rpc.ServiceException;

import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import junit.framework.TestCase;

/**
 * This is the test class for the certificate checker
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/security/src/java/test/com/bluejungle/destiny/server/security/CertificateCheckerTest.java#2 $
 */

public class CertificateCheckerTest extends TestCase {

    private static final String BUILD_ROOT_PROPERTY_NAME = "build.root.dir";

    private IKeyManager keyManager;

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public CertificateCheckerTest(String testName) {
        super(testName);

        this.keyManager = this.initKeyManager();
    }

    public void testInvoke() throws ServiceException, UnauthorizedCallerFault, RemoteException {
        CertificateChecker checkerToTest = new CertificateChecker();
        checkerToTest.setOptions(new Hashtable());
        checkerToTest.setOption("trustedCerts", "dcc");
        
        
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        X509Certificate[] certChain = this.keyManager.getCertificateKeyManager("truststore").getCertificateChain("dcc");
        servletRequest.setupGetAttribute(certChain);

        MessageContext messageContext = new MessageContext(null);
        messageContext.setProperty("transport.http.servletRequest", servletRequest);

        
        checkerToTest.init();
        checkerToTest.invoke(messageContext);

        // Not test without providing a cert
        servletRequest = new MockHttpServletRequest();
        certChain = new X509Certificate[0];
        servletRequest.setupGetAttribute(certChain);
        messageContext.setProperty("transport.http.servletRequest", servletRequest);

        try {
            checkerToTest.invoke(messageContext);
            fail("testInvoke - Should throw UnauthorizedCallerFault for request with wrong cert");
        } catch (UnauthorizedCallerFault exception) {
        }
        
        // Test without a cert in the request
        servletRequest = new MockHttpServletRequest();
        messageContext.setProperty("transport.http.servletRequest", servletRequest);
        servletRequest.setupGetAttribute(null);
        
        checkerToTest.invoke(messageContext);
    }

    /**
     * Intialize the KeyManager.
     * 
     * @return
     * 
     */
    private IKeyManager initKeyManager() {
        String buildRoot = System.getProperty(BUILD_ROOT_PROPERTY_NAME);
        String keystoreLocation = buildRoot + "dcc-keystore.jks";
        String keystorePassword = "password";

        // Initialize the KeyManager
        HashMapConfiguration keyManagerConfiguration = new HashMapConfiguration();
        Set<KeyManagerImpl.KeystoreFileInfo> keyStoreFileInfo = new HashSet<KeyManagerImpl.KeystoreFileInfo>();
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("keystore", keystoreLocation, "jks", keystorePassword));

        String truststoreLocation = buildRoot + "dcc-truststore.jks";
        String truststorePassword = "password";
        keyStoreFileInfo.add(new KeyManagerImpl.KeystoreFileInfo("truststore", truststoreLocation, "jks", truststorePassword));
        keyManagerConfiguration.setProperty(KeyManagerImpl.KEYSTORE_FILE_INFO_PROPERTY_NAME, keyStoreFileInfo);
        ComponentInfo<IKeyManager> keyManagerComponentInfo = 
            new ComponentInfo<IKeyManager>(
                    IKeyManager.COMPONENT_NAME, 
                    KeyManagerImpl.class, 
                    IKeyManager.class, 
                    LifestyleType.SINGLETON_TYPE, 
                    keyManagerConfiguration);

        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        return manager.getComponent(keyManagerComponentInfo);
    }
}
