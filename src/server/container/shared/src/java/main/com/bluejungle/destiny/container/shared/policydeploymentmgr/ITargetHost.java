/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

import java.util.List;

/**
 * This is the target host interface. When policies are pushed to some hosts,
 * one deployment host instance is created for every host that needs to be
 * reached. Each host can contain one or many agents.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/policydeploymentmgr/IDeploymentTarget.java#1 $
 */

public interface ITargetHost {

    /**
     * Returns the hostname targeted for deployment
     * 
     * @return the hostname targeted for deployment
     */
    public String getHostname();

    /**
     * Returns the rowId of the record.
     * 
     * @return the rowId of the record
     */
    public Long getId();

    /**
     * Returns the list of agents associated with this deployment target.
     * 
     * @return the list of agents associated with this deployment target
     */
    public List getAgents();

    /**
     * Returns the deployment request associated with this deployment target.
     * 
     * @return the deployment request associated with this deployment target
     */
    public IDeploymentRequest getDeploymentRequest();

    /**
     * Temporary
     * @return
     */
    public Long getStatusId ();
    
    /**
     * Returns the status of the deployment for this given host.
     * 
     * @return the status of the deployment for this given host
     */
    public ITargetStatus getStatus();
}