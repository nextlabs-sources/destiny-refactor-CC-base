/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

import java.util.Date;


/**
 * This interface represents a user that has already been authenticated as an
 * application user.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/auth/IAuthenticatedUser.java#1 $
 */

public interface IAuthenticatedUser extends IApplicationUser {

    /**
     * Returns the date / time at which the user successfully authenticated in
     * the system
     * 
     * @return the date and time of the user authentication
     */
    public Date getLoginDate();

    /**
     * Logs off an authenticated user
     */
    public void logoff();
}