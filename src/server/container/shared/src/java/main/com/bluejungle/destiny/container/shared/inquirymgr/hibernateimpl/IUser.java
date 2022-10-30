/*
 * Created on Mar 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.framework.utils.TimeRelation;

/**
 * This is the interface for the user entity, In inquiry center, a user simply
 * contains a destiny Id and little bit of information, such as the groups it
 * belongs to.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IUser.java#1 $
 */

public interface IUser {

    /**
     * Returns the display name for the user. In Destiny, the display name is
     * unique accross all users.
     * 
     * @return the display name for the user
     */
    public String getDisplayName();

    /**
     * Returns the user first name
     * 
     * @return the user first name
     */
    public String getFirstName();

    /**
     * Returns the user last name
     * 
     * @return the user last name
     */
    public String getLastName();

    /**
     * Returns the Destiny Id for the user
     * 
     * @return the Destiny Id for the user
     */
    public Long getOriginalId();

    /**
     * Returns the user SID
     * 
     * @return the user SID
     */
    public String getSID();

    /**
     * Returns the user time relation
     * 
     * @return the user time relation
     */
    public TimeRelation getTimeRelation();
}
