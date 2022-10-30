/*
 * Created on Jan 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.internal.IInternalEventManager;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/events/DestinyEventManagerBaseImpl.java#1 $
 */

public abstract class DestinyEventManagerBaseImpl implements IInternalEventManager {

    /**
     * Fires an event to all registered listeners
     * 
     * @param event
     *            event to fire
     */
    public void fireEvent(IDCCServerEvent event) {
        this.fireEvent(event, true);
    }

    /**
     * Registers a listener to be notified when an event fires
     * 
     * @param eventName
     *            event name
     * @param listener
     *            callback interface of the listener
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener) {
        this.registerForEvent(eventName, listener, true);
    }

    /**
     * Unregisters a listener for a given event
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface of the listener
     */
    public void unregisterForEvent(String eventName, IDestinyEventListener listener) {
        this.unregisterForEvent(eventName, listener, true);
    }
}