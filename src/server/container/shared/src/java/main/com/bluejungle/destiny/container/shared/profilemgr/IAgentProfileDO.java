/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

/**
 * The Agent Profile Data Object
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/IAgentProfileDO.java#1 $
 */

public interface IAgentProfileDO extends IBaseProfileDO {

    /**
     * Gets the logViewingEnabled value
     * 
     * @return logViewingEnabled
     */
    public boolean isLogViewingEnabled();

    /**
     * Sets the logViewingEnabled value
     * 
     * @param logViewingEnabled
     */
    public void setLogViewingEnabled(boolean logViewingEnabled);    

    /**
     * Gets the trayIconEnabled value
     * 
     * @return trayIconEnabled
     */
    public boolean isTrayIconEnabled();

    /**
     * Sets the trayIconEnabled value
     * 
     * @param trayIconEnabled
     */
    public void setTrayIconEnabled(boolean trayIconEnabled);        
}