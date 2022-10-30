/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatCookie;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/ComponentHeartbeatResponseImpl.java#1 $
 */

public class ComponentHeartbeatResponseImpl implements IComponentHeartbeatResponse {

    private IComponentHeartbeatCookie cookie;
    private IDCCComponentConfigurationDO configuration;
    private List<IEventRegistrationInfo> eventRegistrations = new ArrayList<IEventRegistrationInfo>();
    private ISharedFolderData sharedFolderData;

    /**
     * Constructor
     */
    public ComponentHeartbeatResponseImpl() {
        super();
    }

    /**
     * Adds a registration event object
     * 
     * @param newInfo
     *            new registration event object to add
     */
    public void addEventRegistrationInfo(IEventRegistrationInfo newInfo) {
        this.eventRegistrations.add(newInfo);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse#getCookie()
     */
    public IComponentHeartbeatCookie getCookie() {
        return this.cookie;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse#getConfiguration()
     */
    public IDCCComponentConfigurationDO getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse#getEventRegistrationInfo()
     */
    public IEventRegistrationInfo[] getEventRegistrationInfo() {
        return eventRegistrations.toArray(new IEventRegistrationInfo[0]);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse#getSharedFolderData()
     */
    public ISharedFolderData getSharedFolderData() {
        return this.sharedFolderData;
    }

    /**
     * Sets the shared folder data
     * 
     * @param sharedFolderData
     *            shared folder data to set
     */
    public void setSharedFolderData(ISharedFolderData sharedFolderData) {
        this.sharedFolderData = sharedFolderData;
    }

    /**
     * Sets the cookie
     * 
     * @param cookie
     *            cookie to set
     */
    public void setCookie(IComponentHeartbeatCookie cookie) {
        this.cookie = cookie;
    }

    /**
     * Sets the configuration
     * 
     * @param configuration
     *            configuration to set
     */
    public void setConfiguration(IDCCComponentConfigurationDO configuration) {
        this.configuration = configuration;
    }
}
