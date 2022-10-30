/*
 * Created on Nov 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */

package com.bluejungle.destiny.agent.controlmanager;

/**
 * @author hfriedland
 */
public class SCMEvent {

    // Corresponds to value in WINSVC.H
    public static final int SERVICE_STOPPED = 0x00000001;
    public static final int SERVICE_STARTED = 0x00000003;
    public static final int SERVICE_CONTROL_DEVICEEVENT = 0x0000000B;
    // Our own events
    public static final int ABNORMAL_SHUTDOWN = 0x1000;

    private int id;

    /**
     * Constructor
     * 
     * @param code
     */
    public SCMEvent(int code) {
        this.id = code;
    }

    /**
     * Return id
     * 
     * @return id
     */
    public int getID() {
        return this.id;
    }
}