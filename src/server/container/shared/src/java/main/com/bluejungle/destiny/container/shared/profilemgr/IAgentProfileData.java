/*
 * Created on Jan 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

/**
 * IAgentProfileData contains the information required to create an AgentProfile Domain Object.
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/IAgentProfileData.java#1 $
 */
public interface IAgentProfileData extends IBaseProfileData {
    /**
     * Retrieve the logViewingEnabled value for the AgentProfile instace to create.
     * 
     * @return logViewingEnabled
     */
    public boolean isLogViewingEnabled();
    
    /**
     * Retrieve the trayIconEnabled value for the AgentProfile instance to create.
     * 
     * @return trayIconEnabled
     */
    public boolean isTrayIconEnabled();
}
