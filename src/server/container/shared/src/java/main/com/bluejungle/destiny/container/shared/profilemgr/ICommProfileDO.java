/*
 * Created on Jan 28, 2005
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
 * The Communication Profile Data Object
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/ICommProfileDO.java#1 $
 */

public interface ICommProfileDO extends IBaseProfileDO {

    /**
     * Gets the DABSLocation value
     * 
     * @return DABSLocation
     */
    public URI getDABSLocation();

    /**
     * Sets the DABSLocation value
     * 
     * @param DABSLocation
     */
    public void setDABSLocation(URI DABSLocation);

    /**
     * Gets the heartBeatFrequency value
     * 
     * @return heartBeatFrequency
     */
    public TimeInterval getHeartBeatFrequency();

    /**
     * Sets the heartBeatFrequency value
     * 
     * @param heartBeatFrequency
     */
    public void setHeartBeatFrequency(TimeInterval heartBeatFrequency);

    /**
     * Gets the logLimit value
     * 
     * @return logLimit
     */
    public int getLogLimit();

    /**
     * Sets the logLimit value
     * 
     * @param logLimit
     */
    public void setLogLimit(int logLimit);

    /**
     * Gets the logFrequency value
     * 
     * @return logFrequency
     */
    public TimeInterval getLogFrequency();

    /**
     * Sets the logFrequency value
     * 
     * @param logFrequency
     */
    public void setLogFrequency(TimeInterval logFrequency);

    /**
     * Determine if push is enabled for this comm profile
     * 
     * @return pushInfo
     */
    public boolean isPushEnabled();

    /**
     * Set if agents with this profile will be pushed enabled
     * 
     * @param pushEnabled
     *            true if the agent will be push enabled; false otherwise
     */
    public void setPushEnabled(boolean pushEnabled);

    /**
     * Retrieve the default push port
     * 
     * @return the default push port
     */
    public int getDefaultPushPort();

    /**
     * Set the default push port
     */
    public void setDefaultPushPort(int pushPort);

    /**
     * Set the agent type for this comm profile
     */
    public void setAgentType(IAgentType agentType);

    /**
     * Retrieve the agent type for this comm profile
     * 
     * @return the agent type for this comm profile
     */
    public IAgentType getAgentType();

    /**
     * Retrieve the currently assigned customer activity journaling settings
     * 
     * @return the currently assigned customer activity journaling settings
     */
    public IActivityJournalingSettings getCurrentJournalingSettings();

    /**
     * Set the currently assigned activity journaling settings
     * 
     * @param journalingSettings
     */
    public void setCurrentJournalingSettings(IActivityJournalingSettings journalingSettings);

    /**
     * Retrieve the custom activity journaling settings for this profile. Note
     * that this may not be the same as the journaling settings currently
     * assigned to this profile. All changes to the custom journaling settings
     * for this profile can be made through the returned object
     * 
     * @return the custom activity journaling settings for this profile
     */
    public ICustomizableActivityJournalingSettings getCustomJournalingSettings();

		/**
	 * Retrieve the password hash assigned to the profile
	 *
	 * @return the password hash assigned to this comm profile
	 */
	public byte[] getPasswordHash();

    /**
     * Set the password.  Will be hashed before storing
     * 
     * @param password
     *            the password assigned to this comm profile
     */
	public void setPassword(String password);
}
