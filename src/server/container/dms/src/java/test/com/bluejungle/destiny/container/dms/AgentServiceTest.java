/*
 * Created on Nov 15, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Calendar;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.UnsignedShort;

import com.bluejungle.destiny.container.dms.components.BaseDMSComponentTest;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.TimeIntervalDTO;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.services.agent.AgentServiceIF;
import com.bluejungle.destiny.services.agent.AgentServiceIFBindingStub;
import com.bluejungle.destiny.services.agent.AgentServiceLocator;
import com.bluejungle.destiny.services.agent.types.AgentHeartbeatData;
import com.bluejungle.destiny.services.agent.types.AgentProfileStatusData;
import com.bluejungle.destiny.services.agent.types.AgentRegistrationData;
import com.bluejungle.destiny.services.agent.types.AgentShutdownData;
import com.bluejungle.destiny.services.agent.types.AgentStartupConfiguration;
import com.bluejungle.destiny.services.agent.types.AgentStartupData;
import com.bluejungle.destiny.services.agent.types.AgentUpdateAcknowledgementData;
import com.bluejungle.destiny.services.agent.types.AgentUpdates;
import com.bluejungle.destiny.services.management.ProfileServiceIF;
import com.bluejungle.destiny.services.management.ProfileServiceLocator;
import com.bluejungle.destiny.services.management.types.AgentDTO;
import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.AgentStatistics;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.policy.types.AgentTypeEnum;
import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.destiny.services.policy.types.SystemUser;
import com.bluejungle.domain.types.AgentTypeDTO;
import com.bluejungle.version.types.Version;

/**
 * This is the agent Service test for DABS. It uses the AgentService on DMS for
 * verification.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/com/bluejungle/destiny/container/dabs/tests/AgentServiceTest.java#3 $
 */
public class AgentServiceTest extends BaseDMSComponentTest {

    private static final String AGENT_HOST = "localhost";
    private static final AgentTypeDTO AGENT_TYPE = AgentTypeDTO.DESKTOP;
    private static final int HEARTBEAT_INTERVAL = 10;
    private static final SystemUser[] USER_LIST = new SystemUser[] { new SystemUser("bogus", "2624") };
    private static final int major = 2;
    private static final int minor = 12;
    private static final int maintenance = 1;
    private static final int patch = 3;
    private static final int build = 329;
    private static final NonNegativeInteger wsMajor = new NonNegativeInteger(String.valueOf(major));
    private static final NonNegativeInteger wsMinor = new NonNegativeInteger(String.valueOf(minor));
    private static final NonNegativeInteger wsMaintenance = new NonNegativeInteger(String.valueOf(maintenance));
    private static final NonNegativeInteger wsPatch = new NonNegativeInteger(String.valueOf(patch));
    private static final NonNegativeInteger wsBuild = new NonNegativeInteger(String.valueOf(build));
    private static final Version wsVersion = new Version(wsMajor, wsMinor, wsMaintenance, wsPatch, wsBuild);

    /*
     * Security-related variables:
     */
    private static final String DESTINY_INSTALL_PATH_PROPERTY_NAME = "build.root.dir";
    private static final String DEFAULT_DESTINY_INSTALL_PATH = "c:\\builds\\destiny";
    private static final String INSTALL_DIR = System.getProperty(DESTINY_INSTALL_PATH_PROPERTY_NAME, DEFAULT_DESTINY_INSTALL_PATH);
    private static final String AGENT_KEYSTORE_FILE = INSTALL_DIR + "\\run\\server\\certificates\\agent-keystore.jks";
    private static final String AGENT_TRUSTSTORE_FILE = INSTALL_DIR + "\\run\\server\\certificates\\agent-truststore.jks";
    private static final String TEMP_AGENT_KEYSTORE_FILE = INSTALL_DIR + "\\run\\server\\certificates\\temp_agent-keystore.jks";
    private static final String TEMP_AGENT_TRUSTSTORE_FILE = INSTALL_DIR + "\\run\\server\\certificates\\temp_agent-truststore.jks";
    private static final String KEYSTORE_PROP = "nextlabs.javax.net.ssl.keyStore";
    private static final String KEYSTORE_PASSWORD_PROP = "nextlabs.javax.net.ssl.keyStorePassword";
    private static final String TRUSTSTORE_PROP = "nextlabs.javax.net.ssl.trustStore";
    private static final String TRUSTSTORE_PASSWORD_PROP = "nextlabs.javax.net.ssl.trustStorePassword";
    private static final String PASSWORD = "password";
    private AgentServiceIF dabsAgentService_secure;

