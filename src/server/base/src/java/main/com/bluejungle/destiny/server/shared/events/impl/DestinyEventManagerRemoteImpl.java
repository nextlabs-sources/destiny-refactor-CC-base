/*
 * Created on Oct 18, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.server.shared.events.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;

/**
 * This is the event manager implementation for the ditributed installation. In
 * this implementation, the DCC components are physically located on different
 * JVM. The remote implementation extends the local implementation. Remote
 * listeners are stored in a separate map so that their listeners are fired only
 * when the event is fired locally (to avoid multiple notifications fired on the
 * same listener for the same event)
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/events/DestinyEventManagerRemoteImpl.java#6 $:
 *  
 */
public class DestinyEventManagerRemoteImpl extends DestinyEventManagerLocalImpl implements IDestinyEventManager, IDCSFRegistrationListener {

    private final Map<String, Set<IDestinyEventListener>> remoteEvents = new HashMap<String, Set<IDestinyEventListener>>();
    private final List<String> remoteEventRegistrationQueue = new LinkedList<String>();
    private final List<String> remoteEventUnregistrationQueue = new LinkedList<String>();
    private IRegisteredDCSFComponent dcsfContainer;

    /**
     * Registration to listen to for a Destiny event.
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface when that event fires
     * @param local
     *            true if the event registration is done locally false if the
     *            event registration is done remotely.
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener, boolean local) {
        //If this is a local event, let the local implementation handle it
        if (local) {
            boolean eventAlreadySubscribedOnce = false;
            Map<String, Set<IDestinyEventListener>> localEvents = getLocalEvents();
            synchronized (localEvents) {
                if (localEvents.get(eventName) != null) {
                    eventAlreadySubscribedOnce = true;
                }
            }

            super.registerForEvent(eventName, listener, local);

            //The event is now locally registered. We need to notify DMS that
            //a new event has been registered. However, we need to do this only
            //if no other listeners are already registered locally
            if (!eventAlreadySubscribedOnce) {
                registerEventWithRemoteDMS(eventName);
            }
        } else {
            //The event is remotely registered, put it in the remote event map
            registerEventInMap(remoteEvents, listener, eventName);
        }
    }

    /**
     * Unregisters to listen for a Destiny event
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface when that event fires
     * @param local
     *            true if the event registration is done locally false if the
     *            event registration is done remotely
     */
    public void unregisterForEvent(String eventName, IDestinyEventListener listener, boolean local) {
        if (local) {
            super.unregisterForEvent(eventName, listener, local);
            Map<String, Set<IDestinyEventListener>> localEvents = getLocalEvents();
            synchronized (localEvents) {
                if (localEvents.get(eventName) == null) {
                    unRegisterEventWithRemoteDMS(eventName);
                }
            }
        } else {
            removeEventListenerInMap(getLocalEvents(), listener, eventName);
        }
    }

    /**
     * This method calls the DCSF container and registers the event. If the DCSF
     * is not ready yet, the event registration request is queued up. Once DCSF
     * is discovered, the event will be passed to DCSF.
     * 
     * @param eventName
     *            name of the event
     */
    private void registerEventWithRemoteDMS(String eventName) {
        synchronized (this) {
            if (dcsfContainer != null) {
                dcsfContainer.registerForRemoteEvent(eventName);
            } else {
                queueEventRegistrationRequest(eventName);
            }
        }
    }

    /**
     * This method calls the DCSF container and unregisters the event. If the
     * DCSF is not ready yet, the event unregistration request is queued up.
     * Once DCSF is discovered, the event unregistration request will be passed
     * to DCSF.
     * 
     * @param eventName
     *            name of the event
     */
    private void unRegisterEventWithRemoteDMS(String eventName) {
        synchronized (this) {
            if (dcsfContainer != null) {
                dcsfContainer.unregisterForRemoteEvent(eventName);
            } else {
                queueEventUnregistrationRequest(eventName);
            }
        }
    }

    /**
     * Fires an event to the registered listeners
     * 
     * @param event
     *            event that is fired
     * @param local
     *            true if the event is fired by a local provider false if the
     *            event is fired from a remote instance
     */
    public void fireEvent(IDCCServerEvent event, boolean local) {

        //If the event is fired locally, it must be dispatched to both local
        // and remote listeners
        //If the event is fired remotely, it must be dispatched to local
        // listeners only
        super.fireEvent(event, local);
        if (local) {
            fireEventInMap(remoteEvents, event);
        }
    }

    /**
     * This function is called when the DCSF container has been registered. From
     * that point on, the event manager can start forwarding events to DCSF for
     * processing.
     * 
     * @param newDCSFContainer
     *            DCSF container that just registered
     */
    public void onDCSFRegistered(IRegisteredDCSFComponent newDCSFContainer) {
        synchronized (this) {
            dcsfContainer = newDCSFContainer;

            //Now, flush any pending remote event registration requests
            for (String eventName : remoteEventRegistrationQueue) {
                dcsfContainer.registerForRemoteEvent(eventName);
            }
            remoteEventRegistrationQueue.clear();

            //Also, flush pending remote event unregistration requests
            for (String eventName : remoteEventUnregistrationQueue) {
                dcsfContainer.unregisterForRemoteEvent(eventName);
            }
            remoteEventUnregistrationQueue.clear();
        }
    }

    /**
     * This function is called when the DCSF web application unregisters with
     * the shared context. This could happen if the web application is shutdown
     * for some reason, but this is not a very likely scenario.
     * 
     * @param dcsfContainer
     *            DCSF web application container object
     */
    public void onDCSFUnRegistered(IRegisteredDCSFComponent ignored) {
        synchronized (this) {
            dcsfContainer = null;
        }
    }

    /**
     * Queues up an event registration request for the DCSF container. If this
     * event was queued for unregistration, it should be deleted from the event
     * unregistration request queue also.
     * 
     * @param eventName
     *            name of the event
     */
    private synchronized void queueEventRegistrationRequest(String eventName) {
        if (remoteEventUnregistrationQueue.contains(eventName)) {
            remoteEventUnregistrationQueue.remove(eventName);
        } else {
            remoteEventRegistrationQueue.add(eventName);
        }
    }

    /**
     * Queues up an event unregistration request for the DCSF container. If this
     * event was queued for registration, it should be deleted from the event
     * registration request queue also.
     * 
     * @param eventName
     *            name of the event
     */
    private synchronized void queueEventUnregistrationRequest(String eventName) {
        if (remoteEventRegistrationQueue.contains(eventName)) {
            remoteEventRegistrationQueue.remove(eventName);
        } else {
            remoteEventUnregistrationQueue.add(eventName);
        }
    }
}
