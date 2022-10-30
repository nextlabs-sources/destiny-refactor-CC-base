/*
 * Created on Dec 10, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * Mock Class for the DCSF Component
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockDCSFComponentImpl.java#1 $:
 */

public class MockDCSFComponentImpl implements IRegisteredDCSFComponent {

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#registerComponentWithDMS(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo,
     *      com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener)
     */
    public void registerComponentWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#registerForRemoteEvent(java.lang.String)
     */
    public void registerForRemoteEvent(String eventName) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#unregisterForRemoteEvent(java.lang.String)
     */
    public void unregisterForRemoteEvent(String eventName) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#fireRemoteEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent,
     *      java.net.URL)
     */
    public void fireRemoteEvent(IDCCServerEvent event, URL remoteLocation) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DCSF;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#sendHeartbeat(com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo)
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat) {
        return null;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#unregisterComponentWithDMS(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo)
     */
    public void unregisterComponentWithDMS(IDCCRegistrationInfo info) {
    }

    /**
     * Returns the component name
     * 
     * @return the component name
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentName()
     */
    public String getComponentName() {
        return null;
    }

}