    /*
     * Test variables
     */
    private AgentServiceIF dabsAgentService;
    private com.bluejungle.destiny.services.management.AgentServiceIF dmsAgentService;
    private ProfileServiceIF dmsProfileService;

    /**
     * Retrieve a JUnit Test Suite for all of the methods in this test class
     * 
     * @return
     */
    public static Test getTestSuite() {
        TestSuite suiteToReturn = new TestSuite("Agent Service Test");
        suiteToReturn.addTest(new AgentServiceTest("testRegisterAgent"));
        suiteToReturn.addTest(new AgentServiceTest("testStartupAgent"));
        suiteToReturn.addTest(new AgentServiceTest("testCheckUpdates"));
        suiteToReturn.addTest(new AgentServiceTest("testShutdownAgent"));
        suiteToReturn.addTest(new AgentServiceTest("testSecurity"));

        return suiteToReturn;
    }

    /**
     * Constructor for PrincipalServiceTest.
     * 
     * @param testName
     *            the name of the test
     */
    public AgentServiceTest(String testName) {
        super(testName);
    }

    /**
     * Sets up the test. This also tests the removeAgent() functionality because
     * if that were to fail and the agent was not removed, then all other tests
     * would fail.
     * 
     * @throws ServiceException
     *             if setup fails
     */
    public void setUp() throws Exception {
        super.setUp();

        // Initialize the DABS-Agent-Service:
        AgentServiceLocator dabsAgentSvcLocator = new AgentServiceLocator();
        dabsAgentSvcLocator.setAgentServiceIFPortEndpointAddress("http://localhost:8081/dabs/services/AgentServiceIFPort");
        this.dabsAgentService = dabsAgentSvcLocator.getAgentServiceIFPort();
        assertNotNull("DABS Agent Service not created properly", this.dabsAgentService);

        // Initialize the DMS-Agent-Service:
        com.bluejungle.destiny.services.management.AgentServiceLocator dmsAgentSvcLocator = new com.bluejungle.destiny.services.management.AgentServiceLocator();
        dmsAgentSvcLocator.setAgentServiceIFPortEndpointAddress("http://localhost:8081/dms/services/AgentServiceIFPort");
        this.dmsAgentService = dmsAgentSvcLocator.getAgentServiceIFPort();
        assertNotNull("DMS Agent Service not created properly", this.dmsAgentService);

        // Initialize the profile service:
        ProfileServiceLocator profileSvcLocator = new ProfileServiceLocator();
        profileSvcLocator.setProfileServiceIFPortEndpointAddress("http://localhost:8081/dms/services/ProfileServiceIFPort");
        this.dmsProfileService = profileSvcLocator.getProfileServiceIFPort();
        assertNotNull("DMS Profile Service not created properly", this.dabsAgentService);

        // Initialize the secure agent service:
        AgentServiceLocator dabsAgentSvcLocator_secure = new AgentServiceLocator();
        dabsAgentSvcLocator_secure.setAgentServiceIFPortEndpointAddress("https://localhost:8443/dabs/services/AgentServiceIFPort");
        System.setProperty(KEYSTORE_PROP, TEMP_AGENT_KEYSTORE_FILE);
        System.setProperty(KEYSTORE_PASSWORD_PROP, PASSWORD);
        System.setProperty(TRUSTSTORE_PROP, TEMP_AGENT_TRUSTSTORE_FILE);
        System.setProperty(TRUSTSTORE_PASSWORD_PROP, PASSWORD);
        this.dabsAgentService_secure = dabsAgentSvcLocator_secure.getAgentServiceIFPort();
        assertNotNull("DABS Agent Service not created properly", this.dabsAgentService_secure);
    }

    /**
     * Tests agent registration
     * 
     * @throws RemoteException
     * @throws ServiceNotReadyFault
     * @throws UnauthorizedCallerFault
     */
    public void testRegisterAgent() throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        AgentDTO agentDTO = null;
        AgentRegistrationData registration = null;
        AgentStartupConfiguration startupConfig = null;
        AgentServiceIFBindingStub agentSvcStub = (AgentServiceIFBindingStub) dabsAgentService;
        Object[] attachments = null;
        boolean keyStoreExists = false;
        boolean trustStoreExists = false;

