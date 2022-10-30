/*
 * Created on Dec 6, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.axis.types.URI;

import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.DMSRegistrationResult;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.server.shared.registration.impl.DCCRegistrationInfoImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.ITask;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.version.VersionDefaultImpl;

/**
 * This is the test class for the DMS registration manager (running within the
 * DCSF web application)
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/DMSRegistrationMgrTest.java#1 $:
 */

public class DMSRegistrationMgrTest extends TestCase {

    private static final String DMS_LOCATION = "http://dmsHost/dmsLocation";
    private static final String DCSF_LOCATION = "http://dcsfHost/dcsfLocation";
    private static final String DABS_LOCATION = "http://dcsfHost/dabsLocation";
    private static final Integer THREAD_POOL_SIZE = new Integer(5);

    /**
     * Constructor
     *  
     */
    public DMSRegistrationMgrTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test to run
     */
    public DMSRegistrationMgrTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that configuration parameters are taken into account
     * properly
     * @throws MalformedURLException 
     */
    public void testInitialization() throws MalformedURLException {
        createModifiedRegistrationManager();
        assertNotNull(MockDMSRegistrationWorker.getTaskList());
        assertEquals(0, MockDMSRegistrationWorker.getTaskList().size());
        Integer inits = MockDMSRegistrationWorker.getInitCounts();
        assertEquals(inits, THREAD_POOL_SIZE);
        Integer configs = MockDMSRegistrationWorker.getConfigCounts();
        assertEquals(configs, THREAD_POOL_SIZE);
    }

