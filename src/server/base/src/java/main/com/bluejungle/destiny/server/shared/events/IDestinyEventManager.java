/*
 * Created on Oct 18, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.server.shared.events;


/**
 * This interface is implemented by the Destiny event manager.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/events/IDestinyEventManager.java#4 $:
 */
public interface IDestinyEventManager {

    /**
     * Fires an event to all registered listeners
     * 
     * @param event
     *            Destiny event to fire
     */
    public void fireEvent(IDCCServerEvent event);

    /**
     * Registration to listen to for a Destiny event.
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface when that event fires
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener);

    /**
     * Unregisters to listen for a Destiny event
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface when that event fires
     */
    public void unregisterForEvent(String eventName, IDestinyEventListener listener);

    /**
     * Shutdown the event manager
     */
    public void shutdown();
    
}
