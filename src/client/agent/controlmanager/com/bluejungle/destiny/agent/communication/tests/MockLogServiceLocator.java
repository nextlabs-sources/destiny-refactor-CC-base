/*
 * Created on Dec 14, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.communication.tests;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.communication.CommunicationManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;

import com.nextlabs.destiny.interfaces.log.v5.LogServiceIF;
import com.nextlabs.destiny.services.log.v5.LogServiceV5Locator;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/communication/tests/MockLogServiceLocator.java#1 $:
 */

public class MockLogServiceLocator extends LogServiceV5Locator {

    private static LogServiceIF logServiceIF = null; 

    /**
     * @see com.bluejungle.destiny.services.log.LogService#getLogServiceIFPort()
     */
    @Override
    public LogServiceIF getLogServiceIFPortV5() throws ServiceException {
        return logServiceIF;
    }
    /**
     * @see com.bluejungle.destiny.services.log.LogServiceLocator#setLogServiceIFPortEndpointAddress(java.lang.String)
     */
    @Override
    public void setLogServiceIFPortV5EndpointAddress(String arg0) {
        IControlManager controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
        if (controlManager == null) {
            TestCase.fail("Control Manager not available.");
        }
        TestCase.assertEquals("Unexpected Log Service endpoint address", controlManager.getCommunicationProfile().getDABSLocation().toString() + CommunicationManager.LOG_SERVICE_SUFFIX, arg0);
        logServiceIF = new MockLogServiceImpl();
        ((MockLogServiceImpl)logServiceIF).setEndpoint(arg0);
        super.setLogServiceIFPortV5EndpointAddress(arg0);
    }
}
