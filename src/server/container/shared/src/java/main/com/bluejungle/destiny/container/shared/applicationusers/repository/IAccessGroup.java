/*
 * Created on Sep 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

import com.bluejungle.destiny.container.shared.applicationusers.core.IGroup;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/IAccessGroup.java#1 $
 */

public interface IAccessGroup extends IGroup {

    public static final String GROUP_AT_DOMAIN_SEPARATOR = "@";

    /**
     * Returns the id of the given group - unique across all groups in the
     * system. The id space is not shared with any other type of entity.
     * 
     * @return id of group
     */
    public Long getDestinyId();

    /**
     * Returns the Access Control string that will be inherited by all objects
     * created by the users within this group.
     * 
     * @return access control string
     */
    public String getApplicableAccessControl();

    /**
     * Returns the description for this group
     * 
     * @return description for group
     */
    public String getDescription();
}