/*
 * Created on Sep 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.openldapimpl;

import java.util.Properties;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext;
import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/openldapimpl/MockExternalAuthenticatorImpl.java#1 $
 */

public class MockExternalAuthenticatorImpl implements IAuthenticator {

    /**
     * Constructor
     * 
     */
    public MockExternalAuthenticatorImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator#initialize(java.util.Properties)
     */
    public void initialize(Properties properties) {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator#authenticate(java.lang.String, java.lang.String)
     */
    public IAuthenticationContext authenticate(String login, String password) throws AuthenticationFailedException {
        return null;
    }

}
