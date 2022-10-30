/*
 * Created on Jan 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.impl.DestinyRemoteEventDispatcher;
import com.bluejungle.destiny.server.shared.internal.IInternalEventManager;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/RemoteListenerRegistrationMgrImpl.java#2 $
 */

public class RemoteListenerRegistrationMgrImpl implements IRemoteListenerRegistrationMgr {

    /*
     * Log related constants:
     */
    private static final MessageFormat CONFIGURATION_MSG = new MessageFormat("Configured Remote Listener Registration Manager with dcsfLocation:''{0}''");
    private static final MessageFormat REGISTER_REMOTE_LISTENER_MSG = new MessageFormat("Registered remote listener of class: ''{0}'' with callback: ''{1}'' for event ''{2}''");
    private static final MessageFormat UNREGISTER_REMOTE_LISTENER_MSG = new MessageFormat("Unregistered remote listener of class: ''{0}'' with callback: ''{1}'' for event ''{2}''");

    protected IConfiguration config;
    protected IInternalEventManager eventManager;
    protected IRegisteredDCSFComponent dcsfComponent;
    protected Log log;
    protected Map remoteListeners;

    /**
     * Sets the configuration for the component. The DCSF component object needs
     * to be passed for this component to work properly.
     * 
     * @param newConfig
     *            component configuration
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.config = newConfig;
        this.dcsfComponent = (IRegisteredDCSFComponent) newConfig.get(PARENT_DCSF_COMPONENT_CONFIG_PARAM);
        if (this.dcsfComponent == null) {
            final String errMsg = "The configuration requires a valid DCSF component";
            throw new IllegalArgumentException(errMsg);
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug(CONFIGURATION_MSG.format(new Object[] { this.dcsfComponent }));
        }
    }

    /**
     * Returns the component configuration
     * 
     * @return the component configuration
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Returns an instance of the remote event listener for the given event name
     * and callback
     * 
     * @param eventName
     *            name of the event
     * @param callback
     *            callback URL to use when the event fires
     * @return the listener for a given event name and callback
     */
    protected IDestinyEventListener getListener(String eventName, URL callback) {
        IDestinyEventListener listener = null;
        synchronized (this.remoteListeners) {
            Map callbacks = (Map) this.remoteListeners.get(eventName);
            if (callbacks == null) {
                callbacks = new HashMap();
                this.remoteListeners.put(eventName, callbacks);
            }

            listener = (IDestinyEventListener) callbacks.get(callback.toString());
            if (listener == null) {
                listener = new DestinyRemoteEventDispatcher(callback, this.dcsfComponent);
                callbacks.put(callback.toString(), listener);
                if (log.isDebugEnabled()) {
                    log.debug("Added new remote listener for event " + eventName + " on callback " + callback.toString());
                }
            }
        }
        return (listener);
    }

    /**
     * Register a remote listener to be notified when a local event fires.
     * 
     * @param eventName
     *            name of the event
     * @param callback
     *            URL of the DCSF web service that needs to be called back when
     *            the event fires
     */
    public void registerRemoteListener(String eventName, URL callback) {
        //      Create a new callback listener, and stores it into the list of remote
        // listeners
        IDestinyEventListener listener = getListener(eventName, callback);

        //Registers event with a "false" flag because the registration is
        // remote
        this.eventManager.registerForEvent(eventName, listener, false);

        if (getLog().isDebugEnabled()) {
            getLog().debug(REGISTER_REMOTE_LISTENER_MSG.format(new Object[] { listener.getClass().getName(), callback, eventName }));
        }
    }

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
    public void unregisterRemoteListener(String eventName, URL callback) {
        IDestinyEventListener listener = getListener(eventName, callback);

        //Unregisters event with a "false" flag because the unregistration is
        // remote
        this.eventManager.unregisterForEvent(eventName, listener, false);

        if (getLog().isDebugEnabled()) {
            getLog().debug(UNREGISTER_REMOTE_LISTENER_MSG.format(new Object[] { listener.getClass().getName(), callback, eventName }));
        }
    }

    /**
     * Initialize the component
     */
    public void init() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);
        this.eventManager = (IInternalEventManager) locator.getSharedContext().getEventManager();
        this.remoteListeners = new HashMap();
    }

    /**
     * This method is called before the component is destroyed.
     */
    public void dispose() {
    }

    /**
     * Sets the log object
     * 
     * @param newLog
     *            log object
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    public Log getLog() {
        return this.log;
    }
}