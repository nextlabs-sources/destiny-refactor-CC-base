/*
 * Created on Nov 17, 2004
 */
package com.bluejungle.destiny.container.dabs;

import java.rmi.RemoteException;

import com.bluejungle.destiny.services.deployment.PolicyDeploymentServiceIF;
import com.bluejungle.destiny.services.deployment.types.PolicyPushList;

/**
 * This is the policy deployment service. It is mostly used to make sure that
 * policy update are properly propagated into DABS. Also, it is used if DABS
 * needs to perform a push to one or more agents.
 * 
 * @author ihanen
 */
public class DABSPolicyDeploymentServiceImpl implements PolicyDeploymentServiceIF {

    /**
     * Invalidates the local policy cache
     * 
     * @return confirmation message
     * @throws RemoteException
     *             if the policy invalidation fails
     */
    public String invalidatePolicy() throws RemoteException {
        return "OK";
    }

    /**
     * Sends a push signal to a list of hosts. The status of each push is saved
     * in the management repository.
     * 
     * @param list
     *            list of hosts to contact
     * @return status of push action
     * @throws RemoteException
     *             if the policy push request fails
     */
    public String pushPolicy(PolicyPushList list) throws RemoteException {
        return "OK";
    }
}