        // Register agent:
        registration = new AgentRegistrationData();
        registration.setHost(AGENT_HOST);
        registration.setType(AGENT_TYPE);
        registration.setVersion(wsVersion);
        startupConfig = this.dabsAgentService.registerAgent(registration);

        // Verify agent registration:
        assertNotNull("Startup config returned after succesful registration should not be null", startupConfig);
        assertNotNull("An ID should be assigned to this agent", startupConfig.getId());
        assertNotNull("An agent profile should be assigned to this agent", startupConfig.getAgentProfile());
        assertNotNull("A comm profile should be assigned to this agent", startupConfig.getCommProfile());
        agentDTO = this.dmsAgentService.getAgentById(startupConfig.getId());
        assertNotNull("Registered agent should have been persisted", agentDTO);
        assertFalse("Agent should not be online immediately after registration", agentDTO.isIsOnline());
        assertFalse("Agent should not be marked as push ready immediately after registration", agentDTO.isIsPushReady());
        assertNull("Agent should not be assigned a push port immediately after registration", agentDTO.getPushPort());
        assertEquals("Agent host name was not persisted correctly", AGENT_HOST, agentDTO.getHost());
        assertEquals("Agent type was not persisted correctly", AGENT_TYPE.getValue(), agentDTO.getType().getId());
        assertNull("Agent should not have a last heartbeat stored", agentDTO.getLastHeartbeat());
        
        // Check to make sure that no certificate was returned since this is an
        // unsecure connection:
        attachments = agentSvcStub.getAttachments();
        keyStoreExists = false;
        trustStoreExists = false;
        for (int i = 0; i < attachments.length; i++) {
            AttachmentPart attachment = (AttachmentPart) attachments[i];
            if (attachment.getContentId().equals("AgentKeyStore")) {
                keyStoreExists = true;
            }
            if (attachment.getContentId().equals("AgentTrustStore")) {
                trustStoreExists = true;
            }
        }
        assertFalse("Agent certificate should NOT have been returned on an unsecure connection", (keyStoreExists || trustStoreExists));

