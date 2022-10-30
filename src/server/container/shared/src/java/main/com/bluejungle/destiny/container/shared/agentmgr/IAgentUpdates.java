/*
 * Created on Jan 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;

/**
 * Interface to represent updates from the server to the agent
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentUpdates.java#1 $
 */

public interface IAgentUpdates {

    /**
     * Gets the comm profile to be sent to the agent
     * 
     * @return comm profile
     */
    public ICommProfileDO getCommProfileUpdate();

    /**
     * Gets the agent profile to be sent to the agent
     * 
     * @return agent profile
     */
    public IAgentProfileDO getAgentProfileUpdate();

    /**
     * Returns whether there are any updates to be applied
     * 
     * @return boolean
     */
    public boolean hasUpdates();
}