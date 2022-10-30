/*
 * Created on Sep 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser;

/**
 * This interface represents an authentication manager for a single domain.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/ISingleDomainAuthenticationManager.java#1 $
 */

public interface ISingleDomainAuthenticationManager {

    /**
     * Authenticates a username/password and returns an authenticated user
     * object
     * 
     * @param username
     * @param password
     * @return authenticated user
     * @throws AuthenticationFailedException
     * @throws UserManagementAccessException
     */
    public IAuthenticatedUser authenticateUser(String username, String password) throws AuthenticationFailedException, UserManagementAccessException;
}