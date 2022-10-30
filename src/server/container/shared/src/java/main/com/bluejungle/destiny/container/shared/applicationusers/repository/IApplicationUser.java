/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

import com.bluejungle.destiny.container.shared.applicationusers.core.IUser;

/**
 * This is the interface to represent a Destiny Application user.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/IApplicationUser.java#2 $
 */

public interface IApplicationUser extends IUser {

    public static final String LOGIN_AT_DOMAIN_SEPARATOR = "@";

    /**
     * Returns the destiny id of this user
     * 
     * @return destiny id
     */
    public Long getDestinyId();

    /**
     * @return true if the user is manually-created, false otherwise
     */
    public boolean isManuallyCreated();
}
