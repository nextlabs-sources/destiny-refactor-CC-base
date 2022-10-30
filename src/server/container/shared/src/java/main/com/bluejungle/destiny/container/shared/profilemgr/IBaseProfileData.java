/*
 * Created on Jan 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

/**
 * IBaseProfileData contains the information required to create a BaseProfile Domain Object.  
 * It should be extended for specific types of profiles (user, agent, communication)
 *  
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/IBaseProfileData.java#1 $
 */
public interface IBaseProfileData {
    
    /**
     * Retrieve the name of the profile to create
     * @return the name of the profile to create
     */
    public String getName();
}
