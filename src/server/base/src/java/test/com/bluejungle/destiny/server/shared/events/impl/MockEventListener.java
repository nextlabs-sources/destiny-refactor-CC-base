/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;

/**
 * Mock event listener class
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/events/tests/MockEventListener.java#1 $:
 */

public class MockEventListener implements IDestinyEventListener {

    private List eventList;

    /**
     * Constructor
     */
    public MockEventListener() {
        super();
        reset();
    }

    /**
     * Resets the listeners statistics
     */
    public void reset() {
        this.eventList = new ArrayList();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventListener#onDestinyEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
     */
    public void onDestinyEvent(IDCCServerEvent event) {
        this.eventList.add(event);
    }

    /**
     * @return true if the event was received
     */
    public boolean isEventReceived() {
        return this.eventList.size() > 0;
    }

    /**
     * Returns the number of events received
     * 
     * @return the number of events received
     */
    public int getEventCount() {
        return this.eventList.size();
    }

    /**
     * Returns a list of received Destiny Events
     * 
     * @return a list of received Destiny Events
     */
    public List getEventList() {
        return this.eventList;
    }
}