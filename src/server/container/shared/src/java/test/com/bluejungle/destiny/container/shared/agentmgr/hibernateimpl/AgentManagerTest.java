/*
 * Created on Jan 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrSortFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTerm;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatus;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatus;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentQueryResults;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdates;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidIDException;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidQuerySpecException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.ProfileNotFoundException;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.services.policy.DefaultPolicyEditorIF;
import com.bluejungle.destiny.services.policy.PolicyEditorIF;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;
import com.bluejungle.framework.search.RelationalOp;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.framework.utils.TimeInterval;
import com.bluejungle.pf.tools.MockSharedContextLocator;

/**
 * JUnit tests for the Agent Manager (both DMS and DABS functionality)
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/agentmgr/test/AgentManagerTest.java#1 $
 */

public class AgentManagerTest extends BaseContainerSharedTestCase {

    private static final Long TEST_HEARTBEAT_RECORD_PROCESS_INTERAL = new Long(1);

    private IProfileManager profileMgr;
    private AgentManager agentMgrToTest;
    private IAgentMgrQueryTermFactory queryTermFactory;
    private ICommProfileDO defaultDesktopCommProfile;
    private ICommProfileDO defaultFileServerCommProfile;
    private IAgentProfileDO defaultAgentProfile;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();

        // Initialize the necessary manager objects:
        // Initialize the component manager:
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        // Setup the mock shared context
        ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(locatorInfo);

        // Initialize a HeartbeatRecorder which has a small store time
        HashMapConfiguration recorderConfig = new HashMapConfiguration();
        recorderConfig.setProperty(HeartbeatRecorderImpl.RECORD_PROCESS_TIME_INTERVAL_PROPERTY_NAME, TEST_HEARTBEAT_RECORD_PROCESS_INTERAL);
        compMgr.registerComponent(HeartbeatRecorderImpl.COMP_INFO, true);
        IHeartbeatRecorder heartbeatRecorder = compMgr.getComponent(HeartbeatRecorderImpl.COMP_INFO);

