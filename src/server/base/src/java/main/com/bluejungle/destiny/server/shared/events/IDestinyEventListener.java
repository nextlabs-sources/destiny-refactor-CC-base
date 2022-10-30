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
 * @author ihanen
 * 
 * This interface needs to be implemented by any object that needs to listen to
 * Destiny events.
 */

public interface IDestinyEventListener {

    /**
     * Fires when a particular event occurs
     * 
     * @param event
     *            DCC server event that has been fired
     */
    public void onDestinyEvent(IDCCServerEvent event);
}