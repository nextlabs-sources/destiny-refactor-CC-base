/*
 * Created on Oct 18, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;

/**
 * Event manager for local deployment (all processes in one JVM) In this case,
 * no web service is involved when dispatching notifications.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/events/DestinyEventManagerLocalImpl.java#6 $:
 */
public class DestinyEventManagerLocalImpl extends DestinyEventManagerBaseImpl implements IDestinyEventManager {

    private static final Set<IDestinyEventListener> EMPTY_SET = new HashSet<IDestinyEventListener>();
    private static final Log LOG = LogFactory.getLog(DestinyEventManagerLocalImpl.class);

    private final List<FireEventRequest> firedEventQueue = new LinkedList<FireEventRequest>();
    private final Map<String, Set<IDestinyEventListener>> localEvents = new HashMap<String, Set<IDestinyEventListener>>();
    private final EventProcessorThread eventProcessorThread = new EventProcessorThread(firedEventQueue);

    /**
     * Constructor
     */
    public DestinyEventManagerLocalImpl() {
        super();
        eventProcessorThread.start();
    }

    /**
     * Returns the local events map
     * 
     * @return the local events map
     */
    protected Map<String, Set<IDestinyEventListener>> getLocalEvents() {
        return localEvents;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    private Log getLog() {
        return LOG;
    }

    /**
     * Registration to listen to for a Destiny event.
     * 
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface when that event fires
     * @param local
     *            true if the event registration is done locally false if the
     *            event registration is done remotely
     */
    public void registerForEvent(String eventName, IDestinyEventListener listener, boolean local) {
        registerEventInMap(getLocalEvents(), listener, eventName);
    }

    /**
     * Fires an event to all registered listeners
     * 
     * @param event
     *            Destiny event
     * @param local
     *            true if the event is fired by a local sender
     */
    public void fireEvent(IDCCServerEvent event, boolean local) {
        fireEventInMap(getLocalEvents(), event);
    }

    /**
     * This function creates an event firing request and notifies the event
     * processor thread to pick it up when it has a chance.
     * 
     * @param map
     *            map containing the registered listeners
     * @param event
     *            event to be fired
     */
    protected void fireEventInMap(Map<String, Set<IDestinyEventListener>> map, IDCCServerEvent event) {
        final FireEventRequest eventFiringRequest = new FireEventRequest(map, event);
        synchronized (firedEventQueue) {
            firedEventQueue.add(eventFiringRequest);
            firedEventQueue.notify();
        }
    }

    /**
     * 
     * @param map
     *            map to store the listener into
     * @param eventName
     *            name of the event
     * @param listener
     *            callback interface when that event fires
     */
    protected void registerEventInMap(Map<String, Set<IDestinyEventListener>> map, IDestinyEventListener listener, String eventName) {
        synchronized (map) {
            //See if this event has already at least one listener
            if (map.get(eventName) != null) {
                //add the listeener to the set
                Set<IDestinyEventListener> listeners = map.get(eventName);
                listeners.add(listener);
            } else {
                Set<IDestinyEventListener> newListeners = new HashSet<IDestinyEventListener>();
                newListeners.add(listener);
                map.put(eventName, newListeners);
            }
        }
    }

    /**
     * Removes an event listener for a given event name from the map
     * 
     * @param map
     *            map containing the registered listeners
     * @param listener
     *            listener to be removed
     * @param eventName
     *            name of the event the listener registered to
     */
    protected void removeEventListenerInMap(Map<String, Set<IDestinyEventListener>> map, IDestinyEventListener listener, String eventName) {
        synchronized (map) {
            if (map.get(eventName) != null) {
                Set<IDestinyEventListener> listeners = map.get(eventName);
                if (listeners.contains(listener)) {
                    listeners.remove(listener);
                    if (listeners.size() == 0) {
                        //We just removed the last listener for the event
                        map.remove(eventName);
                    }
                }
            }
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
        removeEventListenerInMap(getLocalEvents(), listener, eventName);
    }

    /**
     * Shutdown the event manager
     */
    public void shutdown() {
        eventProcessorThread.interrupt();
    }

    /**
     * This is the event dispatcher class. This class role is to asynchronously
     * dispatch events to the event listeners. This thread processes one event
     * at a time. When it finishes processing one event, it checks the event
     * firing queue and picks up any pending event firing request, or it waits
     * for the next event firing request to happen.
     * 
     * @author ihanen
     */
    private class EventProcessorThread extends Thread {

        private List<FireEventRequest> eventQueue;

        /**
         * Constructor
         * 
         * @param queue
         *            queue containing the list of fired event. The thread will
         *            be watching this queue.
         */
        public EventProcessorThread(List<FireEventRequest> queue) {
            setName("SharedCtxEventProc");
            setDaemon(true);
            if (queue == null) {
                throw new NullPointerException("event queue cannot be null");
            }
            eventQueue = queue;
        }

        /**
         * Processes the event and actually calls all the registered listeners.
         * Rather than locking the list of listeners for the entire time, this
         * function "clones" the list of listeners and then processes it. If
         * this function was locking the map for the entire time, there could be
         * a deadlock risk if listener was making a new event registration or
         * unregistration.
         * 
         * @param eventRequest
         *            event firing request
         */
        private void processEvent(FireEventRequest eventRequest) {
            Set<IDestinyEventListener> listenersToNotify = null;
            final IDCCServerEvent eventToFire = eventRequest.getEvent();
            final String eventName = eventToFire.getName();
            final Map<String, Set<IDestinyEventListener>> eventMap = eventRequest.getEventMap();

            synchronized (eventMap) {
                Set<IDestinyEventListener> registeredListeners = eventMap.get(eventName);
                if (registeredListeners != null) {
                    listenersToNotify = new HashSet<IDestinyEventListener>(registeredListeners);
                } else {
                    listenersToNotify = EMPTY_SET;
                }
            }

            //Now that the copy is performed, notify the listeners
            for (IDestinyEventListener listener : listenersToNotify) {
                //Classloader needs to be set in the context of the listener
                final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                final ClassLoader listenerClassLoader = listener.getClass().getClassLoader();
                Thread.currentThread().setContextClassLoader(listenerClassLoader);
                try {
                    listener.onDestinyEvent(eventToFire);
                } catch (Throwable e) {
                    getLog().error("Event notification for: '" + eventToFire.getName() + "' caused exception in listener: '" + listener.getClass().getName() + "'", e);
                } finally {
                    Thread.currentThread().setContextClassLoader(contextClassLoader);
                }
            }
        }

        /**
         * This is the main function. The thread either wait of picks up new
         * elements from the queue and processes them.
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            super.run();
            while (!isInterrupted()) {
                FireEventRequest nextEventRequest = null;
                synchronized (eventQueue) {
                    try {
                        //Wait only if queue is empty, otherwise jump to the
                        // next event to process
                        if (eventQueue.size() == 0) {
                            eventQueue.wait();
                        }

                        nextEventRequest = eventQueue.remove(0);
                    } catch (InterruptedException e) {
                        getLog().trace("Event dispatch thread interrupted, exiting the thread...", e);
                        return;
                    }
                } //End of synchronized section -- processing happens outside
                processEvent(nextEventRequest);
            }
        }
    }

    /**
     * This class represents a request to fire an event. It contains all the
     * information for the event to be processed.
     * 
     * @author ihanen
     */
    private class FireEventRequest {

        private Map<String, Set<IDestinyEventListener>> eventMap;
        private IDCCServerEvent event;

        /**
         * Constructor
         * 
         * @param newEventMap
         *            map of event listeners
         * @param newEventToFire
         *            event that should be fired to listeners
         */
        public FireEventRequest(Map<String, Set<IDestinyEventListener>> newEventMap, IDCCServerEvent newEventToFire) {
            if (newEventMap == null) {
                throw new NullPointerException("Event map cannot be null");
            }
            if (newEventToFire == null) {
                throw new NullPointerException("Event to fire cannot be null");
            }
            eventMap = newEventMap;
            event = newEventToFire;
        }

        /**
         * Returns the map of event to listeners
         * 
         * @return the map of event to listeners
         */
        public Map<String, Set<IDestinyEventListener>> getEventMap() {
            return eventMap;
        }

        /**
         * Returns the event that should be fired
         * 
         * @return the event that should be fired
         */
        public IDCCServerEvent getEvent() {
            return event;
        }
    }
}
