/*
 * Created on Oct 26, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import java.net.URL;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;

/**
 * This is the remote event dispatcher class. This class proxies event
 * notifications between the Destiny shared context and the DCSF web
 * application. The DCSF web application is reponsible to dispatch the event to
 * the location specified.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/events/impl/DestinyRemoteEventDispatcher.java#1 $:
 */
public class DestinyRemoteEventDispatcher implements IDestinyEventListener {

    protected URL location;
    protected IRegisteredDCSFComponent container;

    /**
     * Constructor
     * 
     * @param listenerURL
     *            URL of the remote web service that needs to be invoked when
     *            the event fires
     * @param dcsfContainer
     *            callback interface for the DCSF container
     */
    public DestinyRemoteEventDispatcher(URL listenerURL, IRegisteredDCSFComponent dcsfContainer) {
        super();
        if (listenerURL == null) {
            throw new IllegalArgumentException("Remote listener location cannot be null");
        }

        if (dcsfContainer == null) {
            throw new IllegalArgumentException("DCSF container cannot be null");
        }

        this.location = listenerURL;
        this.container = dcsfContainer;
    }

    /**
     * This API is called when the specific event is fired The dispatcher makes
     * a call to the DCSF container to notify that the event fired. The DCSF
     * container is responsible to route the event to the remote location.
     * 
     * @param event
     *            destiny event to be dispatched
     */
    public void onDestinyEvent(IDCCServerEvent event) {
        this.container.fireRemoteEvent(event, this.location);
    }
}