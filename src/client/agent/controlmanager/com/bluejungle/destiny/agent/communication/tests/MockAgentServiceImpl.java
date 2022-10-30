/*
 * Created on Dec 14, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication.tests;

import java.math.BigInteger;
import java.rmi.RemoteException;

import junit.framework.TestCase;

import org.apache.axis.client.Stub;

import com.bluejungle.destiny.agent.communication.CommunicationManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.agent.AgentServiceIF;
import com.bluejungle.destiny.services.agent.types.AgentHeartbeatData;
import com.bluejungle.destiny.services.agent.types.AgentRegistrationData;
import com.bluejungle.destiny.services.agent.types.AgentShutdownData;
import com.bluejungle.destiny.services.agent.types.AgentStartupConfiguration;
import com.bluejungle.destiny.services.agent.types.AgentStartupData;
import com.bluejungle.destiny.services.agent.types.AgentUpdateAcknowledgementData;
import com.bluejungle.destiny.services.agent.types.AgentUpdates;
import com.bluejungle.destiny.services.agent.types.UserNotificationBag;
import com.bluejungle.destiny.framework.types.CommitFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.framework.types.UnknownEntryFault;
import com.bluejungle.destiny.services.management.types.AgentDTO;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * @author fuad
 * @version $Id:
 *          //depot/personal/ihanen/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/communication/tests/MockAgentServiceImpl.java#1 $:
 */

public class MockAgentServiceImpl extends Stub implements AgentServiceIF {

    private int numberOfCalls;
    private String endpoint = null;

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#registerAgent(com.bluejungle.destiny.services.agent.types.AgentRegistrationData)
     */
    public AgentStartupConfiguration registerAgent(AgentRegistrationData arg0) throws RemoteException {
        checkEndpoint();
        this.numberOfCalls++;
        return null;
    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#unregisterAgent(java.math.BigInteger)
     */
    public void unregisterAgent(BigInteger arg0) throws RemoteException, ServiceNotReadyFault, CommitFault, UnknownEntryFault, UnauthorizedCallerFault {
        // TODO Auto-generated method stub

    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#checkUpdates(com.bluejungle.destiny.services.agent.types.AgentHeartbeatData)
     */
    public AgentUpdates checkUpdates(BigInteger id, AgentHeartbeatData arg0) throws RemoteException {
        checkEndpoint();
        this.numberOfCalls++;
        return null;
    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#acknowledgeUpdates(com.bluejungle.destiny.services.agent.types.AgentUpdateAcknowledgementData)
     */
    public void acknowledgeUpdates(BigInteger id, AgentUpdateAcknowledgementData arg0) throws RemoteException {
        checkEndpoint();
        this.numberOfCalls++;
    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#startup(long,
     *      com.bluejungle.destiny.services.agent.types.AgentStartupData)
     */
    public void startupAgent(BigInteger arg0, AgentStartupData arg1) throws RemoteException {
        checkEndpoint();
        this.numberOfCalls++;
    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#shutdown(long)
     */
    public void shutdownAgent(BigInteger arg0, AgentShutdownData arg1) throws RemoteException {
        checkEndpoint();
        this.numberOfCalls++;
    }

    public AgentDTO getAgentById(BigInteger arg0) throws RemoteException {
        checkEndpoint();
        this.numberOfCalls++;
        return null;
    }

    /**
     * checks that this service impl was created with the right endpoint
     */
    private void checkEndpoint() {
        IControlManager controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
        if (controlManager == null) {
            TestCase.fail("Control Manager not available.");
        }
        TestCase.assertEquals("Unexpected Agent Service endpoint address", controlManager.getCommunicationProfile().getDABSLocation().toString() + CommunicationManager.AGENT_SERVICE_SUFFIX, this.endpoint);
    }

    /**
     * Returns the numberOfCalls.
     * 
     * @return the numberOfCalls.
     */
    public int getNumberOfCalls() {
        return this.numberOfCalls;
    }

    /**
     * Returns the endpoint.
     * 
     * @return the endpoint.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Sets the endpoint
     * 
     * @param endpoint
     *            The endpoint to set.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#sendUserNotifications(com.bluejungle.destiny.services.agent.types.UserNotificationBag)
     */
    public void sendUserNotifications(UserNotificationBag notifications) throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        if (Global.throwException) {
            throw new ServiceNotReadyFault();
        } else {
            TestCase.assertEquals("Incorrect number of notifications.", 2, notifications.getNotifications().length);
        }
    }
}
