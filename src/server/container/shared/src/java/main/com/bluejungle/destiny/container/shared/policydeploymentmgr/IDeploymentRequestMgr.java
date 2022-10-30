/*
 * Created on Feb 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

import java.util.Calendar;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * This is the deployment manager request interface
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/IDeploymentRequestMgr.java#1 $
 */

public interface IDeploymentRequestMgr extends IConfigurable, IDisposable, IInitializable, ILogEnabled {

    public static final String MGMT_DATA_SOURCE_CONFIG_PARAM = "MgmtDataSource";

    /**
     * Creates a deployment request.
     * 
     * @return a deployment request object
     */
    IDeploymentRequest createDeploymentRequest();
    
    /**
     * Creates a deployment request.
     * 
     * @param scheduledTime
     * 			the scheduled time to deploy
     * @return a deployment request object
     */
    IDeploymentRequest createDeploymentRequest(Calendar scheduledTime);

    /**
     * Retrieve a particular deployment request
     * 
     * @param id
     *            deployment request id
     * @return a deployment request object
     */
    IDeploymentRequest getDeploymentRequest(Long id);

    /**
     * Updates and commits an existing deployment request
     * 
     * @param updatedDeploymentRequest
     *            updated deployment request
     */
    void saveDeploymentRequest(IDeploymentRequest updatedDeploymentRequest);
}