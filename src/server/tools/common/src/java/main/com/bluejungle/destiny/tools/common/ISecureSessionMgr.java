/*
 * Created on Mar 3, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.common;

import java.net.URL;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/common/src/java/main/com/bluejungle/destiny/tools/common/ISecureSessionMgr.java#1 $
 */

public interface ISecureSessionMgr {

    /**
     * Logs in a user with credentials
     * 
     * @param username
     *            username
     * @param location
     *            location of the remote web service (must be a secure service location)
     * @param password
     *            password
     * @throws SecureLoginException
     *             if the login fails, or if the service is not secure
     */
    public void login(URL location, String username, String password) throws SecureLoginException;

    /**
     * Logs out from the current secure session
     */
    public void logout();
}
