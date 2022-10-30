/*
 * Created on Jan 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event.defaultimpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * Default implementation of an event manager<br />
 * <br />
 * Note that this class IS thread safe. Listeners may be added/remove and events
 * may be fired concurrently
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/event/defaultimpl/EventManagerImpl.java#2 $
 */

public class EventManagerImpl implements IEventManager, IHasComponentInfo<EventManagerImpl> {

    public static final ComponentInfo COMPONENT_INFO = new ComponentInfo(IEventManager.COMPONENT_NAME, EventManagerImpl.class.getName(), IEventManager.class.getName(), LifestyleType.SINGLETON_TYPE);

    private static final Map EVENT_LISTENER_REGISTRY = Collections.synchronizedMap(new HashMap());
    private static final Map CONTEXTUAL_EVENT_LISTENER_REGISTRY = Collections.synchronizedMap(new HashMap());
    private static final Map ANY_CONTEXT_CONTEXTUAL_LISTENER_REGISTRY = Collections.synchronizedMap(new HashMap());

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo getComponentInfo() {
        return COMPONENT_INFO;
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#registerListener(com.bluejungle.destiny.policymanager.event.IEventListener,
     *      com.bluejungle.destiny.policymanager.event.EventType)
     */
    public void registerListener(IEventListener eventListener, EventType eventType) {
        if (eventListener == null) {
            throw new NullPointerException("eventListener cannot be null.");
        }

        if (eventType == null) {
            throw new NullPointerException("eventType cannot be null.");
        }

        registerListenerImpl(EVENT_LISTENER_REGISTRY, eventType, eventListener);
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#registerListener(IContextualEventListener,
     *      ContextualEventType, java.lang.Object)
     */
    public void registerListener(IContextualEventListener eventListener, ContextualEventType eventType, Object eventContext) {
        if (eventListener == null) {
            throw new NullPointerException("eventListener cannot be null.");
        }

        if (eventType == null) {
            throw new NullPointerException("eventType cannot be null.");
        }

        if (eventContext == null) {
            throw new NullPointerException("eventContext cannot be null.");
        }

        ContextualEventListenerRegistryMapKey registryKey = new ContextualEventListenerRegistryMapKey(eventType, eventContext);
        registerListenerImpl(CONTEXTUAL_EVENT_LISTENER_REGISTRY, registryKey, eventListener);
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#registerListener(com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener,
     *      com.bluejungle.destiny.policymanager.event.ContextualEventType)
     */
    public void registerListener(IMultiContextualEventListener eventListener, ContextualEventType eventType) {
        if (eventListener == null) {
            throw new NullPointerException("eventListener cannot be null.");
        }

        if (eventType == null) {
            throw new NullPointerException("eventType cannot be null.");
        }

        registerListenerImpl(ANY_CONTEXT_CONTEXTUAL_LISTENER_REGISTRY, eventType, eventListener);
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#unregisterListener(com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener,
     *      com.bluejungle.destiny.policymanager.event.ContextualEventType)
     */
    public void unregisterListener(IMultiContextualEventListener eventListener, ContextualEventType eventType) {
        if (eventListener == null) {
            throw new NullPointerException("eventListener cannot be null.");
        }

        if (eventType == null) {
            throw new NullPointerException("eventType cannot be null.");
        }

        unregisterListenerImpl(ANY_CONTEXT_CONTEXTUAL_LISTENER_REGISTRY, eventType, eventListener);
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#unregisterListener(IEventListener,
     *      com.bluejungle.destiny.policymanager.event.EventType)
     */
    public void unregisterListener(IEventListener eventListener, EventType eventType) {
        if (eventListener == null) {
            throw new NullPointerException("eventListener cannot be null.");
        }

        if (eventType == null) {
            throw new NullPointerException("eventType cannot be null.");
        }

        unregisterListenerImpl(EVENT_LISTENER_REGISTRY, eventType, eventListener);
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#unregisterListener(IContextualEventListener,
     *      ContextualEventType, java.lang.Object)
     */
    public void unregisterListener(IContextualEventListener eventListener, ContextualEventType eventType, Object eventContext) {
        if (eventListener == null) {
            throw new NullPointerException("eventListener cannot be null.");
        }

        if (eventType == null) {
            throw new NullPointerException("eventType cannot be null.");
        }

        if (eventContext == null) {
            throw new NullPointerException("eventContext cannot be null.");
        }

        ContextualEventListenerRegistryMapKey registryKey = new ContextualEventListenerRegistryMapKey(eventType, eventContext);
        unregisterListenerImpl(CONTEXTUAL_EVENT_LISTENER_REGISTRY, registryKey, eventListener);
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#fireEvent(com.bluejungle.destiny.policymanager.event.IEvent)
     */
    public void fireEvent(final IEvent eventToFire) {
        if (eventToFire == null) {
            throw new NullPointerException("eventToFire cannot be null.");
        }

        synchronized (EVENT_LISTENER_REGISTRY) {
            /**
             * We clone the listener Set because listeners may unregsiter during
             * event handling and Iteratos are fail fast. HashSet is not thread
             * safe, so we synchronize here during the clone. We could put a
             * wrapper around it instead, but since we require more coarse
             * synchronization in the register and unregsiter methods, we must
             * follow the same approach here
             */
            HashSet registeredListeners = (HashSet) EVENT_LISTENER_REGISTRY.get(eventToFire.getEventType());
            if (registeredListeners != null) {
                final Set listenersToNotify = (Set) registeredListeners.clone();

                /*
                 * Ideally, this would be outside the synchronized, but it's not
                 * a huge deal because it's async(). Can't be outside due to the
                 * anonymous class
                 */
                Display currentDisplay = Display.getCurrent();
                currentDisplay.asyncExec(new Runnable() {

                    public void run() {
                        Iterator eventListenersToNotify = listenersToNotify.iterator();
                        while (eventListenersToNotify.hasNext()) {
                            IEventListener nextListener = (IEventListener) eventListenersToNotify.next();
                            nextListener.onEvent(eventToFire);
                        }
                    }
                });
            }
        }
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#fireEvent(com.bluejungle.destiny.policymanager.event.IContextualEvent)
     */
    public void fireEvent(IContextualEvent contextualEventToFire) {
        fireEvent(Collections.singleton(contextualEventToFire));
    }

    /**
     * @see com.bluejungle.destiny.policymanager.event.IEventManager#fireEvent(Set)
     */
    public void fireEvent(Set contextualEventsToFire) {
        if (contextualEventsToFire == null) {
            throw new NullPointerException("contextualEventsToFire cannot be null.");
        }

        Iterator contextualEventIterator = contextualEventsToFire.iterator();
        while (contextualEventIterator.hasNext()) {
            IContextualEvent nextEvent = (IContextualEvent) contextualEventIterator.next();
            fireContextEventImpl(nextEvent);
        }

        fireAnyContextContextualEvents(contextualEventsToFire);
    }

    /**
     * FIX ME - Need a marker interface for event listener and event
     * 
     * @param eventListenerRegistry
     * @param registrationKey
     * @param eventListener
     */
    private void registerListenerImpl(Map eventListenerRegistry, Object registrationKey, Object eventListener) {
        synchronized (eventListenerRegistry) {
            Set registeredListeners = (Set) eventListenerRegistry.get(registrationKey);
            if (registeredListeners == null) {
                registeredListeners = new HashSet();
                eventListenerRegistry.put(registrationKey, registeredListeners);
            }
            registeredListeners.add(eventListener);
        }
    }

    /**
     * @param registry
     * @param eventType
     * @param eventListener
     */
    private void unregisterListenerImpl(Map registry, Object eventType, Object eventListener) {
        synchronized (registry) {
            Set registeredListeners = (Set) registry.get(eventType);
            if (registeredListeners != null) {
                registeredListeners.remove(eventListener);
            }
        }
    }

    /**
     * Fire a Set of contextual events to the "any context" listeners
     * 
     * @param eventsToFire
     */
    private void fireAnyContextContextualEvents(final Set eventsToFire) {
        if (!eventsToFire.isEmpty()) {
            ContextualEventType contextualEventType = ((IContextualEvent) eventsToFire.iterator().next()).getContextualEventType();

            synchronized (ANY_CONTEXT_CONTEXTUAL_LISTENER_REGISTRY) {
                /**
                 * We clone the listener Set because listeners may unregsiter
                 * during event handling and Iterators are fail fast. HashSet is
                 * not thread safe, so we synchronize here during the clone. We
                 * could put a wrapper around it instead, but since we require
                 * more coarse synchronization in the register and unregsiter
                 * methods, we must follow the same approach here
                 */
                HashSet registeredListeners = (HashSet) ANY_CONTEXT_CONTEXTUAL_LISTENER_REGISTRY.get(contextualEventType);
                if (registeredListeners != null) {
                    final Set listenersToNotify = (Set) registeredListeners.clone();

                    /*
                     * Ideally, this would be outside the synchronized, but it's
                     * not a huge deal because it's async(). Can't be outside
                     * due to the anonymous class
                     */
                    Display currentDisplay = Display.getCurrent();
                    currentDisplay.asyncExec(new Runnable() {

                        public void run() {
                            Iterator eventListenersToNotify = listenersToNotify.iterator();
                            while (eventListenersToNotify.hasNext()) {
                                IMultiContextualEventListener nextListener = (IMultiContextualEventListener) eventListenersToNotify.next();
                                nextListener.onEvents(eventsToFire);
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Fires a single contextual event to listeners listening in the event's
     * context
     * 
     * @param contextualEventToFire
     *            the event to fire
     */
    private void fireContextEventImpl(final IContextualEvent contextualEventToFire) {
        if (contextualEventToFire == null) {
            throw new NullPointerException("contextualEventToFire cannot be null.");
        }

        Object eventContext = contextualEventToFire.getEventContext();
        ContextualEventType contextualEventType = contextualEventToFire.getContextualEventType();
        ContextualEventListenerRegistryMapKey registryKey = new ContextualEventListenerRegistryMapKey(contextualEventType, eventContext);

        synchronized (CONTEXTUAL_EVENT_LISTENER_REGISTRY) {
            /**
             * We clone the listener Set because listeners may unregsiter during
             * event handling and Iterators are fail fast. HashSet is not thread
             * safe, so we synchronize here during the clone. We could put a
             * wrapper around it instead, but since we require more coarse
             * synchronization in the register and unregsiter methods, we must
             * follow the same approach here
             */
            HashSet registeredListeners = (HashSet) CONTEXTUAL_EVENT_LISTENER_REGISTRY.get(registryKey);
            if (registeredListeners != null) {
                final Set listenersToNotify = (Set) registeredListeners.clone();

                /*
                 * Ideally, this would be outside the synchronized, but it's not
                 * a huge deal because it's async(). Can't be outside due to the
                 * anonymous class
                 */
                Display currentDisplay = Display.getCurrent();
                currentDisplay.asyncExec(new Runnable() {

                    public void run() {
                        Iterator eventListenersToNotify = listenersToNotify.iterator();
                        while (eventListenersToNotify.hasNext()) {
                            IContextualEventListener nextListener = (IContextualEventListener) eventListenersToNotify.next();
                            nextListener.onEvent(contextualEventToFire);
                        }
                    }
                });
            }
        }

    }

    private class ContextualEventListenerRegistryMapKey {

        private ContextualEventType eventType;
        private Object eventContext;

        /**
         * Create an instance of EventListenerRegistryMapKey
         * 
         * @param eventType
         * @param eventContext
         */
        public ContextualEventListenerRegistryMapKey(ContextualEventType eventType, Object eventContext) {
            if (eventType == null) {
                throw new NullPointerException("eventType cannot be null.");
            }

            if (eventContext == null) {
                throw new NullPointerException("eventContext cannot be null.");
            }

            this.eventType = eventType;
            this.eventContext = eventContext;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            boolean valueToReturn = false;

            if (obj == this) {
                valueToReturn = true;
            } else if ((obj != null) && (obj instanceof ContextualEventListenerRegistryMapKey)) {
                ContextualEventListenerRegistryMapKey objectToTest = (ContextualEventListenerRegistryMapKey) obj;
                if ((this.getContextualEventType().equals(objectToTest.getContextualEventType())) && (this.getEventContext().equals(objectToTest.getEventContext()))) {
                    valueToReturn = true;
                }
            }

            return valueToReturn;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return this.getContextualEventType().hashCode() + this.getEventContext().hashCode();
        }

        /**
         * Retrieve the eventContext.
         * 
         * @return the eventContext.
         */
        private Object getEventContext() {
            return this.eventContext;
        }

        /**
         * Retrieve the eventType.
         * 
         * @return the eventType.
         */
        private ContextualEventType getContextualEventType() {
            return this.eventType;
        }

    }
}
