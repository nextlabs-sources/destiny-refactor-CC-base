/*
 * Created on Nov 16, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dabs;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.services.deployment.AgentDeploymentServiceIF;
import com.bluejungle.destiny.services.deployment.AgentDeploymentServiceLocator;
import com.bluejungle.destiny.services.deployment.types.AgentDeployResult;
import com.bluejungle.destiny.services.deployment.types.AgentDeployStatus;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * Test case for the agent deployment service
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/com/bluejungle/destiny/container/dabs/tests/AgentDeploymentServiceTest.java#4 $
 */
public class AgentDeploymentServiceTest extends BaseDestinyTestCase {

    private AgentDeploymentServiceIF deploymentService;

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public AgentDeploymentServiceTest(String testName) {
        super(testName);
    }

    /**
     * Sets up the test
     * 
     * @throws ServiceException
     *             if setting up the service fails
     */
    protected void setUp() throws ServiceException {
        AgentDeploymentServiceLocator locator = new AgentDeploymentServiceLocator();
        locator.setAgentDeploymentServiceIFPortEndpointAddress("http://localhost:8081/dabs/services/AgentDeploymentServiceIFPort");
        this.deploymentService = locator.getAgentDeploymentServiceIFPort();
    }

    /**
     * Test the agent deployment API
     */
    public void testAgentDeploy() throws RemoteException {
        AgentDeployResult result = this.deploymentService.deployAgent(12345, "hostname.test.com");

        assertEquals(result.getStatus(), AgentDeployStatus.Failed);
    }

    /**
     * Test the agent undeployment API
     */
    public void testAgentUndeploy() {
        AgentDeployResult result = new AgentDeployResult();
        try {
            result = this.deploymentService.undeployAgent(12345, "hostname.test.com");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        assertEquals(result.getStatus(), AgentDeployStatus.Failed);
    }
}