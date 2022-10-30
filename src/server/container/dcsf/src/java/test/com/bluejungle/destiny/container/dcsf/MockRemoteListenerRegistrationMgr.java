/*
 * Created on Jan 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;

import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;

/**
 * This is a dmmy class extending the remote listener registration manager. Its
 * main purpose is to access some internal objects within the superclass.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/MockRemoteListenerRegistrationMgr.java#1 $
 */

public final class MockRemoteListenerRegistrationMgr extends RemoteListenerRegistrationMgrImpl {

    /**
     * Overrides the regular init method
     */
    public void init() {
        super.init();
        this.eventManager = new MockSharedEventManager();
    }

    /**
     * Returns the listener for a given event name and callback URL
     * 
     * @param callback
     *            URI to callback if the event fires
     * @param eventName
     *            name of the event
     * @return the listener for a given event name and callback URL
     */
    public IDestinyEventListener getListener(String eventName, URL callback) {
        return super.getListener(eventName, callback);
    }

    /**
     * Returns the dummy event manager
     * 
     * @return the dummy event manager
     */
    public MockSharedEventManager getMockEventManager() {
        return (MockSharedEventManager) this.eventManager;
    }
}