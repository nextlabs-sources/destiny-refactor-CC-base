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
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * This interface is implemented by the remote event dispatch manager. The
 * remote event dispatch manager allows dispatching local events to remote
 * listeners through a web sercice call to a remote DCSF instance
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcsf/IRemoteEventDispatchMgr.java#1 $:
 */

public interface IRemoteEventDispatchMgr extends IInitializable, IDisposable, IConfigurable, ILogEnabled {

    public static final String COMP_NAME = "RemoteEventDispatchMgr";

    /**
     * Fire an event to a remote DCSF listener
     * 
     * @param event
     *            event object
     * @param remoteLocation
     *            location of the remote DCSF listener
     */
    public void fireEvent(IDCCServerEvent event, URL remoteLocation);
}