        // Register incorrect agent:
        registration.setType(null);
        registration.setHost(null);
        registration.setVersion(null);
        try {
            this.dabsAgentService.registerAgent(registration);
            fail("Agent registration should have failed");
        } catch (RemoteException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests the startup of the agent
     * 
     * @throws RemoteException
     * @throws ServiceNotReadyFault
     * @throws UnauthorizedCallerFault
     */
    public void testStartupAgent() throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        AgentDTO agentDTO = null;
        AgentRegistrationData registration = null;
        AgentStartupConfiguration startupConfig = null;
        AgentStartupData startupData = null;

        // Register agent:
        registration = new AgentRegistrationData();
        registration.setHost(AGENT_HOST);
        registration.setType(AGENT_TYPE);
        registration.setVersion(wsVersion);
        startupConfig = this.dabsAgentService.registerAgent(registration);
        startupData = new AgentStartupData();
        startupData.setPushPort(null);
        this.dabsAgentService.startupAgent(startupConfig.getId(), startupData);

        // Verify agent status:
        agentDTO = this.dmsAgentService.getAgentById(startupConfig.getId());
        assertNotNull("Registered agent should have been persisted", agentDTO);
        assertFalse("Agent should not be online immediately after registration", agentDTO.isIsOnline());
        assertFalse("Agent should not be marked as push ready since push port was null", agentDTO.isIsPushReady());
        assertNull("Agent should not be assigned a push port", agentDTO.getPushPort());
        assertNull("Agent should not have a last heartbeat stored", agentDTO.getLastHeartbeat());

        // Now register a push-port
        startupData.setPushPort(new UnsignedShort(5534));
        this.dabsAgentService.startupAgent(startupConfig.getId(), startupData);

        // Verify agent status:
        agentDTO = this.dmsAgentService.getAgentById(startupConfig.getId());
        assertNotNull("Registered agent should have been persisted", agentDTO);
        assertFalse("Agent should not be online immediately after registration", agentDTO.isIsOnline());
        assertTrue("Agent should be marked as push ready since push port was provided", agentDTO.isIsPushReady());
        assertNotNull("Agent should be assigned a push port", agentDTO.getPushPort());
        assertNull("Agent should not have a last heartbeat stored", agentDTO.getLastHeartbeat());

        // Try negative test case:
        try {
            this.dabsAgentService.startupAgent(null, null);
            fail("Agent startup should have failed");
        } catch (RemoteException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests heartbeat, acknowledgement and online status of agent
     * 
     * @throws InterruptedException
     * @throws RemoteException
     * @throws ServiceNotReadyFault
     * @throws UnauthorizedCallerFault
     */
    public void testCheckUpdates() throws InterruptedException, RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        AgentDTO agentDTO = null;
        AgentRegistrationData registration = null;
        AgentStartupConfiguration startupConfig = null;
        AgentStartupData startupData = null;
        AgentHeartbeatData heartbeatData = null;
        AgentProfileStatusData profileStatusData = null;
        AgentUpdateAcknowledgementData acknowledgementData = null;
        AgentProfileDTO lastReceivedAgentProfile = null;
        CommProfileDTO lastReceivedCommProfile = null;

        // Setup agent:
        registration = new AgentRegistrationData();
        registration.setHost(AGENT_HOST);
        registration.setType(AGENT_TYPE);
        registration.setVersion(wsVersion);
        startupConfig = this.dabsAgentService.registerAgent(registration);
        startupData = new AgentStartupData();
        startupData.setPushPort(null);
        this.dabsAgentService.startupAgent(startupConfig.getId(), startupData);

        // Create a dpeloyment bundle request:
        DeploymentRequest deploymentBundleRequest = new DeploymentRequest();
        deploymentBundleRequest.setAgentHost(AGENT_HOST);
        deploymentBundleRequest.setPolicyUsers(USER_LIST);
        deploymentBundleRequest.setTimestamp(null);

        // Create heartbeat:
        heartbeatData = new AgentHeartbeatData();
        //**heartbeatData.setPolicyAssemblyStatus(deploymentBundleRequest);

        // Send heartbeat to obtain profiles:
        AgentUpdates updates = this.dabsAgentService.checkUpdates(startupConfig.getId(), heartbeatData);
        assertNotNull("Updates sent to the agent should not be null", updates);
        assertNotNull("Comm profile should be returned on updates", updates.getCommProfile());
        assertNotNull("Agent profile should be returned on updates", updates.getAgentProfile());
        //**assertNotNull("Policy deployment bundle should have been returned",
        // updates.getPolicyDeploymentBundle());
        lastReceivedAgentProfile = updates.getAgentProfile();
        lastReceivedCommProfile = updates.getCommProfile();

        // Change/setup heartbeat interval on agent's comm profile so that we
        // can do timing tests:
        CommProfileDTO commProfile = updates.getCommProfile();
        commProfile.setHeartBeatFrequency(new TimeIntervalDTO(new UnsignedShort(HEARTBEAT_INTERVAL), TimeUnits.fromString("seconds")));
        this.dmsProfileService.updateCommProfile(commProfile);

        // Send heartbeat after acknowledging the first profiles received:
        profileStatusData = new AgentProfileStatusData();
        profileStatusData.setLastCommittedAgentProfileName(lastReceivedAgentProfile.getName().toString());
        profileStatusData.setLastCommittedAgentProfileTimestamp(lastReceivedAgentProfile.getModifiedDate());
        profileStatusData.setLastCommittedCommProfileName(lastReceivedCommProfile.getName().toString());
        profileStatusData.setLastCommittedCommProfileTimestamp(lastReceivedCommProfile.getModifiedDate());
        deploymentBundleRequest.setTimestamp(Calendar.getInstance());
        heartbeatData.setProfileStatus(profileStatusData);
        //**heartbeatData.setPolicyAssemblyStatus(deploymentBundleRequest);
        updates = this.dabsAgentService.checkUpdates(startupConfig.getId(), heartbeatData);
        assertNotNull("Updates sent to the agent should not be null", updates);
        assertNotNull("Comm profile update should be returned since we just changed the heartbeat rate", updates.getCommProfile());
        assertNull("No agent profile update should be returned", updates.getAgentProfile());
        //**assertNull("No policy deployment bundle should be returned",
        // updates.getPolicyDeploymentBundle());
        lastReceivedCommProfile = updates.getCommProfile();

        // Verify agent status:
        agentDTO = this.dmsAgentService.getAgentById(startupConfig.getId());
        assertNotNull("Registered agent should have been persisted", agentDTO);
        assertTrue("Agent should be online since we've sent a heartbeat", agentDTO.isIsOnline());
        assertNotNull("Agent should have a last heartbeat stored", agentDTO.getLastHeartbeat());

        // Try incorrect heartbeat case:
        try {
            this.dabsAgentService.checkUpdates(null, null);
            fail("Agent heartbeat should have failed");
        } catch (RemoteException e) {
            assertTrue(true);
        }

        // Send acknowledgement for last comm profile received and verify
        // status:
        profileStatusData = new AgentProfileStatusData();
        profileStatusData.setLastCommittedCommProfileName(lastReceivedCommProfile.getName().toString());
        profileStatusData.setLastCommittedCommProfileTimestamp(lastReceivedCommProfile.getModifiedDate());
        profileStatusData.setLastCommittedAgentProfileName(lastReceivedAgentProfile.getName().toString());
        profileStatusData.setLastCommittedAgentProfileTimestamp(lastReceivedAgentProfile.getModifiedDate());
        acknowledgementData = new AgentUpdateAcknowledgementData();
        acknowledgementData.setProfileStatus(profileStatusData);
        agentDTO = this.dmsAgentService.getAgentById(startupConfig.getId());
        assertNotNull("Agent should have a last heartbeat stored", agentDTO.getLastHeartbeat());
        //!!! TODO -- there is no way of telling whether an acknowledgement was
        // stored. Fill this in when that sort of API is available.

        // Try incorrect acknowledgement case:
        try {
            this.dabsAgentService.acknowledgeUpdates(null, null);
            fail("Agent update acknowledgement should have failed");
        } catch (RemoteException e) {
            assertTrue(true);
        }

        // Send another heartbeat.Make sure nothing is returned in updates:
        heartbeatData.setProfileStatus(profileStatusData);
        updates = this.dabsAgentService.checkUpdates(startupConfig.getId(), heartbeatData);
        assertNotNull("Updates sent to the agent should not be null", updates);
        assertNull("No comm profile update should be returned", updates.getCommProfile());
        assertNull("No agent profile update sould be returned", updates.getAgentProfile());

        // Unregister this agent, then re-register and ensure that new
        // policies/profiles are returned:
        this.dabsAgentService.unregisterAgent(agentDTO.getId());
        agentDTO = this.dmsAgentService.getAgentById(startupConfig.getId());
        assertFalse("Agent should be considered offline", agentDTO.isIsOnline());
        startupConfig = this.dabsAgentService.registerAgent(registration);
        startupData = new AgentStartupData();
        startupData.setPushPort(null);
        this.dabsAgentService.startupAgent(startupConfig.getId(), startupData);
        deploymentBundleRequest = new DeploymentRequest();
        deploymentBundleRequest.setAgentHost(AGENT_HOST);
        deploymentBundleRequest.setPolicyUsers(USER_LIST);
        deploymentBundleRequest.setAgentType(AgentTypeEnum.DESKTOP);
        deploymentBundleRequest.setTimestamp(null);
        heartbeatData = new AgentHeartbeatData();
        heartbeatData.setPolicyAssemblyStatus(deploymentBundleRequest);
        updates = this.dabsAgentService.checkUpdates(startupConfig.getId(), heartbeatData);
        assertNotNull("Updates sent to the agent should not be null", updates);
        assertNotNull("Comm profile should be returned on updates", updates.getCommProfile());
        assertNotNull("Agent profile should be returned on updates", updates.getAgentProfile());
        assertNotNull("Policy bundle should have been returned", updates.getPolicyDeploymentBundle());
    }

    /**
     * Tests agent shutdown
     * 
     * @throws RemoteException
     * @throws ServiceNotReadyFault
     * @throws UnauthorizedCallerFault
     */
    public void testShutdownAgent() throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        AgentDTO agentDTO = null;
        AgentRegistrationData registration = null;
        AgentStartupConfiguration startupConfig = null;
        AgentStartupData startupData = null;
        AgentShutdownData shutdownData = null;

        // Setup agent:
        registration = new AgentRegistrationData();
        registration.setHost(AGENT_HOST);
        registration.setType(AGENT_TYPE);
        registration.setVersion(wsVersion);
        startupConfig = this.dabsAgentService.registerAgent(registration);
        startupData = new AgentStartupData();
        startupData.setPushPort(null);
        this.dabsAgentService.startupAgent(startupConfig.getId(), startupData);
        this.dabsAgentService.shutdownAgent(startupConfig.getId(), shutdownData);

        agentDTO = this.dmsAgentService.getAgentById(startupConfig.getId());
        assertNotNull("Registered agent should have been persisted", agentDTO);
        assertFalse("Agent should not be online after shutdown", agentDTO.isIsOnline());

        // Try incorrect heartbeat case:
        try {
            this.dabsAgentService.shutdownAgent(null, null);
            fail("Agent shutdown should have failed");
        } catch (RemoteException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests that the temporary agent certificate works to call registerAgent()
     * and that upon succesfull registration a new certificate is returned to
     * this agent for all subsequent API calls.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RemoteException
     * @throws SOAPException
     */
    public void testSecurity() throws RemoteException, IOException, FileNotFoundException, SOAPException {
        AgentDTO agentDTO = null;
        AgentRegistrationData registration = null;
        AgentStartupConfiguration startupConfig = null;
        AgentServiceIFBindingStub agentSvcStub = (AgentServiceIFBindingStub) dabsAgentService_secure;
        Object[] attachments = null;
        boolean keyStoreExists = false;
        boolean trustStoreExists = false;

        // Register agent:
        registration = new AgentRegistrationData();
        registration.setHost(AGENT_HOST);
        registration.setType(AGENT_TYPE);
        registration.setVersion(wsVersion);
        startupConfig = this.dabsAgentService_secure.registerAgent(registration);
        assertNotNull("Startup config returned after succesful registration should not be null", startupConfig);

        // Now confirm that a security certificate was returned as an
        // attachment:
        attachments = agentSvcStub.getAttachments();
        keyStoreExists = false;
        for (int i = 0; i < attachments.length; i++) {
            AttachmentPart attachment = (AttachmentPart) attachments[i];

            // We will get a trustStore and a keyStore from the agent. Check
            // both:
            if (attachment.getContentId().equals("AgentKeyStore")) {
                keyStoreExists = true;

                // Check the content of the keystore attachment:
                DataHandler dataHandler = (DataHandler) attachment.getDataHandler();
                InputStream inputStreamToVerify = dataHandler.getInputStream();
                InputStream correctInputStream = new FileInputStream(new File(AGENT_KEYSTORE_FILE));
                int a = inputStreamToVerify.read();
                int b = correctInputStream.read();
                while ((a != -1) && (b != -1)) {
                    assertTrue("Content of certificate is not as expected", a == b);
                    a = inputStreamToVerify.read();
                    b = correctInputStream.read();
                }
            } else if (attachment.getContentId().equals("AgentTrustStore")) {
                trustStoreExists = true;

                // Check the content of the keystore attachment:
                DataHandler dataHandler = (DataHandler) attachment.getDataHandler();
                InputStream inputStreamToVerify = dataHandler.getInputStream();
                InputStream correctInputStream = new FileInputStream(new File(AGENT_TRUSTSTORE_FILE));
                int a = inputStreamToVerify.read();
                int b = correctInputStream.read();
                while ((a != -1) && (b != -1)) {
                    assertTrue("Content of certificate is not as expected", a == b);
                    a = inputStreamToVerify.read();
                    b = correctInputStream.read();
                }
            }
        }
        assertTrue("Agent keyStore/trustStore should have been returned after successful registration", (keyStoreExists && trustStoreExists));

        // Register incorrect agent:
        registration.setType(null);
        registration.setHost(null);
        try {
            this.dabsAgentService.registerAgent(registration);
            fail("Agent registration should have failed");
        } catch (RemoteException e) {
            assertTrue(true);
        }

        // Check to make sure that no certificate was returned:
        attachments = agentSvcStub.getAttachments();
        keyStoreExists = false;
        trustStoreExists = false;
        for (int i = 0; i < attachments.length; i++) {
            AttachmentPart attachment = (AttachmentPart) attachments[i];
            if (attachment.getContentId().equals("AgentKeyStore")) {
                keyStoreExists = true;
            }
            if (attachment.getContentId().equals("AgentTrustStore")) {
                trustStoreExists = true;
            }
        }
        assertFalse("Agent certificate should NOT have been returned after failed registration", (keyStoreExists || trustStoreExists));
    }

    public void testGetAgentStatistics() throws ServiceNotReadyFault, RemoteException, InterruptedException {
        // Get current number of agents
        AgentStatistics agentStats = this.dmsAgentService.getAgentStatistics();

        // Ensure all stats not null
        assertNotNull("Ensure statistics are not null", agentStats);

        /*
         * That's about all that we can test from here. Getting the state of the
         * db at this point is very painful and not worth the effort at this
         * point
         */
    }
}
