/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

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
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/MockRegistrationManager.java#1 $:
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
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#addDCSFRegistrationListener(com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener)
     */
    public void addDCSFRegistrationListener(IDCSFRegistrationListener listener) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#registerComponent(com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent)
     */
    public void registerComponent(IRegisteredDCCComponent component) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#unregisterComponent(com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent)
     */
    public void unregisterComponent(IRegisteredDCCComponent component) {
    }

    /**
     * 
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#sendHeartbeat(com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo)
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat) {
        this.heartbeatRequests.add(heartbeat);
        return new ComponentHeartbeatResponseImpl();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#getDefaultRegistrationInfo()
     */
    public IDCCRegistrationInfo getDefaultRegistrationInfo() {
        return null;
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
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#registerWithDMS(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo,
     *      com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener)
     */
    public void registerWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
    }
}