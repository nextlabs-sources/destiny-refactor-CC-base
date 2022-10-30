/*
 * Created on Feb 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

/**
 * This is the deployment request execution interface.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/IDeploymentRequestExecutor.java#1 $
 */

public interface IDeploymentRequestExecutor {

    /**
     * Executes a given policy deployment request.
     * 
     * @param id
     *            id of the request to be executed
     * @throws RequestAlreadyExecutedException
     *             if the request has already been processed.
     * @throws UnknownDeploymentIdException
     *             if the id of the deployment request is unknown
     */
    public void executeDeploymentRequest(Long id) throws RequestAlreadyExecutedException, UnknownDeploymentIdException;
}