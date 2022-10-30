/*
 * Created on Jan 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentStartupConfiguration.java#1 $
 */

public interface IAgentStartupConfiguration {

    /**
     * Gets the id that this agent has been assigned. This is unique among all
     * agents in the Destiny system.
     * 
     * @return id
     */
    public Long getId();

    /**
     * Gets the registration id for the registration request
     * 
     * @return registration id
     */
    public Long getRegistrationId();

    /**
     * Gets the comm profile to be sent to the agent
     * 
     * @return comm profile
     */
    public ICommProfileDO getCommProfile();

    /**
     * Gets the agent profile to be sent to the agent
     * 
     * @return agent profile
     */
    public IAgentProfileDO getAgentProfile();
}