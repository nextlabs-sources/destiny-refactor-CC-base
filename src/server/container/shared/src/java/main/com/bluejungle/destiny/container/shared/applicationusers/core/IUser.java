/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

/**
 * This is a base interface that holds basic user information for the
 * application users component.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/IUser.java#1 $
 */

public interface IUser extends IDomainEntity {

    /**
     * Returns the login name of the user
     * 
     * @return login name
     */
    public String getLogin();

    /**
     * Returns the first name of this user
     * 
     * @return first name
     */
    public String getFirstName();

    /**
     * Returns the last name of this user
     * 
     * @return last name
     */
    public String getLastName();

    /**
     * Returns the principal name of this user
     * 
     * @return principal name
     */
    public String getUniqueName();

    /**
     * Returns teh display name of this user
     * 
     * @return display name
     */
    public String getDisplayName();
}