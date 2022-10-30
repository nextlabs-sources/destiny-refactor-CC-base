/*
 * Created on Dec 16, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.profile;

import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.framework.comp.IInitializable;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/profile/IProfileManager.java#1 $:
 */

public interface IProfileManager extends IInitializable{

    public static final String NAME = IProfileManager.class.getName();
    
    /**
     * Returns the agentProfile.
     * @return the agentProfile.
     */
    public abstract AgentProfileDTO getAgentProfile();

    /**
     * Sets the agentProfile and saves it to disk
     * @param agentProfile The agentProfile to set.
     */
    public abstract void setAgentProfile(AgentProfileDTO agentProfile);

    /**
     * Returns the communicationProfile.
     * @return the communicationProfile.
     */
    public abstract CommProfileDTO getCommunicationProfile();

    /**
     * Sets the communicationProfile and saves it to disk
     * @param communicationProfile The communicationProfile to set.
     */
    public abstract void setCommunicationProfile(CommProfileDTO communicationProfile);
}