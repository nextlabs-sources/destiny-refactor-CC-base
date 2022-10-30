/*
 * Created on Oct 28, 2004
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * @author ihanen This is the interface implemented by any DMS registration
 *         manager.
 */
public interface IDMSRegistrationMgr extends ILogEnabled {

    public static final String COMP_NAME = "RegistrationMgr";
    public static final String DCSF_LOCATION_CONFIG_PARAM = "DCSFLocation";
    public static final String DMS_LOCATION_CONFIG_PARAM = "DMSLocation";

    /**
     * Register a DCC component with the DMS.
     * 
     * @param regInfo
     *            DCC component registration information
     * @param callback
     *            callback object that is notified when DMS registration is
     *            completed
     */
    public void registerComponentWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback);

    /**
     * Unregister a DCC component with the DMS.
     * 
     * @param regInfo
     *            partial DCC component registration information (name and type)
     */
    public void unregisterComponentWithDMS(IDCCRegistrationInfo regInfo);

    /**
     * Send a DCC component heartbeat to DMS
     * 
     * @param heartbeat
     *            heartbeat information
     * @return updates for the DCC component
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat);
}