/*
 * Created on Jul 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.auth;

/**
 * This interface represents authentication context that is associated with an
 * authentication.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/auth/IAuthenticationContext.java#1 $
 */

public interface IAuthenticationContext {

    /**
     * Logs off the authenticated user from the authentication context
     */
    public void logoff();
}