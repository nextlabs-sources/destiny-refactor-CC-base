/*
 * Created on Nov 16, 2004
 */
package com.bluejungle.destiny.container.dabs;

import java.rmi.RemoteException;

import com.bluejungle.destiny.services.deployment.AgentDeploymentServiceIF;
import com.bluejungle.destiny.services.deployment.types.AgentDeployResult;
import com.bluejungle.destiny.services.deployment.types.AgentDeployStatus;

/**
 * @author ihanen This is the Agent deployment service. This service allows
 *         deploying agent on remote machines. For version 1.0, this service is
 *         not implemented, and returns a failure message when it is called.
 */
public class DABSAgentDeploymentServiceImpl implements AgentDeploymentServiceIF {

    /**
     * Constructor
     */
    public DABSAgentDeploymentServiceImpl() {
        super();
    }

    /**
     * Deploys an agent on a remote host
     * 
     * @param agentId
     *            id of the agent to deploy
     * @param hostName
     *            name of the machine to deploy to
     * @return status of the agent deployment
     * @throws RemoteException
     *             if deployment fails
     */
    public AgentDeployResult deployAgent(long agentId, String hostName) throws RemoteException {
        AgentDeployResult result = new AgentDeployResult();
        result.setStatus(AgentDeployStatus.Failed);
        result.setMessage("Not implemented");
        return result;
    }

    /**
     * Undeploys an agent installed on a remote host
     * 
     * @param agentId
     *            id of the agent to undeploy
     * @param hostName
     *            name of the machine to undeploy from
     * @return status of the agent undeployment
     * @throws RemoteException
     *             if undeployment fails
     */
    public AgentDeployResult undeployAgent(long agentId, String hostName) throws RemoteException {
        AgentDeployResult result = new AgentDeployResult();
        result.setStatus(AgentDeployStatus.Failed);
        result.setMessage("Not implemented");
        return result;
    }
}