    /**
     * This test verifies that the DMS registration occurs with the correct
     * parameter specified in the configuration
     * @throws MalformedURLException 
     */
    public void testDMSRegistrationRequest() throws MalformedURLException {
        IDMSRegistrationMgr regMgr = createModifiedRegistrationManager();
        URL dcsfURI = null;
        try {
            dcsfURI = new URL(DCSF_LOCATION);
        } catch (MalformedURLException e1) {
            assertTrue(false);
        }
        //Register one DABS
        IDCCRegistrationInfo dabsRegInfo = new DCCRegistrationInfoImpl();
        dabsRegInfo.setComponentName("dabs #1");
        dabsRegInfo.setComponentType(ServerComponentType.DABS);
        dabsRegInfo.setEventListenerURL(dcsfURI);
        //Register one DMS
        IDCCRegistrationInfo dmsRegInfo = new DCCRegistrationInfoImpl();
        dmsRegInfo.setComponentName("dms #1");
        dmsRegInfo.setComponentType(ServerComponentType.DMS);
        dmsRegInfo.setEventListenerURL(dcsfURI);
        //Register one DPS
        IDCCRegistrationInfo dpsRegInfo = new DCCRegistrationInfoImpl();
        dpsRegInfo.setComponentName("dps #1");
        dpsRegInfo.setComponentType(ServerComponentType.DPS);
        dpsRegInfo.setEventListenerURL(dcsfURI);
        MockDMSRegistrationListener listener = new MockDMSRegistrationListener();

        //Registers the three components
        regMgr.registerComponentWithDMS(dabsRegInfo, listener);
        regMgr.registerComponentWithDMS(dmsRegInfo, listener);
        regMgr.registerComponentWithDMS(dpsRegInfo, listener);

        //Wait for thread to be done
        //This is a bit weird, but this is only for an isolated test scenario
        synchronized (this) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
                assertFalse(true);
            }
        }

        //no need to synchronize on tasks, nobody should access it now
        List tasks = MockDMSRegistrationWorker.getTaskList();
        assertEquals(3, tasks.size());
        assertTrue(tasks.get(0) instanceof DMSRegistrationRequest);
        assertTrue(tasks.get(0) instanceof ITask);
        DMSRegistrationRequest req = (DMSRegistrationRequest) tasks.get(0);
        assertEquals(req.getListener(), listener);

        //we have no guarantee for the order of registration...
        List regInfoList = new ArrayList();
        regInfoList.add(req.getRegistrationInfo());
        req = (DMSRegistrationRequest) tasks.get(1);
        regInfoList.add(req.getRegistrationInfo());
        req = (DMSRegistrationRequest) tasks.get(2);
        regInfoList.add(req.getRegistrationInfo());
        assertTrue(regInfoList.contains(dabsRegInfo));
        assertTrue(regInfoList.contains(dmsRegInfo));
        assertTrue(regInfoList.contains(dpsRegInfo));
    }

    /**
     * This test verifies that the arguments given to the registration manager
     * are checked propertly.
     * @throws MalformedURLException 
     */
    public void testInitialArguments() throws MalformedURLException {
        IConfigurable registrationMgr = new DMSRegistrationMgrImpl();
        HashMapConfiguration badConfig = new HashMapConfiguration();
        URL dmsLocation = new URL(DMS_LOCATION);
        URL dcsfLocation = new URL(DCSF_LOCATION);

        boolean assertionThrown = false;
        badConfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, dcsfLocation);
        badConfig.setProperty(IThreadPool.THREADPOOL_SIZE, THREAD_POOL_SIZE);
        try {
            registrationMgr.setConfiguration(badConfig);
        } catch (IllegalArgumentException ex) {
            assertionThrown = true;
        }
        assertTrue(assertionThrown);
        assertionThrown = false;

        badConfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, dmsLocation);
        badConfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, null);
        try {
            registrationMgr.setConfiguration(badConfig);
        } catch (IllegalArgumentException ex) {
            assertionThrown = true;
        }
        assertTrue(assertionThrown);
        assertionThrown = false;

        badConfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, dmsLocation);
        badConfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, dcsfLocation);
        try {
            registrationMgr.setConfiguration(badConfig);
        } catch (IllegalArgumentException ex) {
            assertionThrown = true;
        }
        assertFalse(assertionThrown);
        assertionThrown = false;
    }

    /**
     * This test verifies that the DMS registration manager calls back properly
     * after the registration succeeds or not, and retries of the web service
     * cannot be contacted.
     * @throws MalformedURLException 
     */
    public void testCallbackOnRegistration() throws MalformedURLException {
        IComponentManager compManager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        HashMapConfiguration threadPoolConfig = new HashMapConfiguration();
        URL dmsLocation = new URL(DMS_LOCATION);
        URL dabsURI = new URL(DABS_LOCATION);
        
        threadPoolConfig.setProperty(IThreadPool.THREADPOOL_CONFIG, config);
        config.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, dmsLocation);
        config.setProperty(IDMSRegistrationWorker.SLEEP_TIME_CONFIG_PARAM, new Long("100")); //don't
        // sleep
        // much

        //put our mock class instead of the basic class to spy what's going on
        ComponentInfo info = new ComponentInfo("TestComp", MockRegistrationMgrWorkerImpl.class.getName(), IDMSRegistrationWorker.class.getName(), LifestyleType.TRANSIENT_TYPE, threadPoolConfig);
        IDMSRegistrationWorker regWorker = (IDMSRegistrationWorker) compManager.getComponent(info);
        assertNotNull(regWorker);
        IDCCRegistrationInfo regInfo = new DCCRegistrationInfoImpl();
        regInfo.setComponentName("Test DCC Component");
        regInfo.setComponentType(ServerComponentType.DABS);
        regInfo.setComponentVersion(new VersionDefaultImpl(1, 1, 1, 1, 1));
        regInfo.setComponentURL(dabsURI);
        URL dummyListenerURL = null;
        try {
            dummyListenerURL = new URL("http://eventlistener.com");
        } catch (MalformedURLException e1) {
            assertTrue(false);
        }
        regInfo.setEventListenerURL(dummyListenerURL);
        MockDMSRegistrationListener registrationListener = new MockDMSRegistrationListener();
        DMSRegistrationRequest request = new DMSRegistrationRequest(regInfo, registrationListener);
        
        //Perform the component registration
        regWorker.doWork(request);

        //Now, let's see what happened
        MockComponentServiceStub stub = MockComponentServiceLocator.getServiceStub();
        assertEquals("Number of DMS registration tries before success", 2, stub.getCounter());
        IDCCRegistrationStatus status = registrationListener.getRegistrationStatus();
        assertNotNull("Callback notified", status);
        assertEquals("DMS registration succeeded", DMSRegistrationResult.SUCCESS, status.getRegistrationResult());
        IDCCComponentConfigurationDO dccConfig = status.getComponentConfiguration();
        assertNotNull("Configuration received");
        assertEquals("Correct heartbeat", MockComponentServiceStub.HEARTBEAT.intValue(), dccConfig.getHeartbeatInterval());
    }

    /**
     * Creates an instance of the DMS registration manager using a dummy class
     * for the registration worker thread
     * 
     * @return an instance of the DMS registration manager
     * @throws MalformedURLException 
     */
    private IDMSRegistrationMgr createModifiedRegistrationManager() throws MalformedURLException {
        IComponentManager compManager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        URL dmsLocation = new URL(DMS_LOCATION);
        URL dcsfLocation = new URL(DCSF_LOCATION);

        config.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, dmsLocation);
        config.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, dcsfLocation);
        config.setProperty(IThreadPool.THREADPOOL_SIZE, THREAD_POOL_SIZE);

        //put our mock class instead of the basic class to see what happens
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, MockDMSRegistrationWorker.class.getName());
        ComponentInfo info = new ComponentInfo("TestComp", DMSRegistrationMgrImpl.class.getName(), IDMSRegistrationMgr.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        IDMSRegistrationMgr regMgr = (IDMSRegistrationMgr) compManager.getComponent(info);
        assertNotNull(regMgr);
        return (regMgr);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        compMgr.shutdown();
    }
}