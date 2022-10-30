package com.bluejungle.pf.tools;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/tools/MockEventManager.java#1 $
 */

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;

/**
 * @author safdar, sergey
 *
 */
public class MockEventManager implements IDestinyEventManager {

    private int eventFiredCount = 0;

    /**
     * Constructor
     */
    public MockEventManager() {
        super();
    }

    /**
     * @see IDestinyEventManager#fireEvent(IDCCServerEvent)
     */
    public void fireEvent(IDCCServerEvent event) {
        eventFiredCount++;
    }

    /**
     * Returns the number of events that have been fired so far
     *
     * @return count
     */
    public int getEventFiredCount() {
        return eventFiredCount;
    }

    /**
     * @see IDestinyEventManager#registerForEvent(String,IDestinyEventListener)
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener) {
    }

    /**
     * @see IDestinyEventManager#unregisterForEvent(String, IDestinyEventListener)
     */
    public void unregisterForEvent(String eventName, IDestinyEventListener listener) {
    }

    /**
     * @see IDestinyEventManager#shutdown()
     */
    public void shutdown() {
    }

}
