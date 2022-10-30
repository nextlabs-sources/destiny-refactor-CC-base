/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.service;

import java.rmi.RemoteException;

import javax.xml.soap.SOAPException;

import junit.framework.TestCase;

import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPHeaderElement;

import com.bluejungle.destiny.interfaces.secure_session.v1.SecureSessionServiceIF;
import com.bluejungle.destiny.services.secure_session.v1.SecureSessionServiceLocator;
import com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_112/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/securesession/service/SecureSessionServiceTest.java#1 $
 */

public class SecureSessionServiceTest extends TestCase {

    private static final String WS_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    private SecureSessionServiceIF secureSessionServiceIF;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SecureSessionServiceTest.class);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        SecureSessionServiceLocator secureSessionServiceLocator = new SecureSessionServiceLocator();
        secureSessionServiceLocator.setSecureSessionServiceEndpointAddress("http://localhost:8081/dac/services/SecureSessionService");
        this.secureSessionServiceIF = secureSessionServiceLocator.getSecureSessionService();
    }

    /**
     * Constructor for SecureSessionServiceTest.
     * 
     * @param testName
     *            name of the test
     */
    public SecureSessionServiceTest(String testName) {
        super(testName);
    }

    public void testInitSession() throws RemoteException, SOAPException {
        // First try without authentication header
        AccessDeniedFault expectedException = null;
        try {
            this.secureSessionServiceIF.initSession();
            fail("Should throw RemoteException.");
        } catch (AccessDeniedFault exception) {
            expectedException = exception;
        }
        assertNotNull("testInitSession - Ensure exception thrown when auth information is not provided.", expectedException);

        // Now, provide bad auth
        setAuthHeader("basename", "basepassword");
        expectedException = null;
        try {
            this.secureSessionServiceIF.initSession();
            fail("Should throw RemoteException.");
        } catch (AccessDeniedFault exception) {
            expectedException = exception;
        }
        assertNotNull("testInitSession - Ensure exception thrown when auth information is bad.", expectedException);

        // Now, try with good auth
        final String expectedPrincipalName = "Administrator@Local";
        setAuthHeader("Administrator", "123blue!");
        SecureSession secureSession = this.secureSessionServiceIF.initSession();
        assertNotNull("testInitSession - Ensure retrieved secure session init info is not null.", secureSession);
    }

    /**
     * @param string
     * @param string2
     * @throws SOAPException
     */
    private void setAuthHeader(String username, String password) throws SOAPException {
        ((Stub) this.secureSessionServiceIF).clearHeaders();

        SOAPHeaderElement usernameSoapHeaderElement = new SOAPHeaderElement(WS_SECURITY_NAMESPACE, "Username", username);
        SOAPHeaderElement passwordSoapHeaderElement = new SOAPHeaderElement(WS_SECURITY_NAMESPACE, "Password", password);
        SOAPHeaderElement usernameTokenHeaderElement = new SOAPHeaderElement(WS_SECURITY_NAMESPACE, "UsernameToken");
        usernameTokenHeaderElement.addChild(usernameSoapHeaderElement);
        usernameTokenHeaderElement.addChild(passwordSoapHeaderElement);

        SOAPHeaderElement wsSecurityHeaderElement = new SOAPHeaderElement(WS_SECURITY_NAMESPACE, "Security");
        wsSecurityHeaderElement.setActor(null);
        wsSecurityHeaderElement.addChild(usernameTokenHeaderElement);

        ((Stub) this.secureSessionServiceIF).setHeader(wsSecurityHeaderElement);
    }

}