/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import java.net.URL;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;

/**
 * This is a class used only for testing purposes. It hold an remote event
 * information when it is fired (location and event)
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/test/com/bluejungle/destiny/server/shared/registration/MockRemoteEventFiredRequest.java#1 $:
 */

public class MockRemoteEventFiredRequest {

    private IDCCServerEvent event;
    private URL location;

    /**
     * Constructor
     * @param event event that fired
     * @param location location of the remote listener
     */
    public MockRemoteEventFiredRequest(IDCCServerEvent event, URL location) {
        super();
        this.event = event;
        this.location = location;
    }

    /**
     * Returns the event that was fired
     * 
     * @return the event that was fired
     */
    public IDCCServerEvent getEvent() {
        return this.event;
    }

    /**
     * Returns the location of the remote listener
     * 
     * @return the location of the remote listener
     */
    public URL getLocation() {
        return this.location;
    }
}