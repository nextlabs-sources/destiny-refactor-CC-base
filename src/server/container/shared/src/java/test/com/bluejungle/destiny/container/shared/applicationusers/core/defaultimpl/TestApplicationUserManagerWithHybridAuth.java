/*
 * Created on Oct 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserImportFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/TestApplicationUserManagerWithHybridAuth.java#1 $
 */

public class TestApplicationUserManagerWithHybridAuth extends BaseTestApplicationUserManager {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestApplicationUserManagerWithHybridAuth.class);
    }

    /*
     * @see BaseTestApplicationUserManager#setUp()
     */
    protected void setUp() throws Exception {

        super.setUp();
    }

    /*
     * @see BaseTestApplicationUserManager#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for TestApplicationUserManagerWithHybridAuth.
     * 
     * @param testName
     */
    public TestApplicationUserManagerWithHybridAuth(String testName) {
        super(testName);
    }

    public void testCanChangePassword() throws ApplicationUserCreationFailedException, UserAlreadyExistsException, UserManagementAccessException, ApplicationUserImportFailedException {
        IApplicationUser user = createLocalUser();

        assertTrue("testCanChangePassword - Ensure local user can change password", this.appUserManagerToTest.canChangePassword(user));
        
        user = importUser();
        
        assertTrue("testCanChangePassword - Ensure imported user cannot change password", this.appUserManagerToTest.canChangePassword(user));
    }

    public void testIsLocallyAuthenticated() throws ApplicationUserCreationFailedException, UserAlreadyExistsException, UserManagementAccessException, ApplicationUserImportFailedException {
        IApplicationUser user = createLocalUser();

        assertTrue("testIsLocallyAuthenticated - Ensure local user is locally authenticated", this.appUserManagerToTest.isLocallyAuthenticated(user));
        
        user = importUser();
        
        assertFalse("testIsLocallyAuthenticated - Ensure imported user is not locally authenticated", this.appUserManagerToTest.isLocallyAuthenticated(user));
    }
    
    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.BaseTestApplicationUserManager#getAuthenticationModeForTest()
     */
    protected AuthenticationModeEnumType getAuthenticationModeForTest() {
        return AuthenticationModeEnumType.HYBRID;
    }
}
