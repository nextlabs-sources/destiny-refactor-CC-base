/*
 * Created on Jan 9, 2005
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
import org.apache.axis.description.OperationDesc;

import javax.xml.rpc.ServiceException;

import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import junit.framework.TestCase;

/**
 * This is the test class for the API Authorization Checker module. The tests
 * verify that a given API can (or cannot) be accessed based on the certificate
 * passed to the target service.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/security/src/java/test/com/bluejungle/destiny/server/security/APIAuthCheckerTest.java#1 $
 */

public class APIAuthCheckerTest extends TestCase {

    private static final String BUILD_ROOT_PROPERTY_NAME = "build.root.dir";

    private IKeyManager keyManager;

    /**
     * Constructor
     */
    public APIAuthCheckerTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public APIAuthCheckerTest(String testName) {
        super(testName);

        this.keyManager = this.initKeyManager();
    }

    public void testInvoke() throws ServiceException, UnauthorizedCallerFault, RemoteException {
        // Test for authorized caller
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        X509Certificate[] certChain = this.keyManager.getCertificateKeyManager("keystore").getCertificateChain("dcc");
        servletRequest.setupGetAttribute(certChain);

        MessageContext messageContext = new MessageContext(null);
        messageContext.setProperty("transport.http.servletRequest", servletRequest);

        APIAuthChecker checkerToTest = new APIAuthChecker();
        checkerToTest.setOptions(new Hashtable());
        checkerToTest.setOption("dcc", "foo");
        checkerToTest.init();
        OperationDesc operation = new OperationDesc();
        operation.setName("foo");
        messageContext.setOperation(operation);
        checkerToTest.invoke(messageContext);

        // Test with an unauthorized method
        operation = new OperationDesc();
        operation.setName("bar");
        messageContext.setOperation(operation);
        servletRequest.setupGetAttribute(certChain);
        try {
            checkerToTest.invoke(messageContext);
            fail("testInvoke - Should throw UnauthorizedCallerFault for request with wrong method");
        } catch (UnauthorizedCallerFault exception) {
        }

        // Test with no cert - mimics no https
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
        String keystoreLocation = buildRoot + "\\dcc-keystore.jks";
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
