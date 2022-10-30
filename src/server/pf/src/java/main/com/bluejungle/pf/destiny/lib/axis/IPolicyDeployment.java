/*
 * Created on Feb 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.destiny.lib.axis;

import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.version.IVersion;

/**
 * Axis-specific interface for policy deployment service.
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lib/axis/IPolicyDeployment.java#1 $:
 */

public interface IPolicyDeployment {
    
    String COMP_NAME = IPolicyDeployment.class.getName();
    
    /**
     * Returns a deployment bundle for the given agent
     * 
     * @param request deployment request
     * @param agentId id of the agent
     * @param agentDomain the agent's domain
     * @param agentVersion The version of the agent requesting the bundle.
     * @return deployment bundle for the given agent
     */
    public IDeploymentBundle getDeploymentBundle(
        DeploymentRequest request
    ,   Long agentId
    ,   String agentDomain
    ,   IVersion agentVersion
    );
}
