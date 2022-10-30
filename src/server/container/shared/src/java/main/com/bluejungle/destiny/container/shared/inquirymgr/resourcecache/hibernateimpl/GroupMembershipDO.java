/*
 * Created on Dec 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

/**
 * The GroupMemberShipDO represents the membership of an entity to a group
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/GroupMembershipDO.java#1 $
 */

public class GroupMembershipDO {

    private final long entityId;
    private final long groupId;

    /**
     * Constructor
     * 
     * @param entityId
     *            id of the entity
     * @param groupId
     *            id of the group
     */
    GroupMembershipDO(Long entityId, Long groupId) {
        this.entityId = entityId.longValue();
        this.groupId = groupId.longValue();
    }

    /**
     * Returns the entity id
     * 
     * @return the entity id
     */
    public long getEntityId() {
        return this.entityId;
    }

    /**
     * Returns the group id
     * 
     * @return the group id
     */
    public long getGroupId() {
        return this.groupId;
    }
}
