/*
 * Created on Jan 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

import java.net.URI;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.framework.utils.TimeInterval;


/**
 * ICommProfileData contains the information required to create a CommProfile Domain Object.
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/ICommProfileData.java#1 $
 */
public interface ICommProfileData extends IBaseProfileData {
    /**
     * Retrieve the DABSLocation value for the CommProfile instance to create.
     * 
     * @return DABSLocation
     */
    public URI getDABSLocation();

    /**
     * Retrieve the heartBeatFrequency value for the CommProfile instance to create.
     * 
     * @return heartBeatFrequency
     */
    public TimeInterval getHeartBeatFrequency();
    
    /**
     * Retrieve the logLimit value for the CommProfile instance to create.
     * 
     * @return logLimit
     */
    public int getLogLimit();
    
    /**
     * Retrieve the logFrequency value for the CommProfile instance to create.
     * 
     * @return logFrequency
     */
    public TimeInterval getLogFrequency();

    /**
     * Retrieve the default push port for the CommProfile instance to create.
     * 
     * @return the default push port
     */
    public int getDefaultPushPort();

    /**
     * Determine if push is enabled for the CommProfile instance to create.     
     * 
     * @return pushInfo
     */
    public boolean isPushEnabled();

    /**
	 * Retrieve the password to be associated with this comm profile
	 *
	 * @return the password to be associated with this comm profile
	 */
	public String getPassword();
	
    /**
     * Retrieve the agent type for this comm profile
     * 
     * @return the agent type for this comm profile
     */
    public IAgentType getAgentType();
}
