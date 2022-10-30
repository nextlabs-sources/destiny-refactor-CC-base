/*
 * Created on Oct 24, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener;

/**
 * This is the Destiny Registration Manager interface. Objects implementing this
 * interface can register and unregister DCC components in the DCC unit.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/registration/IDestinyRegistrationManager.java#5 $:
 */
public interface IDestinyRegistrationManager {

    /**
     * Add a listener to be notified when the DCSF web application is registered
     * 
     * @param listener
     *            callback object
     */
    public void addDCSFRegistrationListener(IDCSFRegistrationListener listener);

    /**
     * Returns an empty registration information
     * 
     * @return an empty registration information
     */
    public IDCCRegistrationInfo getDefaultRegistrationInfo();

    /**
     * Register one DCC component with DCSF
     * 
     * @param component
     *            DCC component to register
     */
    public void registerComponent(IRegisteredDCCComponent component);

    /**
     * Unregister one DCC component with DCSF
     * 
     * @param component
     *            instance of the DCC component to unregister
     */
    public void unregisterComponent(IRegisteredDCCComponent component);

    /**
     * Sends heartbeat to the DMS
     * 
     * @param heartbeat
     *            heartBeat structure from the DCC mcomponent
     * @return updates for the DCC component
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat);

    /**
     * Registers one DCC component with the DMS
     * 
     * @param regInfo
     *            registration information
     * @param callback
     *            callback interface once the registration is complete
     */
    public void registerWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback);
}