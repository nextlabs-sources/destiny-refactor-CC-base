/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;

/**
 * IHeartbeatRecorder will record an entry in the persistence store for every
 * heartbeat. This record is utilized for statistical purposes.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/IHeartbeatRecorder.java#2 $
 */
public interface IHeartbeatRecorder {

    public static final String COMP_NAME = "HeartBeatRecorder";

    /**
     * Record an instance of an Agent heartbeat
     * 
     * @param heartbeat
     *            the heartbeat to record
     */
    public void recordHeartbeat(IAgentHeartbeatData heartbeat);

    /**
     * Retrieve the number of heart beats recorded since the specified time
     * 
     * @param time
     *            the time at which the count should being
     * @return the number of heart beats recorded since the specified time
     * @throws PersistenceException
     *             if a persistence failure occurs
     */
    public long getNumHeartbeatsSinceTime(Calendar time) throws PersistenceException;
}