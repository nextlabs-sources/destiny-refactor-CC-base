/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatResponseImpl;

/**
 * This is a dummy registration manager in the shared context
 * 
 * @author ihanan
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/test/com/bluejungle/destiny/container/dms/components/compmgr/MockRegistrationManager.java#1 $
 */

public class MockRegistrationManager implements IDestinyRegistrationManager {

    private List heartbeatRequests;

    /**
     * Constructor
     */
    public MockRegistrationManager() {
        super();
        this.heartbeatRequests = new ArrayList();
    }

    /**
     * Resets the dummy object
     */
    public void reset() {
        this.heartbeatRequests.clear();
    }

    /**
     * @param listener
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#addDCSFRegistrationListener(com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener)
     */
    public void addDCSFRegistrationListener(IDCSFRegistrationListener listener) {
    }

    /**
     * @param component
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#registerComponent(com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent)
     */
    public void registerComponent(IRegisteredDCCComponent component) {
    }

    /**
     * @param component
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#unregisterComponent(com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent)
     */
    public void unregisterComponent(IRegisteredDCCComponent component) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#sendHeartbeat(com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo)
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat) {
        this.heartbeatRequests.add(heartbeat);
        return new ComponentHeartbeatResponseImpl();
    }

    /**
     * Returns the heartbeat requests.
     * 
     * @return the heartbeat requests.
     */
    public List getHeartbeatRequests() {
        return this.heartbeatRequests;
    }

    /**
     * @param component
     *            component to unregister
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#unregisterWithDMS(com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent)
     */
    public void unregisterWithDMS(IRegisteredDCCComponent component) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#getDefaultRegistrationInfo()
     */
    public IDCCRegistrationInfo getDefaultRegistrationInfo() {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#registerWithDMS(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo,
     *      com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener)
     */
    public void registerWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
    }

}