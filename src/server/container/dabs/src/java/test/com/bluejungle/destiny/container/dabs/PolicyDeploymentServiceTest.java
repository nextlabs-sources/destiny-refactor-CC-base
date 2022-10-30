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

import com.bluejungle.destiny.services.deployment.PolicyDeploymentServiceIF;
import com.bluejungle.destiny.services.deployment.PolicyDeploymentServiceLocator;
import com.bluejungle.destiny.services.deployment.types.PolicyPushList;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * This is the policy deployment service test class
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/PolicyDeploymentServiceTest.java#1 $ 
 */
public class PolicyDeploymentServiceTest extends BaseDestinyTestCase {

    private PolicyDeploymentServiceIF deploymentService;

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public PolicyDeploymentServiceTest(String testName) {
        super(testName);
    }

    /**
     * Sets up the test
     * @throws ServiceException if the deployment service creation fails
     */
    protected void setUp() throws ServiceException {
        PolicyDeploymentServiceLocator locator = new PolicyDeploymentServiceLocator();
        locator.setPolicyDeploymentServiceIFPortEndpointAddress("http://localhost:8081/dabs/services/PolicyDeploymentServiceIFPort");
        this.deploymentService = locator.getPolicyDeploymentServiceIFPort();
    }

    /**
     * Tests the policy invalidation
     */
    public void testInvalidatePolicy() {
        String result = "";
        try {
            result = this.deploymentService.invalidatePolicy();
        } catch (RemoteException e) {
            assertTrue(false);
        }

        assertEquals(result, "OK"); //For now.
    }

    /**
     * Tests the policy push
     */
    public void testPushPolicy() {
        String result = "";
        try {
            PolicyPushList list = new PolicyPushList();
            result = this.deploymentService.pushPolicy(null);
        } catch (RemoteException e) {
            assertTrue(false);
        }

        assertEquals(result, "OK"); //For now.
    }
}