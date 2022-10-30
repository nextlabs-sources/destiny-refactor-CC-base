/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IDomain;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/applicationusers/IAuthenticationDomain.java#1 $
 */

public interface IAuthenticationDomain extends IDomain {

    /**
     * Authenticates a user against the domain
     * 
     * @param login
     * @param password
     * @return the authenticated user
     */
    public IAuthenticatedUser authenticateUser(String login, String password) throws AuthenticationFailedException;
}