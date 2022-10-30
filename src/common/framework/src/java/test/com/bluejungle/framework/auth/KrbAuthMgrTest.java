/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.auth;

import java.security.Principal;
import java.util.Date;

import com.bluejungle.framework.auth.AuthenticationException;
import com.bluejungle.framework.auth.IAuthMgr;
import com.bluejungle.framework.auth.IAuthenticatedUser;
import com.bluejungle.framework.auth.KrbAuthMgrImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
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
        assertTrue("Kerberos auth manager should implement the correct interface", krbImpl instanceof IAuthMgr);
        assertTrue("Kerberos auth manager should implement the correct interface", krbImpl instanceof ILogEnabled);
        assertTrue("Kerberos auth manager should implement the correct interface", krbImpl instanceof IInitializable);
    }

    /**
     * This test verifies that the class rejects invalid configuration
     */
    public void testConfiguration() {
        final String kdcLocation = "KDC Location";
        final String realm = "Realm";

        HashMapConfiguration missingRealmConfig = new HashMapConfiguration();
        missingRealmConfig.setProperty(IAuthMgr.KDC_CONFIG_PARAM, kdcLocation);

        HashMapConfiguration missingKDCConfig = new HashMapConfiguration();
        missingKDCConfig.setProperty(IAuthMgr.REALM_CONFIG_PARAM, realm);

        HashMapConfiguration goodConfig = new HashMapConfiguration();
        goodConfig.setProperty(IAuthMgr.KDC_CONFIG_PARAM, kdcLocation);
        goodConfig.setProperty(IAuthMgr.REALM_CONFIG_PARAM, realm);

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        //Sets the missing KDC config
        boolean exThrown = false;
        try {
            ComponentInfo info = new ComponentInfo("BadAuthMgr", KrbAuthMgrImpl.class.getName(), IAuthMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, missingKDCConfig);
            compMgr.getComponent(info);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Configuration without KDC is invalid", exThrown);

        //Sets the missing Realm config
        exThrown = false;
        try {
            ComponentInfo info = new ComponentInfo("AnotherAuthMgr", KrbAuthMgrImpl.class.getName(), IAuthMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, missingRealmConfig);
            compMgr.getComponent(info);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Configuration without realm is invalid", exThrown);

        //Sets the correct config
        exThrown = false;
        try {
            ComponentInfo info = new ComponentInfo("GoodAuthMgr", KrbAuthMgrImpl.class.getName(), IAuthMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, goodConfig);
            compMgr.getComponent(info);
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
    public void testAuthentication() {
        final String kdc = "linuxad01.linuxtest.bluejungle.com";
        final String realm = "linuxtest.bluejungle.com";

        HashMapConfiguration goodConfig = new HashMapConfiguration();
        goodConfig.setProperty(IAuthMgr.KDC_CONFIG_PARAM, kdc);
        goodConfig.setProperty(IAuthMgr.REALM_CONFIG_PARAM, realm);

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo<IAuthMgr> info = new ComponentInfo<IAuthMgr>(
                IAuthMgr.COMP_NAME, 
                KrbAuthMgrImpl.class, 
                IAuthMgr.class, 
                LifestyleType.TRANSIENT_TYPE, 
                goodConfig);
        IAuthMgr authMgr = compMgr.getComponent(info);

        try {
            IAuthenticatedUser authUser = authMgr.authenticate("jimmy.carter@linuxtest.bluejungle.com", "jimmy.carter");
            assertNotNull("A valid authentication should return an authenticated user", authUser);
            assertNotNull("A valid authentication should return a login time", authUser.getLoginDate());
            assertNotNull("A valid authentication should return a subject", authUser.getSubject());
            Date now = new Date();
            assertTrue("A valid authentication should return a valid login time", now.compareTo(authUser.getLoginDate()) >= 0);
            assertEquals("One principal should be returned", 1, authUser.getSubject().getPrincipals().size());
            Principal p = authUser.getSubject().getPrincipals().iterator().next();
            assertEquals("A valid authentication should return a valid principal", "jimmy.carter@" + realm, p.getName());
            
            //Logs off the user now
            authUser.logoff();
            assertEquals("After logoff, no principal should be returned", 0, authUser.getSubject().getPrincipals().size());
        } catch (AuthenticationException e) {
            fail("Authentication should not fail with valid credentials:" + e.getMessage());
        }

        boolean exThrown = false;
        IAuthenticatedUser badUser = null;
        try {
            badUser = authMgr.authenticate("badUserName", "badPassword");
        } catch (AuthenticationException e) {
            exThrown = true;
        }
        assertTrue("Authentication should fail with invalid credentials", exThrown);
        assertNull("Authentication should fail with invalid credentials", badUser);
    }
}
