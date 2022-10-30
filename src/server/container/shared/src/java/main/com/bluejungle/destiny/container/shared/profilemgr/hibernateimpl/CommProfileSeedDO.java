/*
 * Created on May 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the seed data data object class for the profile. This class stored
 * the list of seeded records that have been added. The map allows looking up a
 * given seed record name, and figure out its id within the system.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/hibernateimpl/CommProfileSeedDO.java#1 $
 */

class CommProfileSeedDO {

    private Long id;
    private Calendar lastUpdated;
    private Map seedItems = new HashMap();

    /**
     * Returns the record id
     * 
     * @return the record id
     */
    protected Long getId() {
        return this.id;
    }

    /**
     * Returns the timestamp of the record
     * 
     * @return the timestamp of the record
     */
    protected Calendar getLastUpdated() {
        return this.lastUpdated;
    }

    /**
     * Returns the map of seeded items
     * 
     * @return the map of seeded items
     */
    protected Map getSeedItems() {
        return this.seedItems;
    }

    /**
     * Sets the record id
     * 
     * @param id
     *            id to set
     */
    protected void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the map of seeded items
     * 
     * @param seedItems
     *            map to set
     */
    protected void setSeedItems(Map seedItems) {
        this.seedItems = seedItems;
    }

    /**
     * Sets the record timestamp
     * 
     * @param lastUpdated
     *            timestamp to set
     */
    protected void setLastUpdated(Calendar lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}