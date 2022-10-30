/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote;

import org.apache.axis.EngineConfigurationFactory;

import junit.framework.TestCase;

//import org.apache.axis.EngineConfigurationFactory;

import com.bluejungle.destiny.appframework.appsecurity.axis.SecureSessionVaultGateway;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoggedInUser;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginMgr;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginInfoImpl;
import com.bluejungle.destiny.appframework.appsecurity.test.MockSecureSessionVault;
import com.bluejungle.destiny.client.security.AxisSecureClientConfigurationFactory;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/apps/webFramework/src/java/test/com/bluejungle/destiny/webui/framework/loginmgr/remote/RemoteLoginManagerTest.java#3 $
 */

public class RemoteLoginManagerTest extends TestCase {

    /* Hard coded for the moment */

    private static final String GOOD_USER_NAME = "Administrator";
    private static final String GOOD_PASSWORD = "123blue!";

    private RemoteLoginManager loginManagerToTest;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(RemoteLoginManagerTest.class);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        // TEMP - FIX ME
        System.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME, AxisSecureClientConfigurationFactory.class.getName());

        SecureSessionVaultGateway.setSecureSessionVault(new MockSecureSessionVault());

        String dacLocation = "http://localhost:8081/dac/services/SecureSessionService";

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();

        //Sets the remote login manager to test
        HashMapConfiguration componentConfig = new HashMapConfiguration();
        componentConfig.setProperty(RemoteLoginManager.SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME, dacLocation);
        ComponentInfo componentInfo = new ComponentInfo(ILoginMgr.COMP_NAME, RemoteLoginManager.class.getName(), ILoginMgr.class.getName(), LifestyleType.SINGLETON_TYPE, componentConfig);
        loginManagerToTest = (RemoteLoginManager) componentManager.getComponent(componentInfo);
    }

    /**
     * Constructor for RemoteLoginManagerTest.
     * 
     * @param testName
     */
    public RemoteLoginManagerTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies if the isLoginValid API works properly
     * 
     * @throws LoginException
     */
    public void testLogin() {
        // First try bad login info
        LoginInfoImpl loginInfo = new LoginInfoImpl();
        loginInfo.setUserName("baduser");
        loginInfo.setPassword("badpas");

        boolean isValid = true;
        try {
            loginManagerToTest.login(loginInfo);
        } catch (LoginException e) {
            isValid = false;
        }
        assertTrue("testLogin - Ensure bad username and password is not valid", !isValid);
        assertNull("testLogin - Ensure secure session is not present.", SecureSessionVaultGateway.getSecureSession());

        // Now test with proper credentials
        loginInfo.setUserName(GOOD_USER_NAME);
        loginInfo.setPassword(GOOD_PASSWORD);

        isValid = true;
        try {
            loginManagerToTest.login(loginInfo);
        } catch (LoginException e) {
            isValid = false;
        }

        assertTrue("testLogin - Ensure correct username and password is valid", isValid);
        assertNotNull("testLogin - Ensure secure session is present.", SecureSessionVaultGateway.getSecureSession());

        // Test null pointers
        NullPointerException expectedException = null;
        try {
            loginManagerToTest.login(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException exception) {
            expectedException = exception;
        } catch (LoginException e) {
            fail("Should not throw LoginException");
        }
        assertNotNull("testLogin - Ensure NullPointerException was thrown for null username", expectedException);
    }

    /**
     * This test verifies if the isLoginValid API works properly
     * 
     * @throws LoginException
     */
    public void testRemoteLoginMgrLoginFunction() {
        // First try bad login info
        LoginInfoImpl loginInfo = new LoginInfoImpl();
        loginInfo.setUserName("baduser");
        loginInfo.setPassword("badpas");

        ILoggedInUser user = null;
        boolean exThrown = false;
        try {
            user = loginManagerToTest.login(loginInfo);
        } catch (LoginException e) {
            exThrown = true;
        }
        assertTrue("Ensure bad username and password is not valid", exThrown);
        assertNull("No logged in user should be returned", user);
        assertNull("No secure session should be created", SecureSessionVaultGateway.getSecureSession());

        // Now test with proper credentials
        loginInfo.setUserName(GOOD_USER_NAME);
        loginInfo.setPassword(GOOD_PASSWORD);
        exThrown = false;

        try {
            user = loginManagerToTest.login(loginInfo);
        } catch (LoginException e) {
            exThrown = true;
        }

        assertFalse("Correct username and password should be granted access", exThrown);
        assertNotNull("Secure session shoudl be created", SecureSessionVaultGateway.getSecureSession());
        assertNotNull("Logged in user should be returned", user);
    }

    /**
     * This test verifies that bad login arguments are rejected properly
     */
    public void testRemoteLoginMgrDeniedLoginArguments() {
        //Try null user name
        final LoginInfoImpl loginInfo = new LoginInfoImpl();
        loginInfo.setUserName(null);
        loginInfo.setPassword(GOOD_PASSWORD);

        ILoggedInUser user = null;
        boolean exThrown = false;
        try {
            user = loginManagerToTest.login(loginInfo);
        } catch (LoginException e) {
            exThrown = true;
        }
        assertTrue("Ensure null username is not valid", exThrown);
        assertNull("No logged in user should be returned", user);
        assertNull("No secure session should be created", SecureSessionVaultGateway.getSecureSession());

        //Try null password
        exThrown = false;
        loginInfo.setUserName(GOOD_USER_NAME);
        loginInfo.setPassword(null);
        try {
            user = loginManagerToTest.login(loginInfo);
        } catch (LoginException e) {
            exThrown = true;
        }
        assertTrue("Ensure null password is not valid", exThrown);
        assertNull("No logged in user should be returned", user);
        assertNull("No secure session should be created", SecureSessionVaultGateway.getSecureSession());

        //Try both null password and null username
        exThrown = false;
        loginInfo.setUserName(null);
        loginInfo.setPassword(null);
        try {
            user = loginManagerToTest.login(loginInfo);
        } catch (LoginException e) {
            exThrown = true;
        }
        assertTrue("Ensure null password is not valid", exThrown);
        assertNull("No logged in user should be returned", user);
        assertNull("No secure session should be created", SecureSessionVaultGateway.getSecureSession());

        //Try both null loginInfo
        exThrown = false;
        try {
            user = loginManagerToTest.login(null);
        } catch (LoginException e) {
            fail("Login exception should not be thrown");
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Ensure null password is not valid", exThrown);
        assertNull("No logged in user should be returned", user);
        assertNull("No secure session should be created", SecureSessionVaultGateway.getSecureSession());
    }
}