/*
 * Created on Jan 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * This is the interface for the remote listener registration manager. This
 * component keeps track of the remote listeners that need to be notified if a
 * local event fires.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcsf/IRemoteListenerRegistrationMgr.java#1 $
 */

public interface IRemoteListenerRegistrationMgr extends IConfigurable, IInitializable, IDisposable, ILogEnabled {

    public static final String PARENT_DCSF_COMPONENT_CONFIG_PARAM = "DCSFComponent";
    public static final String COMP_NAME = "RemoteListenerRegistrationMgr";
    
    /**
     * Register a remote listener to be notified when a local event fires.
     * 
     * @param eventName
     *            name of the event
     * @param callback
     *            URL of the DCSF web service that needs to be called back when
     *            the event fires
     */
    public void registerRemoteListener(String eventName, URL callback);

    /**
     * Unregister a remote listener. When a local event fires, this listener
     * does not need to be notified
     * 
     * @param eventName
     *            name of the event
     * @param callback
     *            URL of the DCSF web service that needs to be called back when
     *            the event fires
     */
    public void unregisterRemoteListener(String eventName, URL callback);
}