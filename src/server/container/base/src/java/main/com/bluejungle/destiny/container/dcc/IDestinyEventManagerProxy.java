/*
 * Created on Jul 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;

/**
 * This interface represents functionality to proxy event notifications so that
 * even notification is executed within the correct class loader.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/IDestinyEventManagerProxy.java#1 $
 */

public interface IDestinyEventManagerProxy extends IDestinyEventListener {

    /**
     * This is the method that should set up the proxy relationship.
     * 
     * @param eventName
     * @param listener
     */
    public void addListener(String eventName, IDestinyEventListener listener);
}