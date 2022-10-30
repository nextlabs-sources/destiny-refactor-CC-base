/*
 * Created on Sep 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockEventManagerImpl.java#1 $
 */

public class MockEventManagerImpl implements IDestinyEventManager {

    private IDCCServerEvent lastEventFired;
    private int nEventsFired = 0;
    private String lastEventRegisteredFor;
    private IDestinyEventListener lastEventRegisteredBy;

    /**
     * Constructor
     *  
     */
    public MockEventManagerImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#fireEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
     */
    public void fireEvent(IDCCServerEvent event) {
        this.lastEventFired = event;
        this.nEventsFired++;
        this.lastEventRegisteredBy.onDestinyEvent(event);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#registerForEvent(java.lang.String,
     *      com.bluejungle.destiny.server.shared.events.IDestinyEventListener)
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener) {
        this.lastEventRegisteredFor = eventName;
        this.lastEventRegisteredBy = listener;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#unregisterForEvent(java.lang.String,
     *      com.bluejungle.destiny.server.shared.events.IDestinyEventListener)
     */
    public void unregisterForEvent(String eventName, IDestinyEventListener listener) {
    }

    /**
     * Returns the lastEventFired.
     * 
     * @return the lastEventFired.
     */
    public IDCCServerEvent getLastEventFired() {
        return this.lastEventFired;
    }

    /**
     * Returns the lastEventRegisteredBy.
     * 
     * @return the lastEventRegisteredBy.
     */
    public IDestinyEventListener getLastEventRegisteredBy() {
        return this.lastEventRegisteredBy;
    }

    /**
     * Returns the lastEventRegisteredFor.
     * 
     * @return the lastEventRegisteredFor.
     */
    public String getLastEventRegisteredFor() {
        return this.lastEventRegisteredFor;
    }

    /**
     * Returns the nEventsFired.
     * 
     * @return the nEventsFired.
     */
    public int getNEventsFired() {
        return this.nEventsFired;
    }

    /**
     * Resets the state
     */
    public void reset() {
        nEventsFired = 0;
        this.lastEventFired = null;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#shutdown()
     */
    public void shutdown() {
    }
}
