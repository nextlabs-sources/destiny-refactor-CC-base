/*
 * Created on Jun 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.auth;

import java.util.Properties;

import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/auth/IAuthenticator.java#2 $
 */

public interface IAuthenticator {

    /**
     * Initialize with configuration properties
     * 
     * @param properties
     */
    public void initialize(Properties properties) throws InvalidConfigurationException;

    /**
     * Authenticates a user against an authentication server
     * 
     * @param login
     * @param password
     * @return object representing authentication context, used for logoff
     * @throws AuthenticationFailedException
     */
    public IAuthenticationContext authenticate(String login, String password) throws AuthenticationFailedException;
}