        // Initialize the agent manager:
        ComponentInfo agentMgrCompInfo = new ComponentInfo(IAgentManager.COMP_NAME, AgentManager.class.getName(), IAgentManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.agentMgrToTest = (AgentManager) compMgr.getComponent(agentMgrCompInfo);
        this.queryTermFactory = this.agentMgrToTest.getAgentMgrQueryTermFactory();

        // Initialize the profile manager:
        ComponentInfo profileMgrCompInfo = new ComponentInfo(IProfileManager.COMP_NAME, HibernateProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.profileMgr = (IProfileManager) compMgr.getComponent(profileMgrCompInfo);

        // Initialize a mock agent statistic
        HashMapConfiguration agentStatisticsCollectionConfig = new HashMapConfiguration();
        agentStatisticsCollectionConfig.setProperty(AgentStatisticsCollectorImpl.HEARTBEAT_RECORDER_PROPERTY_NAME, heartbeatRecorder);
        agentStatisticsCollectionConfig.setProperty(AgentStatisticsCollectorImpl.AGENT_MANAGER_PROPERTY_NAME, this.agentMgrToTest);
        ComponentInfo agentStatisticsCollectorInfo = new ComponentInfo(IAgentStatisticsCollector.COMP_NAME, AgentStatisticsCollectorImplExtension.class.getName(), IAgentStatisticsCollector.class.getName(), LifestyleType.SINGLETON_TYPE,
                agentStatisticsCollectionConfig);
        compMgr.registerComponent(agentStatisticsCollectorInfo, true);

        if (this.profileMgr == null || this.agentMgrToTest == null) {
            throw new NullPointerException("agent and profile managers not initialized");
        }

        deleteAllAgents();

        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        this.defaultDesktopCommProfile = this.profileMgr.getDefaultCommProfile(desktopAgentType);
        this.defaultFileServerCommProfile = this.profileMgr.getDefaultCommProfile(fileServerAgentType);
        this.defaultAgentProfile = this.profileMgr.getDefaultAgentProfile();
    }

    /**
     * Constructor for AgentManagerTest.
     * 
     * @param testName
     *            name of the test
     */
    public AgentManagerTest(String testName) {
        super(testName);
    }

    protected void deleteAllAgents() throws HibernateException {
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            t = s.beginTransaction();
            s.delete("from AgentDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Returns the management data source
     * 
     * @return the management data source
     */
    protected IHibernateRepository getDataSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        return (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
    }

    /**
     * This test case tests the updates functionality in parallel with the
     * setCommProfile, setAgentProfile and acknowledgement functionality. It
     * also verifies the isAlive() functionality if an agent doesn't send a
     * heartbeat for a certain period of time.
     * 
     */
    public void testCheckUpdatesAndAcknowledgement() throws PersistenceException, InvalidIDException, DataSourceException, URISyntaxException, ProfileNotFoundException {
        AgentDO agent = null;
        IAgentProfileStatus profileStatus = null;

        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        // Agent (A, Desktop)
        MockAgentRegistrationData mock_A_Desktop = new MockAgentRegistrationData("A", desktopAgentType);
        IAgentStartupConfiguration config_A_Desktop = this.agentMgrToTest.registerAgent(mock_A_Desktop);
        assertNotNull("registered agent not given configuration", config_A_Desktop);
        this.agentMgrToTest.startupAgent(config_A_Desktop.getId(), new MockStartupAgentData(null));

        // Send a heartbeat to obtain the initial profile information:
        MockAgentHeartbeatData heartbeat_A_Desktop = new MockAgentHeartbeatData();
        IAgentUpdates updates = this.agentMgrToTest.checkUpdates(config_A_Desktop.getId(), heartbeat_A_Desktop);
        assertNotNull("update returned here should not be null", updates);
        assertNotNull("update should contain an agent profile", updates.getAgentProfileUpdate());
        assertEquals("agent profile sent on update was not correct", updates.getAgentProfileUpdate().getName(), this.defaultAgentProfile.getName());
        assertNotNull("update should contain a comm profile", updates.getCommProfileUpdate());
        assertEquals("comm profile sent on update was not correct", updates.getCommProfileUpdate().getName(), this.defaultDesktopCommProfile.getName());
        // ------//TODO:Check stuff for the policy assembly

        // Send a heartbeat reflecting this profile acknowledgement:
        heartbeat_A_Desktop.acknowledgeAgentProfile(updates.getAgentProfileUpdate());
        heartbeat_A_Desktop.acknowledgeCommProfile(updates.getCommProfileUpdate());
        // ------//TODO:Set the policy assembly here too
        updates = this.agentMgrToTest.checkUpdates(config_A_Desktop.getId(), heartbeat_A_Desktop);
        assertNotNull("update returned here should not be null", updates);
        assertNull("update should not contain an agent profile", updates.getAgentProfileUpdate());
        assertNull("update should not contain a comm profile", updates.getCommProfileUpdate());
        // ------//TODO:Check stuff for the policy assembly as well

        // Ensure that this acknowledgement is persisted:
        agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        profileStatus = agent.getProfileStatus();
        assertNotNull("profile status returned here should not be null", profileStatus);
        assertEquals("profile status contains wrong acknowledged agent profile name", profileStatus.getLastAcknowledgedAgentProfileName(), this.defaultAgentProfile.getName());
        assertEquals("profile status contains wrong acknowledged agent profile timestamp", profileStatus.getLastAcknowledgedAgentProfileTimestamp(), this.defaultAgentProfile.getModifiedDate());
        assertEquals("profile status contains wrong acknowledged comm profile name", profileStatus.getLastAcknowledgedCommProfileName(), this.defaultDesktopCommProfile.getName());
        assertEquals("profile status contains wrong acknowledged comm profile timestamp", profileStatus.getLastAcknowledgedCommProfileTimestamp(), this.defaultDesktopCommProfile.getModifiedDate());

        // Now modify the default agent profile
        this.defaultAgentProfile.setTrayIconEnabled(this.defaultAgentProfile.isTrayIconEnabled());
        this.profileMgr.updateAgentProfile(this.defaultAgentProfile);

        // Check if it is sent back on the update:
        updates = this.agentMgrToTest.checkUpdates(config_A_Desktop.getId(), heartbeat_A_Desktop);
        assertNotNull("update returned here should not be null", updates);
        assertNotNull("update should contain an agent profile", updates.getAgentProfileUpdate());
        assertEquals("agent profile sent on update was not correct", updates.getAgentProfileUpdate().getName(), this.defaultAgentProfile.getName());
        assertEquals("agent profile sent on update did not reflect the change that was made", updates.getAgentProfileUpdate().isTrayIconEnabled(), this.defaultAgentProfile.isTrayIconEnabled());

        // Now modify the default comm profile
        TimeInterval heartBeatFrequency = this.defaultDesktopCommProfile.getHeartBeatFrequency();
        heartBeatFrequency.setTime(heartBeatFrequency.getTime() + 1);
        this.defaultDesktopCommProfile.setHeartBeatFrequency(heartBeatFrequency);
        this.profileMgr.updateCommProfile(this.defaultDesktopCommProfile);

        // Acknowledge agent profile. Ensure that comm profile is sent back on
        // the update:
        heartbeat_A_Desktop.acknowledgeAgentProfile(this.defaultAgentProfile);
        updates = this.agentMgrToTest.checkUpdates(config_A_Desktop.getId(), heartbeat_A_Desktop);
        assertNotNull("update returned here should not be null", updates);
        assertNull("update should not contain an agent profile", updates.getAgentProfileUpdate());
        assertNotNull("update should contain a comm profile", updates.getCommProfileUpdate());
        assertEquals("comm profile sent on update was not correct", updates.getCommProfileUpdate().getName(), this.defaultDesktopCommProfile.getName());
        assertEquals("comm profile sent on update did not reflect the change that was made", updates.getCommProfileUpdate().getHeartBeatFrequency(), this.defaultDesktopCommProfile.getHeartBeatFrequency());

        // Acknowledge comm profile. Ensure that nothing is returned next:
        heartbeat_A_Desktop.acknowledgeCommProfile(this.defaultDesktopCommProfile);
        updates = this.agentMgrToTest.checkUpdates(config_A_Desktop.getId(), heartbeat_A_Desktop);
        assertNotNull("update returned here should not be null", updates);
        assertNull("update should not contain an agent profile", updates.getAgentProfileUpdate());
        assertNull("update should not contain a comm profile", updates.getCommProfileUpdate());

        // Now create a new dummy comm/agent profile
        IAgentProfileData dummyAgentProfileData = new AgentProfileDataImpl("Dummy agent profile", true, true);
        IAgentProfileDO dummyAgentProfile = this.profileMgr.createAgentProfile(dummyAgentProfileData);
        ICommProfileData dummyCommProfileData = new CommProfileDataImpl("Dummy comm profile", new URI("http://www.dummy.com"), desktopAgentType, new TimeInterval(5), 20, 30, new TimeInterval(40), true, 80, "password", false);
        ICommProfileDO dummyCommProfile = this.profileMgr.createCommProfile(dummyCommProfileData);

        // Point A --> dummy comm/agent profiles
        this.agentMgrToTest.setAgentProfile(config_A_Desktop.getId(), dummyAgentProfile.getName());
        this.agentMgrToTest.setCommProfile(config_A_Desktop.getId(), dummyCommProfile.getName());

        // Send check-updates - verify that the new profiles are returned
        updates = this.agentMgrToTest.checkUpdates(config_A_Desktop.getId(), heartbeat_A_Desktop);
        assertNotNull("update returned here should not be null", updates);
        assertNotNull("update should contain an agent profile", updates.getAgentProfileUpdate());
        assertEquals("agent profile sent on update was not correct", updates.getAgentProfileUpdate().getName(), dummyAgentProfile.getName());
        assertNotNull("update should contain a comm profile", updates.getCommProfileUpdate());
        assertEquals("comm profile sent on update was not correct", updates.getCommProfileUpdate().getName(), dummyCommProfile.getName());

        // Create an acknowledgement for the default profiles from before. Then
        // ensure that it is persisted:
        MockAgentUpdateAcknowledgementData acknowledgement = new MockAgentUpdateAcknowledgementData();
        acknowledgement.acknowledgeAgentProfile(this.defaultAgentProfile);
        acknowledgement.acknowledgeCommProfile(this.defaultDesktopCommProfile);
        this.agentMgrToTest.acknowledgeUpdates(config_A_Desktop.getId(), acknowledgement);
        agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        profileStatus = agent.getProfileStatus();
        assertNotNull("profile status returned here should not be null", profileStatus);
        assertEquals("profile status contains wrong acknowledged agent profile name", profileStatus.getLastAcknowledgedAgentProfileName(), this.defaultAgentProfile.getName());
        assertEquals("profile status contains wrong acknowledged agent profile timestamp", profileStatus.getLastAcknowledgedAgentProfileTimestamp(), this.defaultAgentProfile.getModifiedDate());
        assertEquals("profile status contains wrong acknowledged comm profile name", profileStatus.getLastAcknowledgedCommProfileName(), this.defaultDesktopCommProfile.getName());
        assertEquals("profile status contains wrong acknowledged comm profile timestamp", profileStatus.getLastAcknowledgedCommProfileTimestamp(), this.defaultDesktopCommProfile.getModifiedDate());

        // Unregister an agent and make sure that the heartbeat reregisters it
        // and that it is given the comm/agent profiles again:
        this.agentMgrToTest.unregisterAgent(agent.getId());
        agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        assertFalse("Ensure agent is not registered after unregistration.", agent.isRegistered());
        this.agentMgrToTest.checkUpdates(agent.getId(), heartbeat_A_Desktop);
        agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        assertTrue("Ensure agent is registered after heartbeat.", agent.isRegistered());

        // Unregister the agent again, then register it and make sure that the
        // correct comm/agent profiles are returned on the next heartbeat:
        this.agentMgrToTest.unregisterAgent(agent.getId());
        agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        assertFalse("Ensure agent is not registered after unregistration.", agent.isRegistered());
        this.agentMgrToTest.registerAgent(mock_A_Desktop);
        agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        assertTrue("Ensure agent is registered now.", agent.isRegistered());
        heartbeat_A_Desktop = new MockAgentHeartbeatData();
        updates = this.agentMgrToTest.checkUpdates(agent.getId(), heartbeat_A_Desktop);
        assertNotNull("Ensure that the agent is returned a comm profile now", updates.getCommProfileUpdate());
        assertEquals("Comm profile sent on update should be the default one now", updates.getCommProfileUpdate().getName(), this.defaultDesktopCommProfile.getName());
        assertNotNull("Ensure that the agent is returned an agent profile now", updates.getAgentProfileUpdate());
        assertEquals("Agent profile sent on update should be the default one now", updates.getAgentProfileUpdate().getName(), this.defaultAgentProfile.getName());
    }

    public void testRegisterWithNonDefaultProfile() throws InvalidIDException,
			PersistenceException, UniqueConstraintViolationException, DataSourceException,
			ProfileNotFoundException {

		IAgentType agentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        MockAgentRegistrationData agentRegistrationData = new MockAgentRegistrationData("woodpecker", agentType);
        IAgentStartupConfiguration startupConfiguration = this.agentMgrToTest.registerAgent(agentRegistrationData);
        assertNotNull("registered agent not given configuration", startupConfiguration);
        this.agentMgrToTest.startupAgent(startupConfiguration.getId(), new MockStartupAgentData(null));
		
        AgentDO agent = (AgentDO) this.agentMgrToTest.getAgentById(startupConfiguration.getId());
        assertNotNull(agent);
        
        IAgentProfileDO agentProfileDO = agent.getAgentProfile();
		ICommProfileDO commProfileDO = agent.getCommProfile();
        
        //assign a different profile, all I care is the name
        final String agentProfileName2 = "Agent_Profile_Name_TWO";
        this.profileMgr.createAgentProfile( new IAgentProfileData() {
            public boolean isLogViewingEnabled() {
                return false;
            }

            public boolean isTrayIconEnabled() {
                return false;
            }

            public String getName() {
                return agentProfileName2;
            }
        });
        
        final String commProfileName2 = "Comm_Profile_Name_7W0";
        final IAgentType desktopAgentType2 = agentType;
        this.profileMgr.createCommProfile(new ICommProfileData(){
			public IAgentType getAgentType() {
				return desktopAgentType2;
			}

			public URI getDABSLocation() {
				try {
					return new URI("http://localhost/somewhere");
				} catch (URISyntaxException e) {
					fail(e.toString());
					throw new RuntimeException(e);
				}
			}

			public int getDefaultPushPort() {
				return 0;
			}

			public TimeInterval getHeartBeatFrequency() {
				return new TimeInterval(63, TimeInterval.TimeUnit.MINUTES);
			}

			public TimeInterval getLogFrequency() {
				return new TimeInterval(42, TimeInterval.TimeUnit.SECONDS);
			}

			public int getLogLimit() {
				return 0;
			}

			public String getPassword() {
				return "";
			}

			public boolean isPushEnabled() {
				return false;
			}

			public String getName() {
				return commProfileName2;
			}
        });
        
        this.agentMgrToTest.setAgentProfile(agent.getId(), agentProfileName2);
        this.agentMgrToTest.setCommProfile(agent.getId(), commProfileName2);
        
        IAgentStartupConfiguration agentStartupConfiguration = this.agentMgrToTest
				.registerAgent(agentRegistrationData);
        assertNotNull(agentStartupConfiguration);
        assertEquals(agentProfileName2,  agentStartupConfiguration.getAgentProfile().getName());
        assertEquals(commProfileName2,  agentStartupConfiguration.getCommProfile().getName());
        
        this.agentMgrToTest.unregisterAgent(agent.getId());
        agentStartupConfiguration = this.agentMgrToTest.registerAgent(agentRegistrationData);
        assertNotNull(agentStartupConfiguration);
        assertEquals(agentProfileDO.getName(), agentStartupConfiguration.getAgentProfile().getName());
        assertEquals(commProfileDO.getName(),  agentStartupConfiguration.getCommProfile().getName());
	}

    /**
     * This test verifies that the query by name function works properly
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     * @throws InvalidQuerySpecException
     */
    public void testGetAgentsByName() throws PersistenceException, InvalidIDException, InvalidQuerySpecException {
        final int nbDA = 10;
        final int nbOtherDA = 5;
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        registerAgents(desktopAgentType, "DesktopHost", nbDA);
        registerAgents(desktopAgentType, "OtherHost", nbOtherDA);
        final AgentQuerySpecImpl queryByName = new AgentQuerySpecImpl();
        queryByName.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.LIKE, "*esktop*"));
        IAgentQueryResults queryResults = this.agentMgrToTest.getAgents(queryByName);
        IAgentDO[] agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbDA, agentsRetrieved.length);
        int size = agentsRetrieved.length;
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertTrue("The query by name should work with wildchar", agent.getHost().indexOf("esktop") > 0);
        }

