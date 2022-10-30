/*
 * Created on Jan 12, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class MockEventManager implements IDestinyEventManager {

    private Map eventsFired;
    private int numEventsFired;

    /**
     * Constructor
     */
    public MockEventManager() {
        super();
        this.eventsFired = new HashMap();
        this.numEventsFired = 0;
    }

    /**
     * Keep track of the events fired and the total number of times the
     * fireEvent() API has been called - for testing purposes only.
     * 
     * @param event
     *            event to fire
     */
    public void fireEvent(IDCCServerEvent event) {
        int nFires;
        if (eventsFired.containsKey(event)) {
            Integer val = (Integer) this.eventsFired.get(event);
            nFires = val.intValue();
            nFires++;
        } else {
            nFires = 1;
        }
        eventsFired.put(event, new Integer(nFires));
        this.numEventsFired++;
    }

    /**
     * Register for an event
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            listener callback
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener) {
    }

    /**
     * Unregister for an event
     * 
     * @param name
     *            event name
     * @param listener
     *            listener callback
     */
    public void unregisterForEvent(String name, IDestinyEventListener listener) {
    }

    /**
     * Returns the events fired.
     * 
     * @return the events fired.
     */
    public Map getEventsFired() {
        return this.eventsFired;
    }

    /**
     * Returns the number of events fired.
     * 
     * @return the number of events fired.
     */
    public int getNumEventsFired() {
        return this.numEventsFired;
    }

    /**
     * Resets the mock object.
     */
    public void reset() {
        this.eventsFired.clear();
        this.numEventsFired = 0;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#shutdown()
     */
    public void shutdown() {
    }
}
