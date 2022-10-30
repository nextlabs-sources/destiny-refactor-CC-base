/*
 * Created on Dec 3, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.framework.threading.ITask;

/**
 * This class containes a remote event dispatch request. It can give the name of
 * the event to be fired and the location of the remote listener.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcsf/RemoteEventDispatchRequest.java#1 $:
 */

public class RemoteEventDispatchRequest implements ITask {

    private IDCCServerEvent event;
    private URL remoteLocation;

    /**
     * Constructor
     * 
     * @param evt
     *            name of the event
     * @param remoteLoc
     *            remote location of the DCSF web service to call
     */
    public RemoteEventDispatchRequest(IDCCServerEvent evt, URL remoteLoc) {
        this.event = evt;
        this.remoteLocation = remoteLoc;
    }

    /**
     * Returns the event object
     * 
     * @return the event object
     */
    public IDCCServerEvent getEvent() {
        return this.event;
    }

    /**
     * Returns the remote DCSF location
     * 
     * @return the remote DCSF location
     */
    public URL getRemoteLocation() {
        return this.remoteLocation;
    }
}