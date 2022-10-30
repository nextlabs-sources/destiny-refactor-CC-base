package com.bluejungle.destiny.agent.controlmanager;
/*
 * Created on Jan 20, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.Calendar;

public interface IHeartbeatRecordManager {
    public static final String NAME = IHeartbeatRecordManager.class.getName();

    /**
     * Report a successful heartbeat
     *
     * @param when the time of the successful heartbeat
     */
    public void updateLastSuccessfulHeartbeat(Calendar when);

    /**
     * Returns the number of seconds since the last
     * successful heartbeat
     *
     * @return number of seconds
     */
    public long getTimeSinceLastSuccessfulHeartbeat();

    /**
     * Called by the Control Manager to serialize the current system time
     */
    public void recordState();

    /**
     * Indicate if we should protect against system clock tampering or not
     */
    public void detectRollback(boolean doRollbackDetection);
}
