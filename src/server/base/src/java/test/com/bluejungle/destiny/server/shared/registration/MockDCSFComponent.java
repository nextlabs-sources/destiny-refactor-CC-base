/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis.types.URI;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatResponseImpl;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/registration/tests/MockDCSFContainer.java#3 $:
 */

public class MockDCSFComponent extends MockDCCComponent implements IRegisteredDCSFComponent {

    private URI mockURI;
    private List eventRegistrationRequests;
    private List eventUnRegistrationRequests;
    private List dmsRegistrationRequests;
    private List dmsUnregistrationRequests;
    private List remoteEventsFired;
    private List heartbeatRequests;

    /**
     * Constructor
     */
    public MockDCSFComponent() {
        super(ServerComponentType.DCSF);
        try {
            this.mockURI = new URI("http://mockurl.com");
        } catch (URI.MalformedURIException e) {
            this.mockURI = null;
        }
        this.eventRegistrationRequests = new ArrayList();
        this.eventUnRegistrationRequests = new ArrayList();
        this.remoteEventsFired = new ArrayList();
        this.dmsRegistrationRequests = new ArrayList();
        this.dmsUnregistrationRequests = new ArrayList();
        this.heartbeatRequests = new ArrayList();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#registerComponentWithDMS(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo,
     *      com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener)
     */
    public void registerComponentWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
        this.dmsRegistrationRequests.add(new MockDMSRegistrationRequest(regInfo, callback));
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#unregisterComponentWithDMS(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo)
     */
    public void unregisterComponentWithDMS(IDCCRegistrationInfo info) {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#registerForRemoteEvent(java.lang.String)
     */
    public void registerForRemoteEvent(String eventName) {
        this.eventRegistrationRequests.add(eventName);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#unregisterForRemoteEvent(java.lang.String)
     */
    public void unregisterForRemoteEvent(String eventName) {
        this.eventUnRegistrationRequests.add(eventName);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#fireRemoteEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent, java.net.URL)
     */
    public void fireRemoteEvent(IDCCServerEvent event, URL remoteLocation) {
        this.remoteEventsFired.add(new MockRemoteEventFiredRequest(event, remoteLocation));
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DCSF;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentName()
     */
    public String getComponentName() {
        return "";
    }

    /**
     * Resets the tracking variables
     */
    public void reset() {
        this.eventRegistrationRequests.clear();
        this.eventUnRegistrationRequests.clear();
        this.remoteEventsFired.clear();
        this.dmsRegistrationRequests.clear();
        this.dmsUnregistrationRequests.clear();
        this.heartbeatRequests.clear();
    }

    /**
     * Returns the list of event registration requests
     * 
     * @return the list of event registration requests
     */
    public List getEventRegistrationRequests() {
        return this.eventRegistrationRequests;
    }

    /**
     * Returns the mock URI
     * 
     * @return the mock URI
     */
    public URI getMockURI() {
        return this.mockURI;
    }

    /**
     * Returns the list of event unregistration requests
     * 
     * @return the list of event unregistration requests
     */
    public List getEventUnRegistrationRequests() {
        return this.eventUnRegistrationRequests;
    }

    /**
     * Returns the list of remote events that have been fired
     * 
     * @return the list of remote events that have been fired
     */
    public List getRemoteEventsFired() {
        return this.remoteEventsFired;
    }

    /**
     * Returns the list of DMS registration requests
     * 
     * @return the list of DMS registration requests
     */
    public List getDmsRegistrationRequests() {
        return this.dmsRegistrationRequests;
    }

    /**
     * Returns the list of DMS unregistration requests
     * 
     * @return the list of DMS unregistration requests
     */
    public List getDmsUnregistrationRequests() {
        return this.dmsUnregistrationRequests;
    }

    /**
     * @param heartbeat
     * @return update
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#sendHeartbeat(com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo)
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat) {
        this.heartbeatRequests.add(heartbeat);
        return (new ComponentHeartbeatResponseImpl());
    }

    /**
     * Returns the heartbeat requests.
     * 
     * @return the heartbeatrequests.
     */
    public List getHeartbeatRequests() {
        return this.heartbeatRequests;
    }
}