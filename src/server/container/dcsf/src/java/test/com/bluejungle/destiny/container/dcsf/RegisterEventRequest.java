/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import org.apache.axis.types.URI;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/RegisterEventRequest.java#1 $:
 */

class RegisterEventRequest {

    private String eventName;
    private URI location;

    /**
     * Constructor
     * 
     * @param event
     *            event name
     * @param loc
     *            location of the callback
     */
    public RegisterEventRequest(String event, URI loc) {
        super();
        this.eventName = event;
        this.location = loc;
    }

    /**
     * Returns the eventName.
     * 
     * @return the eventName.
     */
    public String getEventName() {
        return this.eventName;
    }

    /**
     * Returns the location.
     * 
     * @return the location.
     */
    public URI getLocation() {
        return this.location;
    }
}