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
 * This class is a component of the IAgentDO class that holds the last known
 * status of the profiles that this agent has.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/agentmgr/IAgentProfileStatus.java#1 $
 */

public interface IAgentProfileStatus {

    /**
     * Checks whether the last profile acknowledgement status matches the
     * current profile status provided by the agent via the
     * IAgentProfileStatusData object.
     * 
     * @param status
     *            data
     * @return whether it matches up with the last acknowleded status
     */
    /* public boolean equals(IAgentProfileStatusData data); */

    /**
     * Returns the timestamp of the last profile update that was received (and
     * committed) by the agent. This is used for admin purposes.
     * 
     * @return timestamp
     */
    public Calendar getLastAcknowledgedCommProfileTimestamp();

    /**
     * Returns the name of the last acknowledged comm profile update.
     * 
     * @return name
     */
    public String getLastAcknowledgedCommProfileName();

    /**
     * Returns the timestamp of the last profile update that was received (and
     * committed) by the agent. This is used for admin purposes.
     * 
     * @return timestamp
     */
    public Calendar getLastAcknowledgedAgentProfileTimestamp();

    /**
     * Returns the name of the last acknowledged agent profile update.
     * 
     * @return name
     */
    public String getLastAcknowledgedAgentProfileName();
}