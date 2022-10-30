/*
 * Created on Nov 30, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/IRemoteEventRegistrationMgr.java#1 $:
 */

public interface IRemoteEventRegistrationMgr extends IInitializable, IDisposable, IConfigurable, ILogEnabled {

    public static final String COMP_NAME = "RemoteEventRegMgr";
    public static final String DCSF_LOCATION_CONF_PARAM = "DCSFLocation";
    public static final String DMS_LOCATION_CONF_PARAM = "DMSLocation";

    /**
     * Register for a remote event fired on a separate JVM instance. This
     * function is called when the local JVM instance wants to be notified of a
     * particular event (there is one local listener for this event, so we need
     * to receive notifications for this event).
     * 
     * @param eventName
     *            event name to register for
     */
    public void registerForRemoteEvent(String eventName);

    /**
     * Unregister for a remote event fired on a separate JVM instance. This
     * function is called when the local JVM instance no longer wants to be
     * notified of a particular event (all local listeners are gone, so there is
     * no need to receiving notifications).
     * 
     * @param eventName
     *            name of the event
     */
    public void unregisterForRemoteEvent(String eventName);
}