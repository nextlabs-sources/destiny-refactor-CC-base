/*
 * Created on Jan 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

/**
 * Interface for agent startup data
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/agentmgr/IStartupAgentData.java#1 $
 */

public interface IAgentStartupData {

    /**
     * Returns the port on which the agent has decided to receive push
     * notifications
     * 
     * @return push port number
     */
    public Integer getPushPort();
}