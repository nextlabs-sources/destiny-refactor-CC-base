/*
 * Created on Jan 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import java.util.Calendar;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IAgentProfileStatusData.java#1 $
 */

public interface IAgentProfileStatusData {

    /**
     * Returns the timestamp of the last acknowledged agent profile update.
     * 
     * @return timestamp
     */
    public Calendar getLastCommittedAgentProfileTimestamp();

    /**
     * Returns the name of the last acknowledged agent profile update.
     * 
     * @return name
     */
    public String getLastCommittedAgentProfileName();

    /**
     * Returns the timestamp of the last acknowledged agent profile update.
     * 
     * @return timestamp
     */
    public Calendar getLastCommittedCommProfileTimestamp();

    /**
     * Returns the name of the last acknowledged comm profile update.
     * 
     * @return name
     */
    public String getLastCommittedCommProfileName();
}