/*
 * Created on Dec 10, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.internal.IInternalEventManager;

/**
 * This is a dummy shared event manager that spies the calls from the DCSF web
 * service.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockSharedEventManager.java#1 $:
 */

class MockSharedEventManager implements IDestinyEventManager, IInternalEventManager {

    private IDCCServerEvent lastEvent;
    private String lastEventName;
    private boolean lastLocal = true;
    private IDestinyEventListener lastListener;

    /**
     * Resets the mock object
     */
    public void reset() {
        this.lastEvent = null;
        this.lastEventName = null;
        this.lastLocal = true;
        this.lastListener = null;
    }

    /**
     * @param event
     * @param local
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#fireEvent(com.bluejungle.destiny.services.dcsf.types.DestinyEvent,
     *      boolean)
     */
    public void fireEvent(IDCCServerEvent event, boolean local) {
        this.lastEvent = event;
        this.lastLocal = local;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#fireEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
     */
    public void fireEvent(IDCCServerEvent event) {
        fireEvent(event, true);
    }

    /**
     * @param eventName
     * @param listener
     * @param local
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#registerForEvent(java.lang.String,
     *      com.bluejungle.destiny.server.shared.events.IDestinyEventListener,
     *      boolean)
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener, boolean local) {
        this.lastEventName = eventName;
        this.lastLocal = local;
        this.lastListener = listener;
    }

    /**
     * @param eventName
     * @param listener
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener) {
        this.registerForEvent(eventName, listener, true);
    }

    /**
     * @param eventName
     * @param listener
     * @param local
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#unregisterForEvent(java.lang.String,
     *      com.bluejungle.destiny.server.shared.events.IDestinyEventListener,
     *      boolean)
     */
    public void unregisterForEvent(String eventName, IDestinyEventListener listener, boolean local) {
        this.lastEventName = eventName;
        this.lastLocal = local;
        this.lastListener = listener;
    }

    /**
     * @param eventName
     * @param listener
     */
    public void unregisterForEvent(String eventName, IDestinyEventListener listener) {
        unregisterForEvent(eventName, listener, true);
    }

    /**
     * Returns the lastEvent.
     * 
     * @return the lastEvent.
     */
    public IDCCServerEvent getLastEvent() {
        return this.lastEvent;
    }

    /**
     * Returns the lastEventName.
     * 
     * @return the lastEventName.
     */
    public String getLastEventName() {
        return this.lastEventName;
    }

    /**
     * Returns the lastListener.
     * 
     * @return the lastListener.
     */
    public IDestinyEventListener getLastListener() {
        return this.lastListener;
    }

    /**
     * Returns the lastLocal.
     * 
     * @return the lastLocal.
     */
    public boolean isLastLocal() {
        return this.lastLocal;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#shutdown()
     */
    public void shutdown() {
    }
}
