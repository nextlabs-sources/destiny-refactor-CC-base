/*
 * Created on Oct 27, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.server.shared.internal;

import java.net.URL;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;

/**
 * This interface is implemented by all the registered DCSF component instances
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/registration/IRegisteredDCSFComponent.java#2 $:
 */
public interface IRegisteredDCSFComponent extends IRegisteredDCCComponent {

    public static final String REGISTRATION_MGR_CLASS_NAME = "regMgrClassName";
    public static final String EVENT_REGISTRATION_MGR_CLASS_NAME = "eventRegMgrClassName";

    /**
     * Register a DCC component with the DMS. The actual registration is done by
     * DCSF
     * 
     * @param regInfo
     *            DCC component registration information
     * @param callback
     *            callback URL for events
     */
    public void registerComponentWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback);

    /**
     * Unregister a DCC component with the DMS.
     * 
     * @param regInfo
     *            registration information (partially filled with only name and
     *            type)
     */
    public void unregisterComponentWithDMS(IDCCRegistrationInfo regInfo);

    /**
     * Register for an event fired from a remote instance (separate JVM). The
     * registration needs to be sent to DMS
     * 
     * @param eventName
     *            name of the event to register for
     */
    public void registerForRemoteEvent(String eventName);

    /**
     * Sends a heartbeat to DMS for a given DCC component
     * 
     * @param heartbeat
     *            information
     * @return any update that the DCC component needs
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat);

    /**
     * Unregister for an event fired from a remote instance (separate JVM). The
     * unregistration request needs to be sent to DMS
     * 
     * @param eventName
     *            name of the event to unregister for
     */
    public void unregisterForRemoteEvent(String eventName);

    /**
     * Fire an event to a remote instance (separate JVM).
     * 
     * @param event
     *            event object to be fired
     * @param remoteLocation
     *            location of the remote listener DCSF service
     */
    public void fireRemoteEvent(IDCCServerEvent event, URL remoteLocation);
}