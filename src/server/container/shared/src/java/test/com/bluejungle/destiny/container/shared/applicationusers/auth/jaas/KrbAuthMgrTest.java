/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.auth.jaas;

import java.security.Principal;
import java.util.Properties;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext;
import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * This is the test class for the kerberos authenticaton manager class
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/auth/test/KrbAuthMgrTest.java#3 $
 */

public class KrbAuthMgrTest extends BaseDestinyTestCase {

    /**
     * Constructor
     */
    public KrbAuthMgrTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public KrbAuthMgrTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the class has the correct characteristics.
     */
    public void testClass() {
        KrbAuthMgrImpl krbImpl = new KrbAuthMgrImpl();
        assertTrue("Kerberos auth manager should implement the correct interface", krbImpl instanceof IAuthenticator);
    }

    /**
     * This test verifies that the class rejects invalid configuration
     */
    public void testConfiguration() throws Exception {
        final String kdcLocation = "KDC Location";
        final String realm = "Realm";

        Properties missingRealmConfig = new Properties();
        missingRealmConfig.setProperty(PropertiesToJAASConfigurationConverter.KDC_CONFIG_PARAM, kdcLocation);

        Properties missingKDCConfig = new Properties();
        missingKDCConfig.setProperty(PropertiesToJAASConfigurationConverter.REALM_CONFIG_PARAM, realm);

        Properties goodConfig = new Properties();
        goodConfig.setProperty(PropertiesToJAASConfigurationConverter.KDC_CONFIG_PARAM, kdcLocation);
        goodConfig.setProperty(PropertiesToJAASConfigurationConverter.REALM_CONFIG_PARAM, realm);

        //Sets the missing KDC config
        boolean exThrown = false;
        try {
            KrbAuthMgrImpl testAuthMgr = new KrbAuthMgrImpl();
            testAuthMgr.initialize(missingKDCConfig);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Configuration without KDC is invalid", exThrown);

        //Sets the missing Realm config
        exThrown = false;
        try {
            KrbAuthMgrImpl testAuthMgr = new KrbAuthMgrImpl();
            testAuthMgr.initialize(missingRealmConfig);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Configuration without realm is invalid", exThrown);

        //Sets the correct config
        exThrown = false;
        try {
            KrbAuthMgrImpl testAuthMgr = new KrbAuthMgrImpl();
            testAuthMgr.initialize(goodConfig);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertFalse("Component can be created properly with correct configuration", exThrown);

        //Check the configuration was set in the system property
        final String currentRealm = System.getProperty("java.security.krb5.realm");
        final String currentKdc = System.getProperty("java.security.krb5.kdc");
        assertEquals("Realm system property is set correctly", realm, currentRealm);
        assertEquals("Realm system property is set correctly", kdcLocation, currentKdc);
    }

    /**
     * This test verifies that authentication works / does not work with a set
     * of correct / incorrect credentials.
     */
    public void testAuthentication() throws Exception {
        final String kdc = "linuxad01.linuxtest.bluejungle.com";
        //the realm name must be in UPPERCASE
        final String realm = "LINUXTEST.BLUEJUNGLE.COM";

        Properties goodConfig = new Properties();
        goodConfig.setProperty(PropertiesToJAASConfigurationConverter.KDC_CONFIG_PARAM, kdc);
        goodConfig.setProperty(PropertiesToJAASConfigurationConverter.REALM_CONFIG_PARAM, realm);

        KrbAuthMgrImpl authMgr = new KrbAuthMgrImpl();
        authMgr.initialize(goodConfig);
        try {
            IAuthenticationContext authCtx = authMgr.authenticate("jimmy.carter", "jimmy.carter");
            assertTrue("An auth ctx of required type must be returned", authCtx instanceof KrbAuthenticationContext);
            KrbAuthenticationContext krbAuthCtx = (KrbAuthenticationContext) authCtx;
            assertNotNull("A valid authentication should return an authenticated user", krbAuthCtx);
            assertNotNull("A valid authentication should return a subject", krbAuthCtx.loginContext.getSubject());
            assertEquals("One principal should be returned", 1, krbAuthCtx.loginContext.getSubject().getPrincipals().size());
            Principal p = krbAuthCtx.loginContext.getSubject().getPrincipals().iterator().next();
            assertEquals("A valid authentication should return a valid principal", "jimmy.carter@" + realm, p.getName());

            //Logs off the user now
            authCtx.logoff();
            assertEquals("After logoff, no principal should be returned", 0, krbAuthCtx.loginContext.getSubject().getPrincipals().size());
        } catch (AuthenticationFailedException e) {
            fail("Authentication should not fail with valid credentials:" + e.getMessage());
        }

        boolean exThrown = false;
        IAuthenticationContext badAuthCtx = null;
        try {
            badAuthCtx = authMgr.authenticate("badUserName", "badPassword");
        } catch (AuthenticationFailedException e) {
            exThrown = true;
        }
        assertTrue("Authentication should fail with invalid credentials", exThrown);
        assertNull("Authentication should fail with invalid credentials", badAuthCtx);
    }
}
