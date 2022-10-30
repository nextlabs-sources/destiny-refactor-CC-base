/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.context;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;

/**
 * This is a mock registration manager for testing purpose
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/context/tests/MockRegistrationMgr.java#3 $:
 */

public class MockRegistrationMgr implements IDestinyRegistrationManager {

    private List dcsfRegistrationListeners = new ArrayList();

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#sendHeartbeat(com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo)
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat) {
        return null;
    }

    /**
     * @return list of registration listeners
     */
    public List getDcsfRegistrationListeners() {
        return this.dcsfRegistrationListeners;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#getDefaultRegistrationInfo()
     */
    public IDCCRegistrationInfo getDefaultRegistrationInfo() {
        return null;
    }

    /**
     * @param listener
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#addDCSFRegistrationListener(com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener)
     */
    public void addDCSFRegistrationListener(IDCSFRegistrationListener listener) {
        this.dcsfRegistrationListeners.add(listener);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#registerWithDMS(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo,
     *      com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener)
     */
    public void registerWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
    }

    /**
     * @param component
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#unregisterWithDMS(com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent)
     */
    public void unregisterWithDMS(IRegisteredDCCComponent component) {
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
}