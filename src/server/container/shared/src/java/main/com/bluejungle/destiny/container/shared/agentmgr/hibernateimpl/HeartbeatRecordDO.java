/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.Calendar;

/**
 * A single instance of a Heartbeat record
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/HeartbeatRecordDO.java#2 $
 */
public class HeartbeatRecordDO {

    private Long id;
    private Calendar timestamp;

    /**
     * Create an instance of HeartbeatRecordDO
     *  
     */
    public HeartbeatRecordDO() {
        super();
        timestamp = Calendar.getInstance();
    }

    /**
     * Returns the id.
     * 
     * @return the id.
     */
    Long getId() {
        return this.id;
    }

    /**
     * Returns the timestamp.
     * 
     * @return the timestamp.
     */
    Calendar getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the id
     * 
     * @param id
     *            The id to set.
     */
    void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the timestamp
     * 
     * @param timestamp
     *            The timestamp to set.
     */
    void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }
}