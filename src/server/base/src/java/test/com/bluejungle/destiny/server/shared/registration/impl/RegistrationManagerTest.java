/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.MockDCCComponent;
import com.bluejungle.destiny.server.shared.registration.MockDCSFComponent;
import com.bluejungle.destiny.server.shared.registration.MockDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.MockDMSRegistrationRequest;
import com.bluejungle.destiny.server.shared.registration.MockDSCFRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * This is the registration manager test class
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/registration/tests/RegistrationManagerTest.java#1 $:
 */

public class RegistrationManagerTest extends TestCase {

    /**
     * Constructor
     */
    public RegistrationManagerTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     */
    public RegistrationManagerTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the DCSF registration listener is notified
     * properly when the DCSF container registers, and unregisters.
     */
    public void testDCSFRegistrationListener() {
        MockDSCFRegistrationListener listener = new MockDSCFRegistrationListener();
        MockDSCFRegistrationListener listener2 = new MockDSCFRegistrationListener();
        IDestinyRegistrationManager registrationMgr = new DestinyRegistrationManagerImpl();
        IRegisteredDCCComponent dabsComponent = new MockDCCComponent(ServerComponentType.DABS);
        IRegisteredDCCComponent dcsfComponent = new MockDCSFComponent();
        registrationMgr.addDCSFRegistrationListener(listener);
        registrationMgr.addDCSFRegistrationListener(listener2);

        //Make sure the listener is notified only if the component is DCSF
        registrationMgr.registerComponent(dabsComponent);
        assertNull(listener.getRegisteredDCSF());
        assertNull(listener2.getRegisteredDCSF());
        registrationMgr.registerComponent(dcsfComponent);
        assertEquals(listener.getRegisteredDCSF(), dcsfComponent);
        assertEquals(listener2.getRegisteredDCSF(), dcsfComponent);
        listener.reset();
        listener2.reset();
        registrationMgr.unregisterComponent(dabsComponent);
        assertNull(listener.getRegisteredDCSF());
        assertNull(listener2.getRegisteredDCSF());
        registrationMgr.unregisterComponent(dcsfComponent);
        assertEquals(listener.getRegisteredDCSF(), dcsfComponent);
        assertEquals(listener2.getRegisteredDCSF(), dcsfComponent);
        listener.reset();
        listener2.reset();
    }

    /**
     * This test verifies that the DMS registration requests with are not lost
     * if the DCSF component is not registered yet.
     */
    public void testWaitForDCSFWebApp() {
        URL dcsfURL = null;
        try {
            dcsfURL = new URL("http://dcsfHost/dcsfApp/dcsfService");
        } catch (MalformedURLException ex) {
            fail("URI correcly formed");
        }
        IDestinyRegistrationManager registrationMgr = new DestinyRegistrationManagerImpl();
        MockDCSFComponent dcsfComponent = new MockDCSFComponent();
        MockDMSRegistrationListener listener1 = new MockDMSRegistrationListener();
        MockDMSRegistrationListener listener2 = new MockDMSRegistrationListener();

        //Register one DABS
        DCCRegistrationInfoImpl dabsRegInfo = new DCCRegistrationInfoImpl();
        dabsRegInfo.setComponentName("DABS #1");
        dabsRegInfo.setComponentType(ServerComponentType.DABS);
        dabsRegInfo.setEventListenerURL(dcsfURL);
        registrationMgr.registerWithDMS(dabsRegInfo, listener1);
        assertNull(listener1.getRegistrationStatus());

        //Register one DPS
        DCCRegistrationInfoImpl dpsRegInfo = new DCCRegistrationInfoImpl();
        dpsRegInfo.setComponentName("DPS #1");
        dpsRegInfo.setComponentType(ServerComponentType.DABS);
        dpsRegInfo.setEventListenerURL(dcsfURL);
        registrationMgr.registerWithDMS(dpsRegInfo, listener2);
        assertNull(listener1.getRegistrationStatus());
        assertNull(listener2.getRegistrationStatus());

        //Finally, DCSF comes up!
        registrationMgr.registerComponent(dcsfComponent);
        List requests = dcsfComponent.getDmsRegistrationRequests();
        assertEquals(requests.size(), 2);
        assertEquals(((MockDMSRegistrationRequest) requests.get(0)).getDmsRegistrationlistener(), listener1);
        assertEquals(((MockDMSRegistrationRequest) requests.get(0)).getRegistrationInfo(), dabsRegInfo);
        assertEquals(((MockDMSRegistrationRequest) requests.get(1)).getDmsRegistrationlistener(), listener2);
        assertEquals(((MockDMSRegistrationRequest) requests.get(1)).getRegistrationInfo(), dpsRegInfo);

        //Now, test the case where DCC component register already after DCSF is
        // up
        dcsfComponent.reset();
        requests = dcsfComponent.getDmsRegistrationRequests();
        assertEquals(requests.size(), 0);
        registrationMgr.registerComponent(dcsfComponent);
        requests = dcsfComponent.getDmsRegistrationRequests();
        assertEquals(requests.size(), 0);
        registrationMgr.registerWithDMS(dabsRegInfo, listener1);
        assertNull(listener1.getRegistrationStatus());
        requests = dcsfComponent.getDmsRegistrationRequests();
        assertEquals(requests.size(), 1);
        assertEquals(((MockDMSRegistrationRequest) requests.get(0)).getDmsRegistrationlistener(), listener1);
        assertEquals(((MockDMSRegistrationRequest) requests.get(0)).getRegistrationInfo(), dabsRegInfo);
    }

