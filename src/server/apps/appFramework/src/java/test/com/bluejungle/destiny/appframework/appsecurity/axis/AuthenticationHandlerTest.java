/*
 * Created on Mar 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.axis;

import java.rmi.RemoteException;

import org.apache.axis.EngineConfigurationFactory;
import com.bluejungle.destiny.appframework.appsecurity.test.MockSecureSessionVault;
import com.bluejungle.destiny.client.security.AxisSecureClientConfigurationFactory;
import com.bluejungle.destiny.interfaces.secure_session.v1.SecureSessionServiceIF;
import com.bluejungle.destiny.services.secure_session.v1.SecureSessionServiceLocator;
import com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

import junit.framework.TestCase;

/**
 * Test for AuthenticationHandler
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/apps/inquiryCenter/src/java/test/com/bluejungle/destiny/inquirycenter/security/axis/AuthenticationHandlerTest.java#1 $
 */

public class AuthenticationHandlerTest extends TestCase {
    
    private static final String GOOD_USER_NAME = "Administrator";
    private static final String GOOD_PASSWORD = "123blue!";

    private SecureSessionServiceIF secureSessionServiceIF;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        // TEMP - FIX ME
        System.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME, AxisSecureClientConfigurationFactory.class.getName());
        
        SecureSessionVaultGateway.setSecureSessionVault(new MockSecureSessionVault());
        
        /*
         * FIX ME - Not the ideal service to use for testing.  Change when other services are implemented on DAC
         */
        SecureSessionServiceLocator secureSessionServiceLocator = new SecureSessionServiceLocator();
        secureSessionServiceLocator.setSecureSessionServiceEndpointAddress("http://localhost:8081/dac/services/SecureSessionService");
        this.secureSessionServiceIF = secureSessionServiceLocator.getSecureSessionService();       
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for AuthenticationHandlerTest.
     * 
     * @param testName
     */
    public AuthenticationHandlerTest(String testName) {
        super(testName);
    }

    public void testAuthenticationHandler() throws RemoteException {
        // Test with the Inquiry Alert Service

        // Test with no user
        AccessDeniedFault exceptionCaught = null;
        try {
            this.secureSessionServiceIF.initSession();
        } catch (AccessDeniedFault exception) {
            exceptionCaught = exception;
        }
        assertNotNull("Ensure authentication without user failed", exceptionCaught);

        // Test with bogus user
        AuthenticationContext authContext = AuthenticationContext.getCurrentContext();
        authContext.setUsername("foo");
        authContext.setPassword("foo");

        exceptionCaught = null;
        try {
            this.secureSessionServiceIF.initSession();
        } catch (AccessDeniedFault exception) {
            exceptionCaught = exception;
        }
        assertNotNull("Ensure authentication without bogus user failed", exceptionCaught);

        authContext = AuthenticationContext.getCurrentContext();
        authContext.setUsername(GOOD_USER_NAME);
        authContext.setPassword(GOOD_PASSWORD);
        this.secureSessionServiceIF.initSession();
        
        // Ensure that the session key is not null
        SecureSession sessionKey = SecureSessionVaultGateway.getSecureSession();
        assertNotNull("Ensure session key is not null", sessionKey);
        
        // Test now without an authentication context and make sure the session is active
        AuthenticationContext.clearCurrentContext();        
        this.secureSessionServiceIF.initSession();
        
        // Clear the session and try again
        SecureSessionVaultGateway.clearSecureSession();
        exceptionCaught = null;
        try {
            this.secureSessionServiceIF.initSession();
        } catch (AccessDeniedFault exception) {
            exceptionCaught = exception;
        }
        assertNotNull("Ensure security check bounces user when no session or username/password is specified", exceptionCaught);
    }
}