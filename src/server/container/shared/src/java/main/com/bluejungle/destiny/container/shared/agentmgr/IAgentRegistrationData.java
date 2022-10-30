/*
 * Created on Jan 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import com.bluejungle.version.IVersion;

/**
 * Interface for agent registration data
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/agentmgr/IRegisterAgentData.java#1 $
 */

public interface IAgentRegistrationData {

    /**
     * Returns the host name of the agent being registered
     * 
     * @return host on which agent resides
     */
    public String getHost();

    /**
     * Returns the type of the agent being registered
     * 
     * @return type of agent
     */
    public IAgentType getType();
    
    /**
     * Returns the version of the agent being registered
     * 
     * @return version of agent
     */
    public IVersion getVersion();
}
