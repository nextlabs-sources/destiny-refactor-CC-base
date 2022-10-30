package com.bluejungle.pf.destiny.lib;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/IPolicyQuery.java#1 $
 */

import java.util.Calendar;

import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.version.IVersion;

/**
 * This interface defines a contract for getting deployment bundles.
 */
public interface IPolicyQuery {

    /**
     * returns a deployment bundle for the requesting agent.
     * 
     * @requires agentHost is not null or emptyString
     * @param agentId id of the requesting agent
     * @param agentDomain the agent's domain
     * @param agentType type of the requesting agent
     * @param agentVersion Version of the agent querying for the bundle.
     * @param ts Timestamp of the last deployment bundle update
     * that the requesting host received.
     * The deployment bundle is non-empty only if updates have been made
     * to the assembly since
     * the host's last update. If null, then it is assumed that this is agen't
     * first deployment, and everything applicable is returned.
     * @param policyUsers set of users based on which applicable policies
     * are retrieved and packaged into the deployment bundle. If null or empty,
     * policies applicable to all users in the host's domain are retrieved.
     * @param agentHost host on which the requesting agent is running
     * @return applicable deployment bundle.
     */
    IDeploymentBundle getDeploymentBundle(
        Long[] policyUserIds
    ,   Long agentHostId
    ,   Long agentId
    ,   String agentDomain
    ,   AgentTypeEnumType agentType
    ,   IVersion agentVersion
    ,   Calendar ts
    );

}
