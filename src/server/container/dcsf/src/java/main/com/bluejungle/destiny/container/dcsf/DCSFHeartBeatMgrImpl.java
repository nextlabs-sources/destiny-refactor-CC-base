/*
 * Created on Jan 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.container.dcc.DCCEvents;
import com.bluejungle.destiny.container.dcc.HeartbeatMgrImpl;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo;

/**
 * This is the heartbeat manager implementation for the DCSF component. This
 * heartbeat manager is similar to the regular one, except that it can process
 * updates sent by DMS (while typically other DCC component will not do this).
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/DCSFHeartBeatMgrImpl.java#1 $
 */

public class DCSFHeartBeatMgrImpl extends HeartbeatMgrImpl {

    private IRemoteListenerRegistrationMgr remoteListenerMgr;
    
    public DCSFHeartBeatMgrImpl() {
        super();
        super.setName("DCSFHeartBeatMgrThread");
    }

    /**
     * Init
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();

        // Initialize the remote listener registration manager:
        this.remoteListenerMgr = (IRemoteListenerRegistrationMgr) this.manager.getComponent(IRemoteListenerRegistrationMgr.COMP_NAME);
        if (this.remoteListenerMgr == null) {
            throw new RuntimeException("DCSFHeartBeatMgrImpl could not be instantiated since it relies on an IRemoteListenerRegistrationMgr instance that it could not obtain.");
        }
    }

    public void run() {
        // Before doing it's usual operations, the DCSF heartbeat manager will
        // listen for any events that should cause a heartbeat to be sent to
        // obtain the necessary updates:
        try {
            this.registerForNecessaryEvents();
            super.run();
        } catch (Throwable e) {
            log.error("Exception occurred in the heartbeat mgr thread run() method for component '" + this.componentName + "'", e);
        }
    }

    /**
     * Registers for any events that the DCSF is interested in from a heartbeat
     * perspective.
     */
    protected void registerForNecessaryEvents() {
        // Listen for event registration update events:
        this.log.info("Registering for event registration updates");
        this.sharedCtx.getEventManager().registerForEvent(DCCEvents.EVENT_REGISTRATION_UPDATES_AVAILABLE, this);
    }

    /**
     * Process updates provided during the heartbeat request. Updates will
     * consist of new event registrations. These need to be registered with the
     * remote listener manager.
     * 
     * @param update
     *            update provided by DMS
     */
    protected void processHeartBeatUpdate(IComponentHeartbeatResponse update) {
        super.processHeartBeatUpdate(update);

        // Handle new event registrations and store in list of listened events:
        IEventRegistrationInfo[] newRegistrations = update.getEventRegistrationInfo();
        if (newRegistrations != null) {
            for (int i = 0; i < newRegistrations.length; i++) {
                IEventRegistrationInfo registration = newRegistrations[i];
                // We register/un-register depending on the status of the
                // registration:
                if (registration.isActive()) {
                    this.remoteListenerMgr.registerRemoteListener(registration.getName(), registration.getCallbackURL());
                } else {
                    this.remoteListenerMgr.unregisterRemoteListener(registration.getName(), registration.getCallbackURL());
                }
            }
        }

        // Update the heartbeat with the cookie received from the DMS:
        this.heartbeat.setHeartbeatCookie(update.getCookie());
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.log.info("Un-Registering for event registration updates");
        this.sharedCtx.getEventManager().unregisterForEvent(DCCEvents.EVENT_REGISTRATION_UPDATES_AVAILABLE, this);
        super.dispose();
    }
}