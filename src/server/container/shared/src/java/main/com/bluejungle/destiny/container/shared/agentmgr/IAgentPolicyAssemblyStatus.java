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
 * status of the policy assembly that the agent has.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/agentmgr/IAgentPolicyAssemblyStatus.java#1 $
 */

public interface IAgentPolicyAssemblyStatus {

    /**
     * Checks whether the last policy assembly acknowledgement status matches
     * the current policy assembly status provided by the agent via the
     * IAgentPolicyAssemblyStatusData object.
     * 
     * @param status
     *            data
     * @return whether it matches up with the last acknowleded status
     */
    /* public boolean equals(IAgentPolicyAssemblyStatusData data); */

    /**
     * Returns the timestamp of the last policy update that this agent has.
     * 
     * @return timestamp
     */
    public Calendar getLastAcknowledgedDeploymentBundleTimestamp();
}
