/*
 * Created on Jul 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;

/**
 * This class acts as a proxy registration manager that registers with the
 * shared context for an event on behalf of a local web-app listener. When the
 * concerned event is fired, this object will handle the event by creating a
 * processing thread that is specific to the local class loader. This prevents a
 * thread created from the shared context class loader to execute inside the
 * web-app local class loader.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcc/LocalListenerRegistrationManagerProxyImpl.java#1 $
 */

public class LocalListenerRegistrationManagerProxyImpl implements IDestinyEventManagerProxy {

    /**
     * @see com.bluejungle.destiny.container.dcc.IDestinyEventManagerProxy#addListener(java.lang.String,
     *      com.bluejungle.destiny.server.shared.events.IDestinyEventListener)
     */
    public synchronized void addListener(String eventName, IDestinyEventListener listener) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventListener#onDestinyEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
     */
    public void onDestinyEvent(IDCCServerEvent event) {
    }
}