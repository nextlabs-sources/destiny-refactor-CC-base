/*
 * Created on Jan 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.internal;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/internal/IInternalEventManager.java#1 $
 */

public interface IInternalEventManager extends IDestinyEventManager {
    /**
     * Fires an event to all registered listeners
     * 
     * @param event
     *            Destiny event
     * @param local
     *            true if the event is fired by a local emitter
     */
    public void fireEvent(IDCCServerEvent event, boolean local);

    /**
     * Registration to listen to for a Destiny event.
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface when that event fires
     * @param local
     *            true if the event registration is done locally false if the
     *            event registration is done remotely
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener, boolean local);

    /**
     * Unregisters to listen for a Destiny event
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface when that event fires
     * @param local
     *            true if the event registration is done locally false if the
     *            event registration is done remotely
     */
    public void unregisterForEvent(String eventName, IDestinyEventListener listener, boolean local);
}