    /**
     * This test verifies that the registration for a given component does not
     * happen twice, and that the listeners for DCSF do not get fired twice
     */
    public void testNoDoubleComponentRegistration() {
        String dcsfURL = "http://dcsfHost/dcsfApp/dcsfService";
        IDestinyRegistrationManager registrationMgr = new DestinyRegistrationManagerImpl();
        MockDCSFComponent dcsfComponent = new MockDCSFComponent();
        MockDSCFRegistrationListener listener = new MockDSCFRegistrationListener();
        MockDSCFRegistrationListener listener2 = new MockDSCFRegistrationListener();
        registrationMgr.addDCSFRegistrationListener(listener);
        registrationMgr.addDCSFRegistrationListener(listener2);

        registrationMgr.registerComponent(dcsfComponent);
        assertEquals(listener.getRegisteredDCSF(), dcsfComponent);
        assertEquals(listener2.getRegisteredDCSF(), dcsfComponent);
        listener.reset();
        listener2.reset();

        //Now, register again for DCSF (should not happen, but...)
        registrationMgr.registerComponent(dcsfComponent);
        assertNull(listener.getRegisteredDCSF());
        assertNull(listener2.getRegisteredDCSF());
    }

    /**
     * This test verifies that the DCC components heartbeats are sent properly
     * when a DCC component submits a heartbeat.
     *  
     */
    public void testHeartBeatForwarding() {
        IDestinyRegistrationManager registrationMgr = new DestinyRegistrationManagerImpl();
        MockDCSFComponent dcsfComponent = new MockDCSFComponent();

        ComponentHeartbeatInfoImpl hbInfo = new ComponentHeartbeatInfoImpl();
        hbInfo.setComponentName("dabs-1");
        IComponentHeartbeatResponse update = registrationMgr.sendHeartbeat(hbInfo);
        assertNotNull("Dummy update should be sent if DCSF is not available", update);

        //Now register DCSF - and try again
        registrationMgr.registerComponent(dcsfComponent);
        update = registrationMgr.sendHeartbeat(hbInfo);
        assertNotNull("Shared context should return an update after heartbeat", update);

        List hbRequests = dcsfComponent.getHeartbeatRequests();
        assertNotNull("Registration mgr in shared context should forward heartbeat request to DCSF web app", hbRequests);
        assertEquals("Registration mgr in shared context should forward heartbeat request to DCSF web app", 1, hbRequests.size());
        IComponentHeartbeatInfo info = (IComponentHeartbeatInfo) hbRequests.get(0);
        assertEquals(hbInfo.getComponentName(), info.getComponentName());
        dcsfComponent.reset();
    }
}