        // Try with upper / lower case mix
        final AgentQuerySpecImpl queryByNameUpperLower = new AgentQuerySpecImpl();
        queryByNameUpperLower.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.LIKE, "*esKtOp*"));
        queryResults = this.agentMgrToTest.getAgents(queryByNameUpperLower);
        agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbDA, agentsRetrieved.length);
        size = agentsRetrieved.length;
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertTrue("The query by name shoudl be case insensitive", agent.getHost().indexOf("esktop") > 0);
        }

        // Try with exact match - no finding
        final AgentQuerySpecImpl queryByNameExactMatchNoResult = new AgentQuerySpecImpl();
        queryByNameExactMatchNoResult.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.EQUALS, "DesktopHost"));
        queryResults = this.agentMgrToTest.getAgents(queryByNameExactMatchNoResult);
        agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", 0, agentsRetrieved.length);

        // Try for both names starting with D or O
        final AgentQuerySpecImpl queryByNameHOrO = new AgentQuerySpecImpl();
        Set queryTermsToOr = new HashSet();
        IAgentMgrQueryTerm dQueryTerm = this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.STARTS_WITH, "D");
        IAgentMgrQueryTerm oQueryTerm = this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.STARTS_WITH, "O");
        queryTermsToOr.add(dQueryTerm);
        queryTermsToOr.add(oQueryTerm);
        queryByNameHOrO.addSearchSpecTerm(this.queryTermFactory.getORCompositeQueryTerm(queryTermsToOr));
        queryResults = this.agentMgrToTest.getAgents(queryByNameHOrO);
        agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbDA + nbOtherDA, agentsRetrieved.length);
    }

    /**
     * This test verifies that the query by registered flag works properly
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     * @throws InvalidQuerySpecException
     */
    public void testGetAgentsByRegistered() throws PersistenceException, InvalidIDException, InvalidQuerySpecException {
        final int nbAgents = 10;
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        registerAgents(desktopAgentType, "DesktopHost", nbAgents);

        // Startup desktop host 4
        final AgentQuerySpecImpl queryForHost4 = new AgentQuerySpecImpl();
        queryForHost4.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.EQUALS, "DesktopHost4"));
        IAgentQueryResults queryResults = this.agentMgrToTest.getAgents(queryForHost4);
        assertEquals("One record should be returned", 1, queryResults.getAgents().length);
        final Long host4Id = queryResults.getAgents()[0].getId();
        this.agentMgrToTest.unregisterAgent(host4Id);

        // Query for active agents
        final AgentQuerySpecImpl queryForRegistered = new AgentQuerySpecImpl();
        queryForRegistered.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.REGISTERED, RelationalOp.EQUALS, new Boolean(true)));
        queryResults = this.agentMgrToTest.getAgents(queryForRegistered);
        IAgentDO[] agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbAgents - 1, agentsRetrieved.length);
        int size = agentsRetrieved.length;
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertFalse("The correct inactive record should be retrieved", host4Id.equals(agent.getId()));
        }

        // Query for inactive agents
        final AgentQuerySpecImpl queryForUnregistered = new AgentQuerySpecImpl();
        queryForUnregistered.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.REGISTERED, RelationalOp.EQUALS, new Boolean(false)));
        queryResults = this.agentMgrToTest.getAgents(queryForUnregistered);
        agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", 1, agentsRetrieved.length);
        assertEquals("The correct active record should be retrieved", host4Id, agentsRetrieved[0].getId());

        // Try the bad operators
        Set badOperators = new HashSet();
        badOperators.add(RelationalOp.GREATER_THAN);
        badOperators.add(RelationalOp.GREATER_THAN_EQUALS);
        badOperators.add(RelationalOp.HAS);
        badOperators.add(RelationalOp.LESS_THAN);
        badOperators.add(RelationalOp.LESS_THAN_EQUALS);
        badOperators.add(RelationalOp.STARTS_WITH);

        Iterator it = badOperators.iterator();
        while (it.hasNext()) {
            boolean exThrown = false;
            AgentQuerySpecImpl badQuery = new AgentQuerySpecImpl();
            badQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.ONLINE, (RelationalOp) it.next(), new Boolean(true)));
            try {
                queryResults = this.agentMgrToTest.getAgents(badQuery);
            } catch (InvalidQuerySpecException e) {
                exThrown = true;
            }
            assertTrue("Exception should be thrown upon bad query", exThrown);
        }

        boolean exThrown = false;
        AgentQuerySpecImpl badQuery = new AgentQuerySpecImpl();
        badQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.ONLINE, RelationalOp.EQUALS, "abc"));
        try {
            queryResults = this.agentMgrToTest.getAgents(badQuery);
        } catch (InvalidQuerySpecException e) {
            exThrown = true;
        }
        assertTrue("Exception should be thrown upon bad query", exThrown);
    }

    /**
     * This test verifies that the query by type is working
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     * @throws InvalidQuerySpecException
     */
    public void testGetAgentsByType() throws PersistenceException, InvalidIDException, InvalidQuerySpecException {
        final int nbDA = 10;
        final int nbFSA = 5;
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        registerAgents(desktopAgentType, "DesktopHost", nbDA);
        registerAgents(fileServerAgentType, "OtherHost", nbFSA);

        // Query by desktop agent type
        final AgentQuerySpecImpl desktopQuery = new AgentQuerySpecImpl();
        desktopQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, desktopAgentType));
        IAgentQueryResults queryResults = this.agentMgrToTest.getAgents(desktopQuery);
        IAgentDO[] agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbDA, agentsRetrieved.length);
        int size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved", nbDA, size);
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertEquals("The agent type should be the correct type", desktopAgentType, agent.getType());
        }

        // Redo it with not equals
        final AgentQuerySpecImpl notDesktopQuery = new AgentQuerySpecImpl();
        notDesktopQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.NOT_EQUALS, desktopAgentType));
        queryResults = this.agentMgrToTest.getAgents(notDesktopQuery);
        agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbFSA, agentsRetrieved.length);
        size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved", nbFSA, size);
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertEquals("The agent type should be the correct type", fileServerAgentType, agent.getType());
        }

        // Query by file server agent type
        final AgentQuerySpecImpl fsaQuery = new AgentQuerySpecImpl();
        fsaQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, fileServerAgentType));
        queryResults = this.agentMgrToTest.getAgents(fsaQuery);
        agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbFSA, agentsRetrieved.length);
        size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved", nbFSA, size);
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertEquals("The agent type should be the correct type", fileServerAgentType, agent.getType());
        }

        // Tries the invalid type queries
        Set badOperators = new HashSet();
        badOperators.add(RelationalOp.GREATER_THAN);
        badOperators.add(RelationalOp.GREATER_THAN_EQUALS);
        badOperators.add(RelationalOp.HAS);
        badOperators.add(RelationalOp.LESS_THAN);
        badOperators.add(RelationalOp.LESS_THAN_EQUALS);
        badOperators.add(RelationalOp.STARTS_WITH);

        Iterator it = badOperators.iterator();
        while (it.hasNext()) {
            boolean exThrown = false;
            AgentQuerySpecImpl badQuery = new AgentQuerySpecImpl();
            badQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, (RelationalOp) it.next(), AgentTypeEnumType.FILE_SERVER));
            try {
                queryResults = this.agentMgrToTest.getAgents(badQuery);
            } catch (InvalidQuerySpecException e) {
                exThrown = true;
            }
            assertTrue("Exception should be thrown upon bad query", exThrown);
            exThrown = false;
        }
    }

    /**
     * This test verifies that the query by type, combined with name is working
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     * @throws InvalidQuerySpecException
     */
    public void testGetAgentsByTypeAndName() throws PersistenceException, InvalidIDException, InvalidQuerySpecException {
        final int nbDA = 10;
        final int nbFSA = 5;
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        registerAgents(desktopAgentType, "DesktopHost", nbDA);
        registerAgents(desktopAgentType, "MyDesktopHost", nbDA);
        registerAgents(fileServerAgentType, "OtherHost", nbFSA);

        // Query by desktop agent type
        AgentQuerySpecImpl desktopQuery = new AgentQuerySpecImpl();
        desktopQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, desktopAgentType));
        desktopQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.STARTS_WITH, "D"));
        IAgentQueryResults queryResults = this.agentMgrToTest.getAgents(desktopQuery);
        IAgentDO[] agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbDA, agentsRetrieved.length);
        int size = agentsRetrieved.length;
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertEquals("The agent type should be the correct type", desktopAgentType, agent.getType());
            assertTrue("The agent type should be the correct type", agent.getHost().startsWith("D"));
        }

        // Query for both agents starting with D and starting with M
        desktopQuery = new AgentQuerySpecImpl();
        desktopQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, desktopAgentType));
        Set queryTermsToOr = new HashSet();
        IAgentMgrQueryTerm dQueryTerm = this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.STARTS_WITH, "D");
        IAgentMgrQueryTerm mQueryTerm = this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.STARTS_WITH, "M");
        queryTermsToOr.add(dQueryTerm);
        queryTermsToOr.add(mQueryTerm);
        desktopQuery.addSearchSpecTerm(this.queryTermFactory.getORCompositeQueryTerm(queryTermsToOr));
        queryResults = this.agentMgrToTest.getAgents(desktopQuery);
        agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be results returned", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", 2 * nbDA, agentsRetrieved.length);
        size = agentsRetrieved.length;
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertEquals("The agent type should be the correct type", desktopAgentType, agent.getType());
            assertTrue("The agent type should be the correct type", agent.getHost().startsWith("D") || agent.getHost().startsWith("M"));
        }
    }

    /**
     * This test verifies that the "get all" function works properly
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     * @throws InvalidQuerySpecException
     */
    public void testGetAllAgents() throws PersistenceException, InvalidIDException, InvalidQuerySpecException {
        final int nbDA = 10;
        final int nbFSA = 5;
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        registerAgents(desktopAgentType, "DesktopHost", 10);
        registerAgents(fileServerAgentType, "FSAHost", 5);
        final AgentQuerySpecImpl queryAll = new AgentQuerySpecImpl();
        final IAgentQueryResults queryResults = this.agentMgrToTest.getAgents(queryAll);
        IAgentDO[] agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be agents retrieved", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbDA + nbFSA, agentsRetrieved.length);
        int size = agentsRetrieved.length;
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertNotNull("id should be available", agent.getId());
        }
    }

    /**
     * This test verifies that the "get all" function works properly
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     * @throws InvalidQuerySpecException
     */
    public void testSortOnHostName() throws PersistenceException, InvalidIDException, InvalidQuerySpecException {
        final int nbDA = 10;
        final int nbFSA = 5;
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        registerAgents(desktopAgentType, "DesktopHost", 10);
        registerAgents(fileServerAgentType, "FSAHost", 5);
        final AgentQuerySpecImpl queryAllSortOnName = new AgentQuerySpecImpl();
        queryAllSortOnName.addSortSpecTerm(new AgentSortTermImpl(AgentMgrSortFieldType.HOST, true));
        final IAgentQueryResults queryResults = this.agentMgrToTest.getAgents(queryAllSortOnName);
        IAgentDO[] agentsRetrieved = queryResults.getAgents();
        assertNotNull("There should be agents retrieved", agentsRetrieved);
        assertEquals("The correct number of agents should be retrieved", nbDA + nbFSA, agentsRetrieved.length);
        int size = agentsRetrieved.length;
        String currentHostName = "";
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertTrue("The host name should be ordered by name ascending", currentHostName.compareTo(agent.getHost()) < 0);
            currentHostName = agent.getHost();
        }
    }

    /**
     * Tests agent registration
     * 
     * @throws InvalidIDException
     * @throws PersistenceException
     * @throws InvalidQuerySpecException
     * 
     */
    public void testRegisterAgent() throws InvalidIDException, PersistenceException, InvalidQuerySpecException {
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());

        // Agent (A, Desktop)
        MockAgentRegistrationData mock_A_Desktop = new MockAgentRegistrationData("A", desktopAgentType);
        IAgentStartupConfiguration config_A_Desktop = this.agentMgrToTest.registerAgent(mock_A_Desktop);
        assertNotNull("registered agent not given configuration", config_A_Desktop);
        assertNotNull("registered agent not given id", config_A_Desktop.getId());
        assertNotNull("registered agent not given registration id", config_A_Desktop.getRegistrationId());
        assertNotNull("registered agent not given any comm profile", config_A_Desktop.getCommProfile());
        assertEquals("registered agent not given default comm profile", config_A_Desktop.getCommProfile().getName(), this.defaultDesktopCommProfile.getName());
        assertNotNull("registered agent not given any agent profile", config_A_Desktop.getAgentProfile());
        assertEquals("registered agent not given default agent profile", config_A_Desktop.getAgentProfile().getName(), this.defaultAgentProfile.getName());
        verifyPersistedAgentData(config_A_Desktop.getId(), mock_A_Desktop, this.defaultDesktopCommProfile, this.defaultAgentProfile, true, false, null, null, null, null);

        // Agent (A, WinFS)
        MockAgentRegistrationData mock_A_WinFS = new MockAgentRegistrationData("A", fileServerType);
        IAgentStartupConfiguration config_A_WinFS = this.agentMgrToTest.registerAgent(mock_A_WinFS);
        assertNotNull("registered agent not given configuration", config_A_WinFS);
        assertNotNull("registered agent not given id", config_A_WinFS.getId());
        assertNotNull("registered agent not given registration id", config_A_WinFS.getRegistrationId());
        assertNotNull("registered agent not given any comm profile", config_A_WinFS.getCommProfile());
        assertEquals("registered agent not given default comm profile", config_A_WinFS.getCommProfile().getName(), this.defaultFileServerCommProfile.getName());
        assertNotNull("registered agent not given any agent profile", config_A_WinFS.getAgentProfile());
        assertEquals("registered agent not given default agent profile", config_A_WinFS.getAgentProfile().getName(), this.defaultAgentProfile.getName());
        AgentDO persistedAgent = verifyPersistedAgentData(config_A_WinFS.getId(), mock_A_WinFS, this.defaultFileServerCommProfile, this.defaultAgentProfile, true, false, null, null, null, null);

        // Re-register Agent (A, WinFS) - need to ensure that the id is re-used
        IAgentStartupConfiguration config_A_WinFS2 = this.agentMgrToTest.registerAgent(mock_A_WinFS);
        assertEquals("re-registration of previously registered agent should return the same id as before", config_A_WinFS.getId(), config_A_WinFS2.getId());
        assertTrue("re-registration of previously registered agent should not return the same registration id", !config_A_WinFS.getRegistrationId().equals(config_A_WinFS2.getRegistrationId()));

        // Now unregister the agent and reregister. Make sure same id is used
        // and registered is set to true
        this.agentMgrToTest.unregisterAgent(persistedAgent.getId());
        IAgentStartupConfiguration config_A_WinFS3 = this.agentMgrToTest.registerAgent(mock_A_WinFS);
        verifyPersistedAgentData(config_A_WinFS3.getId(), mock_A_WinFS, this.defaultFileServerCommProfile, this.defaultAgentProfile, true, false, null, null, null, null);
        assertEquals("re-registration of previously registered agent should return the same id as before", config_A_WinFS2.getId(), config_A_WinFS3.getId());
        assertTrue("re-registration of previously registered agent should not return the same registration id", !config_A_WinFS2.getRegistrationId().equals(config_A_WinFS3.getRegistrationId()));

        // Register some erroneous stuff and make sure exception is thrown
        try {
            this.agentMgrToTest.registerAgent(null);
            fail("erroneous information should cause registerAgent() to throw an exception");
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    /**
     * Test setCommProfileForAgents
     * 
     * @throws URISyntaxException
     * @throws DataSourceException
     * @throws UniqueConstraintViolationException
     * @throws PersistenceException
     * @throws ProfileNotFoundException
     */
    public void testSetCommProfileForAgents() throws URISyntaxException, UniqueConstraintViolationException, DataSourceException, ProfileNotFoundException, PersistenceException {
        Set agentIds = new HashSet();

        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());

        // Register a few agents
        // Agent (A, Desktop)
        MockAgentRegistrationData mock_A_Desktop = new MockAgentRegistrationData("A", desktopAgentType);
        IAgentStartupConfiguration config_A_Desktop = this.agentMgrToTest.registerAgent(mock_A_Desktop);
        agentIds.add(config_A_Desktop.getId());

        // Agent (B, Desktop)
        MockAgentRegistrationData mock_B_Desktop = new MockAgentRegistrationData("B", desktopAgentType);
        IAgentStartupConfiguration config_B_Desktop = this.agentMgrToTest.registerAgent(mock_B_Desktop);
        agentIds.add(config_B_Desktop.getId());

        // Create a dummy profile
        ICommProfileData dummyCommProfileData = new CommProfileDataImpl("DummyCommProfile", new URI("http://www.dummy.com"), desktopAgentType, new TimeInterval(5), 20, 30, new TimeInterval(40), true, 80, "password", false);
        ICommProfileDO dummyCommProfile = this.profileMgr.createCommProfile(dummyCommProfileData);

        this.agentMgrToTest.setCommProfileForAgents(agentIds, dummyCommProfile.getId());

        // Retrieve the agents and make sure they have the right profiles
        IAgentDO mock_A_Desktop_Agent = this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        assertEquals("Ensure Agent A has correct profile", dummyCommProfile, mock_A_Desktop_Agent.getCommProfile());
        IAgentDO mock_B_Desktop_Agent = this.agentMgrToTest.getAgentById(config_B_Desktop.getId());
        assertEquals("Ensure Agent B has correct profile", dummyCommProfile, mock_B_Desktop_Agent.getCommProfile());

        NullPointerException expectedException = null;
        try {
            this.agentMgrToTest.setCommProfileForAgents(agentIds, null);
        } catch (NullPointerException exception) {
            expectedException = exception;
        }
        assertNotNull("Ensure null pointer thrown for null profile id", expectedException);

        expectedException = null;
        try {
            this.agentMgrToTest.setCommProfileForAgents(null, dummyCommProfile.getId());
        } catch (NullPointerException exception) {
            expectedException = exception;
        }
        assertNotNull("Ensure null pointer thrown for null agent id set", expectedException);
    }

    /**
     * Tests agent shutdown
     * 
     * @throws PersistenceException
     * @throws InvalidQuerySpecException
     * 
     */
    public void testShutdownAgent() throws InvalidIDException, PersistenceException, InvalidQuerySpecException {
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());

        // Agent (A, DesktopAgent)
        MockAgentRegistrationData mock_A_Desktop = new MockAgentRegistrationData("A", desktopAgentType);
        IAgentStartupConfiguration config_A_Desktop = this.agentMgrToTest.registerAgent(mock_A_Desktop);
        assertNotNull("registered agent not given configuration", config_A_Desktop);
        this.agentMgrToTest.startupAgent(config_A_Desktop.getId(), new MockStartupAgentData(null));
        this.agentMgrToTest.shutdownAgent(config_A_Desktop.getId(), new MockShutdownAgentData());
        verifyPersistedAgentData(config_A_Desktop.getId(), mock_A_Desktop, this.defaultDesktopCommProfile, this.defaultAgentProfile, true, false, null, null, null, null);

        // Agent (A, WinFSAgent)
        Integer pushPort = new Integer(35);
        MockAgentRegistrationData mock_A_WinFS = new MockAgentRegistrationData("A", fileServerAgentType);
        IAgentStartupConfiguration config_A_WinFS = this.agentMgrToTest.registerAgent(mock_A_WinFS);
        assertNotNull("registered agent not given configuration", config_A_WinFS);
        this.agentMgrToTest.startupAgent(config_A_WinFS.getId(), new MockStartupAgentData(pushPort));
        this.agentMgrToTest.shutdownAgent(config_A_WinFS.getId(), new MockShutdownAgentData());
        verifyPersistedAgentData(config_A_WinFS.getId(), mock_A_WinFS, this.defaultFileServerCommProfile, this.defaultAgentProfile, true, false, null, null, null, null);

        // Test with erroneous shutdown info - verify exception.
        // Startup erroneous agent - verify exception
        MockAgentRegistrationData mock_B_WinFS = new MockAgentRegistrationData("B", fileServerAgentType);
        try {
            IAgentStartupConfiguration config_B_WinFS = this.agentMgrToTest.registerAgent(mock_B_WinFS);
            this.agentMgrToTest.startupAgent(config_B_WinFS.getId(), new MockStartupAgentData(pushPort));
            this.agentMgrToTest.shutdownAgent(new Long(Long.MAX_VALUE), new MockShutdownAgentData());
            fail("erroneous information should cause startupAgent() to throw an exception");
        } catch (InvalidIDException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests agent startup
     * 
     * @throws PersistenceException
     * @throws InvalidQuerySpecException
     * 
     */
    public void testStartupAgent() throws InvalidIDException, PersistenceException, InvalidQuerySpecException {
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());

        // Register agent (A, DesktopAgent)
        MockAgentRegistrationData mock_A_Desktop = new MockAgentRegistrationData("A", desktopAgentType);
        IAgentStartupConfiguration config_A_Desktop = this.agentMgrToTest.registerAgent(mock_A_Desktop);
        assertNotNull("registered agent not given configuration", config_A_Desktop);
        this.agentMgrToTest.startupAgent(config_A_Desktop.getId(), new MockStartupAgentData(null));
        verifyPersistedAgentData(config_A_Desktop.getId(), mock_A_Desktop, this.defaultDesktopCommProfile, this.defaultAgentProfile, true, false, null, null, null, null);

        // Register agent (A, WinFSAgent)
        // Startup agent (A, WinFSAgent)
        Integer pushPort = new Integer(35);
        MockAgentRegistrationData mock_A_WinFS = new MockAgentRegistrationData("A", fileServerAgentType);
        IAgentStartupConfiguration config_A_WinFS = this.agentMgrToTest.registerAgent(mock_A_WinFS);
        assertNotNull("registered agent not given configuration", config_A_WinFS);
        this.agentMgrToTest.startupAgent(config_A_WinFS.getId(), new MockStartupAgentData(pushPort));
        verifyPersistedAgentData(config_A_WinFS.getId(), mock_A_WinFS, this.defaultFileServerCommProfile, this.defaultAgentProfile, true, true, pushPort, null, null, null);

        // Shutdown and unregister agent. Restart and ensure that it's
        // registered again
        this.agentMgrToTest.shutdownAgent(config_A_WinFS.getId(), new MockShutdownAgentData());
        this.agentMgrToTest.unregisterAgent(config_A_WinFS.getId());
        verifyPersistedAgentData(config_A_WinFS.getId(), mock_A_WinFS, this.defaultFileServerCommProfile, this.defaultAgentProfile, false, false, null, null, null, null);
        this.agentMgrToTest.startupAgent(config_A_WinFS.getId(), new MockStartupAgentData(pushPort));
        verifyPersistedAgentData(config_A_WinFS.getId(), mock_A_WinFS, this.defaultFileServerCommProfile, this.defaultAgentProfile, true, true, pushPort, null, null, null);

        // Startup erroneous agent - verify exception
        MockAgentRegistrationData mock_B_WinFS = new MockAgentRegistrationData("B", fileServerAgentType);
        try {
            IAgentStartupConfiguration config_B_WinFS = this.agentMgrToTest.registerAgent(mock_B_WinFS);
            this.agentMgrToTest.startupAgent(new Long(Long.MAX_VALUE), new MockStartupAgentData(pushPort));
            fail("erroneous information should cause startupAgent() to throw an exception");
        } catch (InvalidIDException e) {
            assertTrue(true);
        }
    }

    /**
     * This test verifies that the agent unregistration works properly
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     */
    public void testUnregisterAgent() throws PersistenceException, InvalidIDException {
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        MockAgentRegistrationData mock_A_Desktop = new MockAgentRegistrationData("A", desktopAgentType);
        IAgentStartupConfiguration config_A_Desktop = this.agentMgrToTest.registerAgent(mock_A_Desktop);
        AgentDO agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        assertTrue("Ensure agent is registered", agent.isRegistered());

        this.agentMgrToTest.unregisterAgent(agent.getId());
        agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_Desktop.getId());
        assertFalse("Ensure agent is now unregistered", agent.isRegistered());
        assertNull("Ensure agent has no comm profile status", agent.getProfileStatus());
        assertNull("Ensure agent has no policy assembly status", agent.getPolicyAssemblyStatus());

        // Test null pointer
        NullPointerException npe = null;
        try {
            this.agentMgrToTest.unregisterAgent(null);
        } catch (NullPointerException exception) {
            npe = exception;
        }
        assertNotNull("Ensure npe throw for null paramter to unregister", npe);
    }

    /**
     * Registers a number of agents for test purpose
     * 
     * @param desktopAgentType
     *            type of agent to register
     * @param hostNamePattern
     *            host name pattern
     * @param number
     *            number of agents to register
     * @throws PersistenceException
     *             if registration fails
     */
    private void registerAgents(IAgentType desktopAgentType, String hostNamePattern, int number) throws PersistenceException {
        for (int i = 0; i < number; i++) {
            MockAgentRegistrationData mockAgent = new MockAgentRegistrationData(hostNamePattern + i, desktopAgentType);
            IAgentStartupConfiguration agentConfig = this.agentMgrToTest.registerAgent(mockAgent);
        }
    }

    /**
     * Performs cleanup before the next test. All test data is erased.
     */
    protected void tearDown() throws Exception {
        IHibernateRepository ds = getDataSource();
        Session s = null;
        Transaction t = null;
        // Delete all agents
        deleteAllAgents();

        try {
            s = ds.getSession();
            t = s.beginTransaction();
            s.delete("from AgentProfileDO p where p.id != " + this.defaultAgentProfile.getId());
            s.delete("from CommProfileDO p where p.id != " + this.defaultDesktopCommProfile.getId() + " AND p.id != " + this.defaultFileServerCommProfile.getId());
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        // Delete all heartbeat records
        HeartbeatRecorderTestingUtils.deleteAllHeartbeats();
        super.tearDown();
    }

    /**
     * Validates the persisted record of the agent against expected data
     * 
     * @param expectedToBeRegistered
     *            TODO
     * @param host_A
     * @param config_A_Desktop
     * 
     * @throws PersistenceException
     *             return the persisted agent if data is verified
     */
    public AgentDO verifyPersistedAgentData(Long id, IAgentRegistrationData registrationData, ICommProfileDO expectedCommProfile, IAgentProfileDO expectedAgentProfile, boolean expectedToBeRegistered, boolean expectedToBePushReady,
            Integer expectedPushPort, Calendar expectedLastHeartbeat, IAgentProfileStatusData expectedProfileStatus, IAgentPolicyAssemblyStatusData expectedPolicyAssemblyStatus) throws PersistenceException, InvalidQuerySpecException {
        AgentDO persistedAgent = (AgentDO) this.agentMgrToTest.getAgentById(id);
        assertNotNull("registered agent cannot be accessed via id", persistedAgent);

        // Check profile settings:
        assertNotNull("persisted agent not given any agent profile", persistedAgent.getAgentProfile());
        assertEquals("persisted agent not given correct agent profile", persistedAgent.getAgentProfile().getName(), expectedAgentProfile.getName());
        assertNotNull("persisted agent not given any comm profile", persistedAgent.getCommProfile());
        assertEquals("persisted agent not given correct comm profile", persistedAgent.getCommProfile().getName(), expectedCommProfile.getName());

        // Check embedded properties:
        assertEquals("persisted agent doesn't reflect correct host", persistedAgent.getHost(), registrationData.getHost());
        assertEquals("persisted agent doesn't reflect correct type", persistedAgent.getType(), registrationData.getType());
        assertEquals("persisted agent doesn't reflect correct active status", expectedToBeRegistered, persistedAgent.isRegistered());
        assertEquals("persisted agent doesn't reflect correct push ready status", persistedAgent.getIsPushReady(), expectedToBePushReady);
        if (expectedPushPort != null) {
            assertEquals("persisted agent does not have the expected push port", persistedAgent.getPushPort(), expectedPushPort);
        } else {
            assertFalse("persisted agent should not have a push port at this time", persistedAgent.hasPushPort());
        }

        if (expectedLastHeartbeat != null) {
            assertEquals("persisted agent does not have the expected heartbeat timestamp", persistedAgent.getLastHeartbeat(), expectedLastHeartbeat);
        } else {
            assertNull("persisted agent should not have a heartbeat timestamp at this time", persistedAgent.getLastHeartbeat());
        }

        // Ensure profile status is as expected
        if (expectedProfileStatus != null) {
            IAgentProfileStatus profileStatus = persistedAgent.getProfileStatus();
            assertNotNull("persisted agent is expected to have profile status information", profileStatus);

            if (expectedProfileStatus.getLastCommittedAgentProfileName() != null) {
                assertEquals("persisted agent doesn't reflect correct agent profile name", expectedProfileStatus.getLastCommittedAgentProfileName(), profileStatus.getLastAcknowledgedAgentProfileName());
                assertEquals("persisted agent doesn't reflect correct agent profile timestamp", expectedProfileStatus.getLastCommittedAgentProfileTimestamp(), profileStatus.getLastAcknowledgedAgentProfileTimestamp());
            }

            if (expectedProfileStatus.getLastCommittedCommProfileName() != null) {
                assertEquals("persisted agent doesn't reflect correct comm profile name", expectedProfileStatus.getLastCommittedCommProfileName(), profileStatus.getLastAcknowledgedCommProfileName());
                assertEquals("persisted agent doesn't reflect correct comm profile timestamp", expectedProfileStatus.getLastCommittedCommProfileTimestamp(), profileStatus.getLastAcknowledgedCommProfileTimestamp());
            }
        } else {
            IAgentProfileStatus profileStatus = persistedAgent.getProfileStatus();
            assertTrue("persisted agent should not have any profile status data at this point", (profileStatus == null)
                    || ((profileStatus.getLastAcknowledgedAgentProfileName() == null) && (profileStatus.getLastAcknowledgedAgentProfileTimestamp() == null) && (profileStatus.getLastAcknowledgedCommProfileName() == null) && (profileStatus
                            .getLastAcknowledgedCommProfileTimestamp() == null)));
        }

        // Ensure policy assembly status is as expected:
        if (expectedPolicyAssemblyStatus != null) {
            IAgentPolicyAssemblyStatus policyAssemblyStatus = persistedAgent.getPolicyAssemblyStatus();
            assertNotNull("persisted agent is expected to have policy assembly status information", policyAssemblyStatus);
            assertEquals("persisted agent does not reflect the correct policy assembly status information", policyAssemblyStatus.getLastAcknowledgedDeploymentBundleTimestamp(), expectedPolicyAssemblyStatus.getLastCommittedDeploymentBundleTimestamp());
        } else {
            IAgentPolicyAssemblyStatus policyAssemblyStatus = persistedAgent.getPolicyAssemblyStatus();
            assertTrue("persisted agent is expected to not have any policy assembly status at this time", (policyAssemblyStatus == null) || (policyAssemblyStatus.getLastAcknowledgedDeploymentBundleTimestamp() == null));
        }

        // Verify that the same agent is obtained when querying by host and
        // type:
        final AgentQuerySpecImpl agentByHostAndType = new AgentQuerySpecImpl();
        agentByHostAndType.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.EQUALS, registrationData.getHost()));
        agentByHostAndType.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, registrationData.getType()));
        IAgentQueryResults result = this.agentMgrToTest.getAgents(agentByHostAndType);
        AgentDO tempAgent = null;
        if (result.getAgents() != null && result.getAgents().length > 0) {
            tempAgent = (AgentDO) result.getAgents()[0];
        }
        assertNotNull("persisted agent cannot be accessed via host and type", tempAgent);
        assertEquals("persisted agent record does not have expected id", tempAgent.getId(), persistedAgent.getId());

        return persistedAgent;
    }

    public void testGetAgentStatistics() throws InterruptedException, PersistenceException, InvalidIDException, HibernateException {
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());

        IAgentStatistics agentStats = this.agentMgrToTest.getAgentStatistics();
        // Ensure all stats initially 0
        assertTrue("Ensure desktop agent count initially zero", agentStats.getAgentCount(desktopAgentType) == 0);
        assertTrue("Ensure file server agent count initially zero", agentStats.getAgentCount(fileServerAgentType) == 0);
        assertTrue("Ensure heartbeat count initially zero", agentStats.getHeartbeatsInLastDayCount() == 0);
        assertTrue("Ensure total agent count initially zero", agentStats.getTotalAgentCount() == 0);
        assertTrue("Ensure disconnected agent count initially zero", agentStats.getAgentsDisconnectedInLastDayCount() == 0);
        assertTrue("Ensure agents with out of data policy count initially zero", agentStats.getAgentsWithOutOfDatePolicies() == 0);

        // Now, add agents and reverify data
        // Agent (A, Desktop)
        MockAgentRegistrationData mock_A_Desktop = new MockAgentRegistrationData("A", desktopAgentType);
        IAgentStartupConfiguration config_A_Desktop = this.agentMgrToTest.registerAgent(mock_A_Desktop);
        this.agentMgrToTest.startupAgent(config_A_Desktop.getId(), new MockStartupAgentData(null));

        // Agent (A, WinFS)
        Integer pushPort = new Integer(35);
        MockAgentRegistrationData mock_A_WinFS = new MockAgentRegistrationData("A", fileServerAgentType);
        IAgentStartupConfiguration config_A_WinFS = this.agentMgrToTest.registerAgent(mock_A_WinFS);
        this.agentMgrToTest.startupAgent(config_A_WinFS.getId(), new MockStartupAgentData(pushPort));

        // Send a heart beat
        MockAgentHeartbeatData heartbeat_A_Desktop = new MockAgentHeartbeatData();
        IAgentUpdates updates = this.agentMgrToTest.checkUpdates(config_A_Desktop.getId(), heartbeat_A_Desktop);

        // For file system, set last heart beat time to day earlier
        Calendar dateToSet = Calendar.getInstance();
        dateToSet.add(Calendar.DATE, -5);
        AgentDO agent = (AgentDO) this.agentMgrToTest.getAgentById(config_A_WinFS.getId());
        agent.setLastHeartbeat(dateToSet);

        // For file system, set last deployment time to beginning of time
        dateToSet = Calendar.getInstance();
        dateToSet.setTimeInMillis(0);
        AgentPolicyAssemblyStatus policyAssemblyStatus = new AgentPolicyAssemblyStatus();
        policyAssemblyStatus.setLastAcknowledgedDeploymentBundleTimestamp(dateToSet);
        agent.setPolicyAssemblyStatus(policyAssemblyStatus);

        IHibernateRepository ds = getDataSource();
        Session s = null;
        Transaction t = null;
        try {
            s = ds.getSession();
            t = s.beginTransaction();
            s.saveOrUpdateCopy(agent);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.closeSession(s, null);
        } finally {
            HibernateUtils.rollbackTransation(t, null);
        }

        Thread.sleep(TEST_HEARTBEAT_RECORD_PROCESS_INTERAL.longValue() + 5000);

        // Now, test again
        agentStats = this.agentMgrToTest.getAgentStatistics();
        assertEquals("Ensure desktop agent count is now one", 1, agentStats.getAgentCount(desktopAgentType));
        assertEquals("Ensure file agent count now one", 1, agentStats.getAgentCount(fileServerAgentType));
        assertEquals("Ensure heartbeat agent count is now one", 1, agentStats.getHeartbeatsInLastDayCount());
        assertEquals("Ensure total agent count now 2", 2, agentStats.getTotalAgentCount());
        assertEquals("Ensure disconnected agent count is now 1", 1, agentStats.getAgentsDisconnectedInLastDayCount());
        assertEquals("Ensure agents with out of data policy count is now 1", 1, agentStats.getAgentsWithOutOfDatePolicies());

        // Unregister an agent and test again
        this.agentMgrToTest.unregisterAgent(agent.getId());
        agentStats = this.agentMgrToTest.getAgentStatistics();
        assertEquals("Ensure desktop agent count is now one after unregistration", 1, agentStats.getAgentCount(desktopAgentType));
        assertEquals("Ensure file agent count now zero after unregistration", 0, agentStats.getAgentCount(fileServerAgentType));
        assertEquals("Ensure heartbeat agent count is now one after unregistration", 1, agentStats.getHeartbeatsInLastDayCount());
        assertEquals("Ensure total agent count now 1 after unregistration", 1, agentStats.getTotalAgentCount());
        assertEquals("Ensure disconnected agent count is now 0 after unregistration", 0, agentStats.getAgentsDisconnectedInLastDayCount());
        assertEquals("Ensure agents with out of data policy count is now 0 after unregistration", 0, agentStats.getAgentsWithOutOfDatePolicies());
    }

    /**
     * Tests AgentManager.getAgents(). There are already some test methods which
     * do this. However, they are not comprehensive. This is at least the
     * beginning of a comprehensive unit test
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     * 
     */
    public void testGetAgents() throws PersistenceException, InvalidIDException {
        runGetAgentsByAllProperties();
    }

    private void runGetAgentsByAllProperties() throws PersistenceException, InvalidIDException {
        int numberOfDesktopAgents = 5;
        int numberOfFileServerAgents = 10;
        String desktopHostNamePattern = "DesktopHost";
        String fileServerHostNamePattern = "FileServerHost";
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        registerAgents(desktopAgentType, desktopHostNamePattern, numberOfDesktopAgents);
        registerAgents(fileServerAgentType, fileServerHostNamePattern, numberOfFileServerAgents);

        // test by host name property
        AgentQuerySpecImpl testQuery = new AgentQuerySpecImpl();
        testQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.LIKE, desktopHostNamePattern.substring(0, 1) + "*"));
        IAgentQueryResults queryResults = this.agentMgrToTest.getAgents(testQuery);
        IAgentDO[] agentsRetrieved = queryResults.getAgents();
        int size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved for hostname query", numberOfDesktopAgents, size);
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertTrue("The query by name should work with wildchar", agent.getHost().indexOf(desktopHostNamePattern) == 0);
        }

        IAgentDO sampleAgent = agentsRetrieved[0];

        // Test id property
        testQuery = new AgentQuerySpecImpl();
        testQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.ID, RelationalOp.EQUALS, sampleAgent.getId()));
        queryResults = this.agentMgrToTest.getAgents(testQuery);
        agentsRetrieved = queryResults.getAgents();
        size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved for ID query", 1, size);
        assertEquals("runGetAgentsByAllProperties - The correct agent should be retrieve for ID query", sampleAgent, agentsRetrieved[0]);

        // Test comm profile id property
        testQuery = new AgentQuerySpecImpl();
        testQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.COMM_PROFILE_ID, RelationalOp.EQUALS, sampleAgent.getCommProfile().getId()));
        queryResults = this.agentMgrToTest.getAgents(testQuery);
        agentsRetrieved = queryResults.getAgents();
        size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved for comm profile id", numberOfDesktopAgents, size);
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertEquals("The query by comm profile id should retrieve the correct agents", sampleAgent.getCommProfile().getId(), agent.getCommProfile().getId());
        }

        // Test type property
        testQuery = new AgentQuerySpecImpl();
        testQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, fileServerAgentType));
        queryResults = this.agentMgrToTest.getAgents(testQuery);
        agentsRetrieved = queryResults.getAgents();
        size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved for agent type", numberOfFileServerAgents, size);
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertEquals("The query by agent type should retrieve the correct agents", fileServerAgentType, agent.getType());
        }

        // Test registered property
        this.agentMgrToTest.unregisterAgent(sampleAgent.getId());
        testQuery = new AgentQuerySpecImpl();
        testQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.REGISTERED, RelationalOp.EQUALS, Boolean.TRUE));
        queryResults = this.agentMgrToTest.getAgents(testQuery);
        agentsRetrieved = queryResults.getAgents();
        size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved for agent type", numberOfFileServerAgents + numberOfDesktopAgents - 1, size);
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertFalse("The query by agent registered should retrieve the correct agents", sampleAgent.equals(agent));
        }

        // Test online property
        Calendar beforeHeartbeat = Calendar.getInstance();
        MockAgentHeartbeatData heartBeatData = new MockAgentHeartbeatData();
        this.agentMgrToTest.checkUpdates(sampleAgent.getId(), heartBeatData);
        testQuery = new AgentQuerySpecImpl();
        testQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.ONLINE, RelationalOp.EQUALS, Boolean.FALSE));
        queryResults = this.agentMgrToTest.getAgents(testQuery);
        agentsRetrieved = queryResults.getAgents();
        size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved for agent is online", numberOfFileServerAgents + numberOfDesktopAgents - 1, size);
        for (int i = 0; i < size; i++) {
            IAgentDO agent = agentsRetrieved[i];
            assertFalse("The query by agent online should retrieve the correct agents", sampleAgent.equals(agent));
        }
        // Test policy up to date
        // Subtract one second to be sure enough time has passed
        beforeHeartbeat.add(Calendar.SECOND, -1);
        testQuery = new AgentQuerySpecImpl();
        testQuery.addSearchSpecTerm(this.queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.LAST_POLICY_UPDATE, RelationalOp.GREATER_THAN, beforeHeartbeat));
        queryResults = this.agentMgrToTest.getAgents(testQuery);
        agentsRetrieved = queryResults.getAgents();
        size = agentsRetrieved.length;
        assertEquals("The correct number of agents should be retrieved for agent last policy update", 1, size);
        assertEquals("runGetAgentsByAllProperties - The correct agent should be retrieve for query", sampleAgent, agentsRetrieved[0]);
    }

    public void testGetAgentTypes() {
        List<IAgentType> agentTypes = this.agentMgrToTest.getAgentTypes();
        assertNotNull("Ensure agent types is not null.", agentTypes);

        IAgentType lastAgentType = null;
        for (IAgentType nextAgentType : agentTypes) {
            if (lastAgentType != null) {
                assertTrue("testGetAgentTypes - Ensure agent type lists sorted by title", nextAgentType.getTitle().compareTo(lastAgentType.getTitle()) > 0);
            }

            lastAgentType = nextAgentType;
        }
    }

    public void testGetAgentType() {
        IAgentType desktopAgentType = this.agentMgrToTest.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        assertNotNull("Ensure desktop agent type is not null", desktopAgentType);

        try {
            this.agentMgrToTest.getAgentType("foobar");
            fail("Should throw Illegal Argument Exception");
        } catch (IllegalArgumentException exception) {
        }

        try {
            this.agentMgrToTest.getAgentType(null);
            fail("Should throw NPE");
        } catch (NullPointerException exception) {
        }
    }

    private class AgentProfileDataImpl extends BaseProfileDataImpl implements IAgentProfileData {

        private boolean isLogViewingEnabled;
        private boolean isTrayIconEnabled;

        /**
         * Constructor
         * 
         * @param isLogViewingEnabled
         * @param isTrayIconEnabled
         * @param name
         */
        public AgentProfileDataImpl(String name, boolean isLogViewingEnabled, boolean isTrayIconEnabled) {
            super(name);

            this.isLogViewingEnabled = isLogViewingEnabled;
            this.isTrayIconEnabled = isTrayIconEnabled;
        }

        /**
         * @see com.bluejungle.destiny.container.dms.components.profilemgr.IAgentProfileData#isLogViewingEnabled()
         */
        public boolean isLogViewingEnabled() {
            return this.isLogViewingEnabled;
        }

        /**
         * @see com.bluejungle.destiny.container.dms.components.profilemgr.IAgentProfileData#isTrayIconEnabled()
         */
        public boolean isTrayIconEnabled() {
            return this.isTrayIconEnabled;
        }
    }

    private class BaseProfileDataImpl {

        private String name;

        private BaseProfileDataImpl(String name) {
            this.name = name;
        }

        /**
         * Retrieve the name of the profile to create
         * 
         * @return the name of the profile to create
         */
        public String getName() {
            return this.name;
        }
    }

    private class CommProfileDataImpl extends BaseProfileDataImpl implements ICommProfileData {

        private URI dabsLocation;
        private IAgentType agentType;
        private TimeInterval heartBeatFrequency;
        private int logExpiration;
        private int logLimit;
        private TimeInterval logFrequency;
        private boolean pushEnabled;
        private int defaultPushPort;
        private boolean secure;
        private String password;

        /**
         * Constructor
         * 
         * @param commProfileName
         * @param agentPushParameters
         * @param dabsLocation
         * @param heartBeatFrequency
         * @param logExpiration
         * @param logFrequency
         * @param logLimit
         * @param secure
         */
        public CommProfileDataImpl(String commProfileName, URI dabsLocation, IAgentType agentType, TimeInterval heartBeatFrequency, int logExpiration, int logLimit, TimeInterval logFrequency, boolean pushEnabled, int defaultPushPort, String password,
                boolean secure) {
            super(commProfileName);

            this.dabsLocation = dabsLocation;
            this.agentType = agentType;
            this.heartBeatFrequency = heartBeatFrequency;
            this.logExpiration = logExpiration;
            this.logLimit = logLimit;
            this.logFrequency = logFrequency;
            this.pushEnabled = pushEnabled;
            this.defaultPushPort = defaultPushPort;
            this.secure = secure;
            this.password = password;
        }

        /**
         * @see com.bluejungle.destiny.container.dms.components.profilemgr.ICommProfileData#getDABSLocation()
         */
        public URI getDABSLocation() {
            return this.dabsLocation;
        }

        /**
         * @see com.bluejungle.destiny.container.dms.components.profilemgr.ICommProfileData#getHeartBeatFrequency()
         */
        public TimeInterval getHeartBeatFrequency() {
            return this.heartBeatFrequency;
        }

        /**
         * @see com.bluejungle.destiny.container.dms.components.profilemgr.ICommProfileData#getLogExpiration()
         */
        public int getLogExpiration() {
            return this.logExpiration;
        }

        /**
         * @see com.bluejungle.destiny.container.dms.components.profilemgr.ICommProfileData#getLogLimit()
         */
        public int getLogLimit() {
            return this.logLimit;
        }

        /**
         * @see com.bluejungle.destiny.container.dms.components.profilemgr.ICommProfileData#getLogFrequency()
         */
        public TimeInterval getLogFrequency() {
            return this.logFrequency;
        }

        /**
         * @see com.bluejungle.destiny.container.dms.components.profilemgr.ICommProfileData#isSecure()
         */
        public boolean isSecure() {
            return this.secure;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getDefaultPushPort()
         */
        public int getDefaultPushPort() {
            return this.defaultPushPort;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#isPushEnabled()
         */
        public boolean isPushEnabled() {
            return this.pushEnabled;
        }

        /**
         * Retrieve the agentType.
         * 
         * @return the agentType.
         */
        public IAgentType getAgentType() {
            return this.agentType;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getPassword()
         */
        public String getPassword() {
            return this.password;
        }
    }

    public static class AgentStatisticsCollectorImplExtension extends AgentStatisticsCollectorImpl {

        private PolicyEditorIF policyEditorService;

        public AgentStatisticsCollectorImplExtension() {
            super();
            this.policyEditorService = new TestPolicyService();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentStatisticsCollectorImpl#getPolicyEditorService()
         */
        protected PolicyEditorIF getPolicyEditorService() throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException {
            return this.policyEditorService;
        }
    }

    private static class TestPolicyService extends DefaultPolicyEditorIF {

        private Calendar lastDeploymentTime = Calendar.getInstance();

        /**
         * @see com.bluejungle.destiny.services.policy.PolicyEditorIF#getNumDeployedPolicies()
         */
        public int getNumDeployedPolicies() throws RemoteException, ServiceNotReadyFault {
            return -1;
        }

        /**
         * @see com.bluejungle.destiny.services.policy.PolicyEditorIF#getLatestDeploymentTime()
         */
        public Calendar getLatestDeploymentTime() throws RemoteException, ServiceNotReadyFault {
            return this.lastDeploymentTime;
        }

    }
}

