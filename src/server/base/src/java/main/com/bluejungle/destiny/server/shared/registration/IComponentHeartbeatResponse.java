/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;

/**
 * This is the component heartbeat update interface. The component heartbeat
 * update sends updated information to the component as a response to its
 * heartbeat request.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/IComponentHeartbeatResponse.java#1 $
 */

public interface IComponentHeartbeatResponse {

    /**
     * Returns the cookie associated with the heartbeat
     * 
     * @return the cookie associated with the heartbeat
     */
    public IComponentHeartbeatCookie getCookie();

    /**
     * Returns the component configuration
     * 
     * @return the component configuration
     */
    public IDCCComponentConfigurationDO getConfiguration();

    /**
     * Returns the list of event registration information
     * 
     * @return the list of event registration information
     */
    public IEventRegistrationInfo[] getEventRegistrationInfo();

    /**
     * Returns the shared folder data
     * 
     * @return the shared folder data
     */
    public ISharedFolderData getSharedFolderData();
}
