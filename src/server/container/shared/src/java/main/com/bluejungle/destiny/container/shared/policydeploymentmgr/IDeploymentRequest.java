/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

import java.util.Calendar;
import java.util.List;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/policydeploymentmgr/IDeploymentRequest.java#1 $
 */

public interface IDeploymentRequest {

    /**
     * Adds a new target for the deployment request
     * 
     * @param hostname
     *            hostname where the deployment should occur
     */
    public void addTargetHost(String hostname);

    /**
     * Returns whether a deployment request has been asked to execute
     * 
     * @return true if the deployment request has been executed, false otherwise
     */
    public boolean getExecuted();

    /**
     * Returns the Id of the deployment request
     * 
     * @return the Id of the deployment request
     */
    public Long getId();

    /**
     * Return the list of targets (hostnames) where the deployment should occur
     * 
     * @return the list of targets (hostnames) where the deployment should occur
     */
    public List getTargetHosts();

    /**
     * Returns the time of the deployment request
     * 
     * @return the time of the deployment request
     */
    public Calendar getTime();
    
    /**
     * Returns the scheduled deployment time of the request
     * 
     * @return the scheduled time of the request
     */
    public Calendar getScheduleTime();
    
}