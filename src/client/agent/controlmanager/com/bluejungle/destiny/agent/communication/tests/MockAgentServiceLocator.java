/*
 * Created on Dec 14, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication.tests;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.communication.CommunicationManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.services.agent.AgentServiceIF;
import com.bluejungle.destiny.services.agent.AgentServiceLocator;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class MockAgentServiceLocator extends AgentServiceLocator {

    private static AgentServiceIF agentServiceIF = null; 
    
    /**
     * @see com.bluejungle.destiny.services.agent.AgentService#getAgentServiceIFPort()
     */
    public AgentServiceIF getAgentServiceIFPort() throws ServiceException {

        return agentServiceIF;
    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceLocator#setAgentServiceIFPortEndpointAddress(java.lang.String)
     */
    public void setAgentServiceIFPortEndpointAddress(String arg0) {
        IControlManager controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
        if (controlManager == null) {
            TestCase.fail("Control Manager not available.");
        }
        TestCase.assertEquals("Unexpected Agent Service endpoint address", controlManager.getCommunicationProfile().getDABSLocation().toString() + CommunicationManager.AGENT_SERVICE_SUFFIX, arg0);
        agentServiceIF = new MockAgentServiceImpl ();
        ((MockAgentServiceImpl)agentServiceIF).setEndpoint(arg0);

        super.setAgentServiceIFPortEndpointAddress(arg0);
    }
}