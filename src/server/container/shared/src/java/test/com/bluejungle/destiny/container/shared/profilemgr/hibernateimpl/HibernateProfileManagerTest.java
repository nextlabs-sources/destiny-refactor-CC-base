/*
 * Created on Jan 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidIDException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.MockAgentRegistrationData;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryField;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryTerm;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryField;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryTerm;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.IUserProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IUserProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.ProfileNotFoundException;
import com.bluejungle.destiny.container.shared.profilemgr.UserProfileQueryField;
import com.bluejungle.destiny.container.shared.profilemgr.UserProfileQueryTerm;
import com.bluejungle.destiny.container.shared.profilemgr.UserProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;
import com.bluejungle.framework.utils.CryptUtils;
import com.bluejungle.framework.utils.TimeInterval;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/test/HibernateProfileManagerTest.java#1 $
 */

public class HibernateProfileManagerTest extends BaseContainerSharedTestCase {

    private static final Log LOG = LogFactory.getLog(HibernateProfileManagerTest.class.getName());

    private static final int MAX_NAME_LENGTH = 128;

    private IProfileManager profileManagerToTest;
    private List<IUserProfileDO> userProfilesToClean = new LinkedList<IUserProfileDO>();
    private List<ICommProfileDO> commProfilesToClean = new LinkedList<ICommProfileDO>();
    private List<IAgentProfileDO> agentProfilesToClean = new LinkedList<IAgentProfileDO>();

    private IAgentManager agentManager;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HibernateProfileManagerTest.class);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        
        // Initialize the agent manager:
        ComponentInfo agentMgrCompInfo = new ComponentInfo(IAgentManager.COMP_NAME, AgentManager.class.getName(), IAgentManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.agentManager = (IAgentManager) componentManager.getComponent(agentMgrCompInfo);

        ComponentInfo profileMgrCompInfo = new ComponentInfo(IProfileManager.COMP_NAME, HibernateProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.profileManagerToTest = (IProfileManager) componentManager.getComponent(profileMgrCompInfo);        
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Iterator userProfilesToCleanIterator = this.userProfilesToClean.iterator();
        while (userProfilesToCleanIterator.hasNext()) {
            try {
                this.profileManagerToTest.deleteUserProfile(((UserProfileDO) userProfilesToCleanIterator.next()).getId());
            } catch (DataSourceException exception) {
                LOG.warn("Failed to clean up created user profile", exception);
            }
        }

        Iterator commProfilesToCleanIterator = this.commProfilesToClean.iterator();
        while (commProfilesToCleanIterator.hasNext()) {
            try {
                this.profileManagerToTest.deleteCommProfile(((CommProfileDO) commProfilesToCleanIterator.next()).getId());
            } catch (DataSourceException exception) {
                LOG.warn("Failed to clean up created comm profile", exception);
            }
        }

        Iterator agentProfilesToCleanIterator = this.agentProfilesToClean.iterator();
        while (agentProfilesToCleanIterator.hasNext()) {
            try {
                this.profileManagerToTest.deleteAgentProfile(((AgentProfileDO) agentProfilesToCleanIterator.next()).getId());
            } catch (DataSourceException exception) {
                LOG.warn("Failed to clean up created agent profile", exception);
            }
        }

        super.tearDown();
    }

    /**
     * Constructor for HibernateProfileManagerTest.
     * 
     * @param testName
     *            name of the test
     */
    public HibernateProfileManagerTest(String testName) {
        super(testName);
    }

    public void testCreateUserProfile() throws DataSourceException, ProfileNotFoundException {
        String userProfileName = "testCreateProfile";

        // Don't use the short cut, since we're testing creation
        IUserProfileDO profileCreated = this.profileManagerToTest.createUserProfile(new UserProfileDataImpl(userProfileName));
        this.userProfilesToClean.add(profileCreated);
        assertNotNull("Ensure created user profile is not null.", profileCreated);
        assertNotNull("Ensure created date is not null", profileCreated.getCreatedDate());
        assertNotNull("Ensure modified date is not null", profileCreated.getModifiedDate());
        assertEquals("Ensure user profile name is as provided", userProfileName, profileCreated.getName());

        // Ensure we can retrieve the same user profile from DB
        IUserProfileDO profileToCompare = this.profileManagerToTest.retrieveUserProfileByName(userProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);
        
        boolean exThrown = false;
        try {
            StringBuffer nameBuffer = new StringBuffer();
            for (int i = 0; i < MAX_NAME_LENGTH + 1; i++) {
                nameBuffer.append("s");
            }

            profileCreated = this.profileManagerToTest.createUserProfile(new UserProfileDataImpl(nameBuffer.toString()));
            this.profileManagerToTest.deleteUserProfile(profileCreated.getId());
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException is name is too long", exThrown);
    }

    public void testCreateCommProfile() throws DataSourceException, ProfileNotFoundException, URISyntaxException, NoSuchAlgorithmException {
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        
        String commProfileName = "testCreateProfile";
        URI dabsLocation = new URI("http://www.foo.com");
        IAgentType agentType = desktopAgentType;
        int heartBeatFrequency = 10;
        int logExpiration = 20;
        int logLimit = 30;
        int logFrequency = 40;
        boolean pushEnabled = true;
        int defaultPushPort = 2000;
		String password="password";
        boolean secure = false;

        // Don't use shortcut method, since we're testing creation
        ICommProfileData commProfileData = new CommProfileDataImpl(commProfileName, dabsLocation, agentType, new TimeInterval(heartBeatFrequency, TimeInterval.TimeUnit.SECONDS), logExpiration, logLimit, new TimeInterval(logFrequency, TimeInterval.TimeUnit.SECONDS),
                pushEnabled, defaultPushPort, password, secure);
        ICommProfileDO profileCreated = this.profileManagerToTest.createCommProfile(commProfileData);
        this.commProfilesToClean.add(profileCreated);
        assertNotNull("Ensure created comm profile is not null.", profileCreated);
        assertNotNull("Ensure created date is not null", profileCreated.getCreatedDate());
        assertNotNull("Ensure modified date is not null", profileCreated.getModifiedDate());
        assertEquals("Ensure comm profile name is as provided", commProfileName, profileCreated.getName());
        assertEquals("Ensure DABS Location is as provided", dabsLocation, profileCreated.getDABSLocation());
        assertEquals("Ensure heart beat rate is as provided", heartBeatFrequency, profileCreated.getHeartBeatFrequency().getTime());       
        assertEquals("Ensure log limit is as provided", logLimit, profileCreated.getLogLimit());
        assertEquals("Ensure log frequency is as provided", logFrequency, profileCreated.getLogFrequency().getTime());
        assertEquals("Ensure push enabled is as provided", pushEnabled, profileCreated.isPushEnabled());
        assertEquals("Ensure default push port is as provided", defaultPushPort, profileCreated.getDefaultPushPort());
        assertTrue("Ensure password hash as expected given provided password", Arrays.equals(CryptUtils.digest(password, "SHA1", 0), profileCreated.getPasswordHash()));
        
        // Ensure we can retrieve the same comm profile from DB
        ICommProfileDO profileToCompare = this.profileManagerToTest.retrieveCommProfileByName(commProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Test with bad data
        boolean exThrown = false;
        try {
            profileCreated = this.profileManagerToTest.createCommProfile(null);
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException if data is null", exThrown);
        
        /**
         * Try creating profiles with the same name
         */
        UniqueConstraintViolationException expectedException = null;
        try {
            profileCreated = this.profileManagerToTest.createCommProfile(commProfileData);
            this.commProfilesToClean.add(profileCreated);            
        } catch (UniqueConstraintViolationException exception) {
            expectedException = exception;
        }
        assertNotNull("Ensure unique violation constraint was thrown", expectedException);
        
        /**
         * Now, try with a different agent type
         */
        IAgentType fileServerAgentType = this.agentManager.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        commProfileData = new CommProfileDataImpl(commProfileName, dabsLocation, fileServerAgentType, new TimeInterval(heartBeatFrequency, TimeInterval.TimeUnit.SECONDS), logExpiration, logLimit, new TimeInterval(logFrequency, TimeInterval.TimeUnit.SECONDS),
                pushEnabled, defaultPushPort, password, secure);
        profileCreated = this.profileManagerToTest.createCommProfile(commProfileData);
        this.commProfilesToClean.add(profileCreated);          
    }

    public void testCreateAgentProfile() throws DataSourceException, ProfileNotFoundException {
        String agentProfileName = "testCreateProfile";
        boolean logViewingEnabled = true;
        boolean trayIconEnabled = true;

        // Don't use shortcut method, since we're testing creation
        IAgentProfileData agentProfileData = new AgentProfileDataImpl(agentProfileName, logViewingEnabled, trayIconEnabled);
        IAgentProfileDO profileCreated = this.profileManagerToTest.createAgentProfile(agentProfileData);
        this.agentProfilesToClean.add(profileCreated);
        assertNotNull("Ensure created agent profile is not null.", profileCreated);
        assertNotNull("Ensure created date is not null", profileCreated.getCreatedDate());
        assertNotNull("Ensure modified date is not null", profileCreated.getModifiedDate());
        assertEquals("Ensure agent profile name is as provided", agentProfileName, profileCreated.getName());
        assertEquals("Ensure logViewingEnabled is as provided", logViewingEnabled, profileCreated.isLogViewingEnabled());
        assertEquals("Ensure trayIconEnabled is as provided", trayIconEnabled, profileCreated.isTrayIconEnabled());

        // Ensure we can retrieve the same agent profile from DB
        IAgentProfileDO profileToCompare = this.profileManagerToTest.retrieveAgentProfileByName(agentProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Test with bad data
        boolean exThrown = false;
        try {
            profileCreated = this.profileManagerToTest.createAgentProfile(null);
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException if data is null", exThrown);
    }

    public void testRetrieveUserProfileByName() throws ProfileNotFoundException, ProfileNotFoundException, DataSourceException {
        String userProfileName = "testRetrieveProfileByName";

        // First try to retrieve it, should get an exception
        boolean exThrown = false;
        try {
            this.profileManagerToTest.retrieveUserProfileByName(userProfileName);
        } catch (ProfileNotFoundException exception) {
            // FIX ME - Check content of ProfileNotFoundException when exception
            // framework is complete
            exThrown = true;
        }
        assertTrue("Should throw ProfileNotFoundException", exThrown);

        // Now create it
        IUserProfileDO profileCreated = createTestUserProfile(userProfileName);

        // Now, make sure it is there
        IUserProfileDO profileToCompate = this.profileManagerToTest.retrieveUserProfileByName(userProfileName);
        assertEquals("Ensure profile from database is equal to that created", profileCreated, profileToCompate);

        // Test IllegalArgumentException with null name
        exThrown = false;
        try {
            this.profileManagerToTest.retrieveUserProfileByName(null);
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException", exThrown);
    }

    public void testRetrieveCommProfileByName() throws ProfileNotFoundException, ProfileNotFoundException, DataSourceException, URISyntaxException {
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        
        String commProfileName = "testRetrieveProfileByName";

        // First try to retrieve it, should get an exception
        boolean exThrown = false;
        try {
            this.profileManagerToTest.retrieveCommProfileByName(commProfileName);

        } catch (ProfileNotFoundException exception) {
            // FIX ME - Check content of ProfileNotFoundException when exception
            // framework is complete
            exThrown = true;
        }

        // Now create it
        URI dabsLocation = new URI("http://www.foo.com");
        IAgentType agentType = desktopAgentType;
        int heartBeatFrequency = 10;
        int logExpiration = 20;
        int logLimit = 30;
        int logFrequency = 40;
        boolean pushEnabled = true;
        int defaultPushPort = 2000;
		String password = "password";
        boolean secure = false;

        ICommProfileDO profileCreated = createTestCommProfile(commProfileName, dabsLocation, agentType, heartBeatFrequency, logExpiration, logLimit, logFrequency, pushEnabled, defaultPushPort, password, secure);

        // Now, make sure it is there
        ICommProfileDO profileToCompate = this.profileManagerToTest.retrieveCommProfileByName(commProfileName);
        assertEquals("Ensure profile from database is equal to that created", profileCreated, profileToCompate);

        // Test IllegalArgumentException with null name
        exThrown = false;
        try {
            this.profileManagerToTest.retrieveCommProfileByName(null);
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException", exThrown);
    }

    public void testRetrieveAgentProfileByName() throws ProfileNotFoundException, ProfileNotFoundException, DataSourceException {
        String agentProfileName = "testRetrieveProfileByName";

        // First try to retrieve it, should get an exception
        boolean exThrown = false;
        try {
            this.profileManagerToTest.retrieveAgentProfileByName(agentProfileName);
        } catch (ProfileNotFoundException exception) {
            // FIX ME - Check content of ProfileNotFoundException when exception
            // framework is complete
            exThrown = true;
        }
        assertTrue("Should throw ProfileNotFoundException", exThrown);

        // Now create it
        boolean logViewingEnabled = true;
        boolean trayIconEnabled = true;

        IAgentProfileDO profileCreated = createTestAgentProfile(agentProfileName, logViewingEnabled, trayIconEnabled);

        // Now, make sure it is there
        IAgentProfileDO profileToCompate = this.profileManagerToTest.retrieveAgentProfileByName(agentProfileName);
        ;
        assertEquals("Ensure profile from database is equal to that created", profileCreated, profileToCompate);

        // Test IllegalArgumentException with null name
        exThrown = false;
        try {
            this.profileManagerToTest.retrieveAgentProfileByName(null);
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException", exThrown);
    }

    public void testRetrieveUserProfileByID() throws ProfileNotFoundException, ProfileNotFoundException, DataSourceException {
        String userProfileName = "testRetrieveProfileByName";

        // Now create it
        IUserProfileDO profileCreated = createTestUserProfile(userProfileName);

        // Now, make sure it is there
        IUserProfileDO profileToCompate = this.profileManagerToTest.retrieveUserProfileByID(profileCreated.getId().longValue());
        assertEquals("Ensure profile from database is equal to that created", profileCreated, profileToCompate);

        // Try with bogus ID
        boolean exThrown = false;
        try {
            this.profileManagerToTest.retrieveUserProfileByID(999L);
        } catch (ProfileNotFoundException exception) {
            // FIX ME - Check content of ProfileNotFoundException when exception
            // framework is complete
            exThrown = true;
        }
        assertTrue("Should throw ProfileNotFoundException", exThrown);

    }

    public void testRetrieveCommProfileByID() throws ProfileNotFoundException, ProfileNotFoundException, DataSourceException, URISyntaxException {
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        
        String commProfileName = "testRetrieveProfileByName";
        URI dabsLocation = new URI("http://www.foo.com");
        IAgentType agentType = desktopAgentType;
        int heartBeatFrequency = 10;
        int logExpiration = 20;
        int logLimit = 30;
        int logFrequency = 40;
        boolean pushEnabled = true;
        int defaultPushPort = 2000;
		String password = "password";
        boolean secure = false;

        // Create it
        ICommProfileDO profileCreated = createTestCommProfile(commProfileName, dabsLocation, agentType, heartBeatFrequency, logExpiration, logLimit, logFrequency, pushEnabled, defaultPushPort, password, secure);

        // Now, make sure it is there
        ICommProfileDO profileToCompate = this.profileManagerToTest.retrieveCommProfileByID(profileCreated.getId().longValue());
        assertEquals("Ensure profile from database is equal to that created", profileCreated, profileToCompate);

        // Try with bogus ID
        boolean exThrown = false;
        try {
            this.profileManagerToTest.retrieveCommProfileByID(999L);
        } catch (ProfileNotFoundException exception) {
            // FIX ME - Check content of ProfileNotFoundException when exception
            // framework is complete
            exThrown = true;
        }
        assertTrue("Should throw ProfileNotFoundException", exThrown);
    }

    public void testRetrieveAgentProfileByID() throws ProfileNotFoundException, ProfileNotFoundException, DataSourceException {
        String agentProfileName = "testRetrieveProfileByName";
        boolean logViewingEnabled = true;
        boolean trayIconEnabled = true;

        // Create it
        IAgentProfileDO profileCreated = createTestAgentProfile(agentProfileName, logViewingEnabled, trayIconEnabled);

        // Now, make sure it is there
        IAgentProfileDO profileToCompate = this.profileManagerToTest.retrieveAgentProfileByID(profileCreated.getId().longValue());
        assertEquals("Ensure profile from database is equal to that created", profileCreated, profileToCompate);

        // Try with bogus ID
        boolean exThrown = false;
        try {
            this.profileManagerToTest.retrieveAgentProfileByID(999L);
        } catch (ProfileNotFoundException exception) {
            // FIX ME - Check content of ProfileNotFoundException when exception
            // framework is complete
            exThrown = true;
        }
        assertTrue("Should throw ProfileNotFoundException", exThrown);
    }

    public final void testRetrieveUserProfiles() throws DataSourceException {
        // Create 3 user profiles
        List<IUserProfileDO> userProfileDataSet = new LinkedList<IUserProfileDO>();
        IUserProfileDO profileOne = createTestUserProfile("testRetrieveProfileOne");
        userProfileDataSet.add(profileOne);

        IUserProfileDO profileTwo = createTestUserProfile("testRetrieveProfileTwo");
        userProfileDataSet.add(profileTwo);
        IUserProfileDO profileThree = createTestUserProfile("testRetrieveProfileThree");
        userProfileDataSet.add(profileThree);

        UserProfileQueryTermSet emptyQueryTermSet = new UserProfileQueryTermSet();
        //  Test with an empty query term list to see if we get all of them back
        List profilesRetrieved = this.profileManagerToTest.retrieveUserProfiles(emptyQueryTermSet, null, 5, null);
        assertEquals("Ensure full list of profiles is retrieved.", userProfileDataSet, profilesRetrieved);

        // Test with a single query term
        UserProfileQueryTerm singleQueryTerm = new UserProfileQueryTerm(UserProfileQueryField.NAME, profileOne.getName());
        UserProfileQueryTermSet userProfileQueryTermSet = new UserProfileQueryTermSet();
        userProfileQueryTermSet.addQueryTerm(singleQueryTerm);
        profilesRetrieved = this.profileManagerToTest.retrieveUserProfiles(userProfileQueryTermSet, null, 5, null);
        assertEquals("Ensure one profile returned", 1, profilesRetrieved.size());
        assertEquals("Ensure correct one profile returned", profileOne, profilesRetrieved.get(0));

        // Test paging - This test paging mechanism, sort criteria, and fetch
        // size
        // First page

        profilesRetrieved = this.profileManagerToTest.retrieveUserProfiles(emptyQueryTermSet, null, 2, UserProfileQueryField.NAME);
        assertEquals("Ensure two profiles returned in first page", 2, profilesRetrieved.size());
        assertEquals("Ensure correct profiles in first page One", profileOne, profilesRetrieved.get(0));
        assertEquals("Ensure correct profiles in first page One", profileThree, profilesRetrieved.get(1));

        // Second Page
        profilesRetrieved = this.profileManagerToTest.retrieveUserProfiles(emptyQueryTermSet, profileThree, 2, UserProfileQueryField.NAME);
        assertEquals("Ensure one profile returned in second page", 1, profilesRetrieved.size());
        assertEquals("Ensure last profile returned in second page", profileTwo, profilesRetrieved.get(0));
    }

    public final void testRetrieveCommProfiles() throws DataSourceException, URISyntaxException {

        List<ICommProfileDO> commProfileDataSet = new LinkedList<ICommProfileDO>();

        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentManager.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        IAgentType portalAgentType = this.agentManager.getAgentType(AgentTypeEnumType.PORTAL.getName());
        
        // Add default comm profile to data set
        ICommProfileDO defaultDesktopProfile = this.profileManagerToTest.getDefaultCommProfile(desktopAgentType);
        commProfileDataSet.add(defaultDesktopProfile);
        ICommProfileDO defaultFileServerProfile = this.profileManagerToTest.getDefaultCommProfile(fileServerAgentType);
        commProfileDataSet.add(defaultFileServerProfile);        
        ICommProfileDO defaultPortalProfile = this.profileManagerToTest.getDefaultCommProfile(portalAgentType);
        commProfileDataSet.add(defaultPortalProfile);

        //      Create 3 comm profiles
        String commProfileNameOne = "testRetrieveProfileOne";
        URI dabsLocationOne = new URI("http://www.foo.com");
        IAgentType agentTypeOne = desktopAgentType;
        int heartBeatFrequencyOne = 10;
        int logExpirationOne = 20;
        int logLimitOne = 30;
        int logFrequencyOne = 40;
        boolean pushEnabledOne = true;
        int defaultPushPortOne = 2000;
		String passwordOne = "passwordOne";
        boolean secureOne = false;

        ICommProfileDO profileOne = createTestCommProfile(commProfileNameOne, dabsLocationOne, agentTypeOne, heartBeatFrequencyOne, logExpirationOne, logLimitOne, logFrequencyOne, pushEnabledOne, defaultPushPortOne, passwordOne, secureOne);
        commProfileDataSet.add(profileOne);

        String commProfileNameTwo = "testRetrieveProfileTwo";
        URI dabsLocationTwo = new URI("http://www.fooTwo.com");
        IAgentType agentTypeTwo = desktopAgentType;
        int heartBeatFrequencyTwo = 100;
        int logExpirationTwo = 200;
        int logLimitTwo = 300;
        int logFrequencyTwo = 400;
        boolean pushEnabledTwo = false;
        int defaultPushPortTwo = 600;
		String passwordTwo = "passwordTwo";
        boolean secureTwo = true;

        ICommProfileDO profileTwo = createTestCommProfile(commProfileNameTwo, dabsLocationTwo, agentTypeTwo, heartBeatFrequencyTwo, logExpirationTwo, logLimitTwo, logFrequencyTwo, pushEnabledTwo, defaultPushPortTwo, passwordTwo, secureTwo);
        commProfileDataSet.add(profileTwo);

        String commProfileNameThree = "testRetrieveProfileThree";
        URI dabsLocationThree = new URI("http://www.fooThree.com");
        IAgentType agentTypeThree = fileServerAgentType;
        int heartBeatFrequencyThree = 1;
        int logExpirationThree = 2;
        int logLimitThree = 3;
        int logFrequencyThree = 4;
        boolean pushEnabledThree = true;
        int defaultPushPortThree= 7;
		String passwordThree = "passwordThree";
        boolean secureThree = false;

        ICommProfileDO profileThree = createTestCommProfile(commProfileNameThree, dabsLocationThree, agentTypeThree, heartBeatFrequencyThree, logExpirationThree, logLimitThree, logFrequencyThree, pushEnabledThree, defaultPushPortThree, passwordThree, secureThree);
        commProfileDataSet.add(profileThree);

        CommProfileQueryTermSet emptyQueryTermSet = new CommProfileQueryTermSet();
        //  Test with an empty query term list to see if we get all of them back
        List profilesRetrieved = this.profileManagerToTest.retrieveCommProfiles(emptyQueryTermSet, null, 6, null);
        assertEquals("Ensure full list of profiles is retrieved.", commProfileDataSet, profilesRetrieved);

        // Test with a single query term
        // Name
        CommProfileQueryTerm singleQueryTerm = new CommProfileQueryTerm(CommProfileQueryField.NAME, profileOne.getName());
        CommProfileQueryTermSet commProfileQueryTermSet = new CommProfileQueryTermSet();
        commProfileQueryTermSet.addQueryTerm(singleQueryTerm);
        profilesRetrieved = this.profileManagerToTest.retrieveCommProfiles(commProfileQueryTermSet, null, 6, null);
        assertEquals("Ensure one profile returned", 1, profilesRetrieved.size());
        assertEquals("Ensure correct one profile returned", profileOne, profilesRetrieved.get(0));

        // Test with two query terms
        // Name and dabsLocation
        CommProfileQueryTerm secondQueryTerm = new CommProfileQueryTerm(CommProfileQueryField.DABSLocation, profileOne.getDABSLocation());
        commProfileQueryTermSet.addQueryTerm(secondQueryTerm);
        profilesRetrieved = this.profileManagerToTest.retrieveCommProfiles(commProfileQueryTermSet, null, 6, null);
        assertEquals("Ensure one profile returned with two query terms", 1, profilesRetrieved.size());
        assertEquals("Ensure correct one profile returned with two query terms", profileOne, profilesRetrieved.get(0));

        secondQueryTerm = new CommProfileQueryTerm(CommProfileQueryField.DABSLocation, profileTwo.getDABSLocation());
        commProfileQueryTermSet = new CommProfileQueryTermSet();
        commProfileQueryTermSet.addQueryTerm(singleQueryTerm);
        commProfileQueryTermSet.addQueryTerm(secondQueryTerm);
        profilesRetrieved = this.profileManagerToTest.retrieveCommProfiles(commProfileQueryTermSet, null, 6, null);
        assertEquals("Ensure none returned with two query terms that conflict", 0, profilesRetrieved.size());

        // Test paging - This test paging mechanism, sort criteria, and fetch
        // size
        // First page
        profilesRetrieved = this.profileManagerToTest.retrieveCommProfiles(emptyQueryTermSet, null, 4, CommProfileQueryField.NAME);
        assertEquals("Ensure four profiles returned in first page", 4, profilesRetrieved.size());
        assertEquals("Ensure correct profiles in first page One", defaultDesktopProfile, profilesRetrieved.get(0));
        assertEquals("Ensure correct profiles in first page One", defaultFileServerProfile, profilesRetrieved.get(1));
        assertEquals("Ensure correct profiles in first page One", defaultPortalProfile, profilesRetrieved.get(2));
        assertEquals("Ensure correct profiles in first page Two", profileOne, profilesRetrieved.get(3));

        // Second Page
        profilesRetrieved = this.profileManagerToTest.retrieveCommProfiles(emptyQueryTermSet, profileOne, 2, CommProfileQueryField.NAME);
        assertEquals("Ensure two profiles returned in second page", 2, profilesRetrieved.size());
        assertEquals("Ensure correct profile returned in second page One", profileThree, profilesRetrieved.get(0));
        assertEquals("Ensure correct profile returned in second page Two", profileTwo, profilesRetrieved.get(1));
    }

    public final void testRetrieveAgentProfiles() throws DataSourceException {

        List<IAgentProfileDO> agentProfileDataSet = new LinkedList<IAgentProfileDO>();

        // Add default agent profile to data set
        agentProfileDataSet.add(this.profileManagerToTest.getDefaultAgentProfile());

        // Create 3 agent profiles
        String agentProfileNameOne = "testRetrieveProfileOne";
        boolean logViewingEnabledOne = true;
        boolean trayIconEnabledOne = true;

        IAgentProfileDO profileOne = createTestAgentProfile(agentProfileNameOne, logViewingEnabledOne, trayIconEnabledOne);
        agentProfileDataSet.add(profileOne);

        String agentProfileNameTwo = "testRetrieveProfileTwo";
        boolean logViewingEnabledTwo = false;
        boolean trayIconEnabledTwo = false;

        IAgentProfileDO profileTwo = createTestAgentProfile(agentProfileNameTwo, logViewingEnabledTwo, trayIconEnabledTwo);
        agentProfileDataSet.add(profileTwo);

        String agentProfileNameThree = "testRetrieveProfileThree";
        boolean logViewingEnabledThree = true;
        boolean trayIconEnabledThree = false;

        IAgentProfileDO profileThree = createTestAgentProfile(agentProfileNameThree, logViewingEnabledThree, trayIconEnabledThree);
        agentProfileDataSet.add(profileThree);

        AgentProfileQueryTermSet emptyQueryTermSet = new AgentProfileQueryTermSet();
        //  Test with an empty query term list to see if we get all of them back
        List profilesRetrieved = this.profileManagerToTest.retrieveAgentProfiles(emptyQueryTermSet, null, 5, null);
        assertEquals("Ensure full list of profiles is retrieved.", agentProfileDataSet, profilesRetrieved);

        // Test with a single query term
        // Name
        AgentProfileQueryTerm singleQueryTerm = new AgentProfileQueryTerm(AgentProfileQueryField.NAME, profileOne.getName());
        AgentProfileQueryTermSet agentProfileQueryTermSet = new AgentProfileQueryTermSet();
        agentProfileQueryTermSet.addQueryTerm(singleQueryTerm);
        profilesRetrieved = this.profileManagerToTest.retrieveAgentProfiles(agentProfileQueryTermSet, null, 5, null);
        assertEquals("Ensure one profile returned", 1, profilesRetrieved.size());
        assertEquals("Ensure correct one profile returned", profileOne, profilesRetrieved.get(0));

        // Test with two query terms
        // Name and dabsLocation
        AgentProfileQueryTerm secondQueryTerm = new AgentProfileQueryTerm(AgentProfileQueryField.TRAY_ICON_ENABLED, new Boolean(profileOne.isLogViewingEnabled()));
        agentProfileQueryTermSet.addQueryTerm(secondQueryTerm);
        profilesRetrieved = this.profileManagerToTest.retrieveAgentProfiles(agentProfileQueryTermSet, null, 5, null);
        assertEquals("Ensure one profile returned with two query terms", 1, profilesRetrieved.size());
        assertEquals("Ensure correct one profile returned with two query terms", profileOne, profilesRetrieved.get(0));

        secondQueryTerm = new AgentProfileQueryTerm(AgentProfileQueryField.LOG_VIEW_ENABLED, new Boolean(profileTwo.isLogViewingEnabled()));
        agentProfileQueryTermSet = new AgentProfileQueryTermSet();
        agentProfileQueryTermSet.addQueryTerm(singleQueryTerm);
        agentProfileQueryTermSet.addQueryTerm(secondQueryTerm);
        profilesRetrieved = this.profileManagerToTest.retrieveAgentProfiles(agentProfileQueryTermSet, null, 5, null);
        assertEquals("Ensure none returned with two query terms that conflict", 0, profilesRetrieved.size());

        //      Test paging - This test paging mechanism, sort criteria, and fetch
        // size
        // First page
        profilesRetrieved = this.profileManagerToTest.retrieveAgentProfiles(emptyQueryTermSet, null, 2, AgentProfileQueryField.NAME);
        assertEquals("Ensure two profiles returned in first page", 2, profilesRetrieved.size());
        assertEquals("Ensure correct profiles in first page One", this.profileManagerToTest.getDefaultAgentProfile(), profilesRetrieved.get(0));
        assertEquals("Ensure correct profiles in first page Two", profileOne, profilesRetrieved.get(1));

        // Second Page
        profilesRetrieved = this.profileManagerToTest.retrieveAgentProfiles(emptyQueryTermSet, profileOne, 2, AgentProfileQueryField.NAME);
        assertEquals("Ensure two profiles returned in second page", 2, profilesRetrieved.size());
        assertEquals("Ensure correct profile returned in second page One", profileThree, profilesRetrieved.get(0));
        assertEquals("Ensure correct profile returned in second page Two", profileTwo, profilesRetrieved.get(1));
    }

    public void testUpdateUserProfile() throws DataSourceException, ProfileNotFoundException {
        String userProfileName = "testUpdateProfile";

        IUserProfileDO profileCreated = createTestUserProfile(userProfileName);

        // Ensure we can retrieve the same user profile from DB
        IUserProfileDO profileToCompare = this.profileManagerToTest.retrieveUserProfileByName(userProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now change it
        String changedName = "testUpdateProfileChanged";
        profileCreated.setName(changedName);
        this.profileManagerToTest.updateUserProfile(profileCreated);

        // Ensure we can retrieve the same user profile from DB with changed
        // name
        profileToCompare = this.profileManagerToTest.retrieveUserProfileByName(changedName);
        assertEquals("Ensure name updated.", changedName, profileToCompare.getName());

        // Test with bad argument
        boolean exThrown = false;
        try {
            this.profileManagerToTest.updateUserProfile(null);
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException is profile is null", exThrown);
    }

    public void testUpdateCommProfile() throws Exception {
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        
        String commProfileName = "testUpdateProfile";
        URI dabsLocation = new URI("http://www.foo.com");
        int heartBeatFrequency = 10;
        IAgentType agentType = desktopAgentType;
        int logExpiration = 20;
        int logLimit = 30;
        int logFrequency = 40;
        boolean pushEnabled = true;
        int defaultPushPort = 2000;
		String password = "password";
        boolean secure = false;

        ICommProfileDO profileCreated = createTestCommProfile(commProfileName, dabsLocation, agentType, heartBeatFrequency, logExpiration, logLimit, logFrequency, pushEnabled, defaultPushPort, password, secure);

        // Ensure we can retrieve the same comm profile from DB
        ICommProfileDO profileToCompare = this.profileManagerToTest.retrieveCommProfileByName(commProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now change it
        String changedName = "testUpdateProfileChanged";
        profileCreated.setName(changedName);
        this.profileManagerToTest.updateCommProfile(profileCreated);

        // Ensure we can retrieve the same comm profile from DB
        profileToCompare = this.profileManagerToTest.retrieveCommProfileByName(changedName);
        assertEquals("Ensure value retrieved from database is equals to that updated.", profileCreated, profileToCompare);

        // Test password update
        String changedPassword= "testChangedPassword";
        profileCreated.setPassword(changedPassword);
        this.profileManagerToTest.updateCommProfile(profileCreated);
        profileToCompare = this.profileManagerToTest.retrieveCommProfileByName(changedName);
        assertTrue("Ensure password hash updated as a result of changed password.", Arrays.equals(CryptUtils.digest(changedPassword, "SHA1", 0), profileToCompare.getPasswordHash()));        
        
        // Test with bad argument
        boolean exThrown = false;
        try {
            this.profileManagerToTest.updateCommProfile(null);
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException is profile is null", exThrown);
    }

    public void testUpdateAgentProfile() throws DataSourceException, ProfileNotFoundException {
        String agentProfileName = "testCreateProfile";
        boolean logViewingEnabled = true;
        boolean trayIconEnabled = true;

        IAgentProfileDO profileCreated = createTestAgentProfile(agentProfileName, logViewingEnabled, trayIconEnabled);

        // Ensure we can retrieve the same agent profile from DB
        IAgentProfileDO profileToCompare = this.profileManagerToTest.retrieveAgentProfileByName(agentProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now change it
        String changedName = "testUpdateProfileChanged";
        profileCreated.setName(changedName);
        this.profileManagerToTest.updateAgentProfile(profileCreated);

        // Ensure we can retrieve the same agent profile from DB
        profileToCompare = this.profileManagerToTest.retrieveAgentProfileByName(changedName);
        assertEquals("Ensure value retrieved from database is equals to that updated.", profileCreated, profileToCompare);

        // Test with bad argument
        boolean exThrown = false;
        try {
            this.profileManagerToTest.updateAgentProfile(null);
        } catch (IllegalArgumentException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException is profile is null", exThrown);
    }

    public final void testGetDefaultCommProfile() throws DataSourceException {
        // Simply test if we can retrieve is and it's not null
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        ICommProfileDO defaultCommProfile = this.profileManagerToTest.getDefaultCommProfile(desktopAgentType);
        assertNotNull("Ensure default comm profile exists", defaultCommProfile);
    }

    public final void testGetDefaultAgentProfile() throws DataSourceException {
        // Simply test if we can retrieve is and it's not null
        IAgentProfileDO defaultAgentProfile = this.profileManagerToTest.getDefaultAgentProfile();
        assertNotNull("Ensure default agent profile exists", defaultAgentProfile);
    }

    public final void testDeleteUserProfile() throws DataSourceException, ProfileNotFoundException {
        String userProfileName = "testDeleteProfile";

        IUserProfileDO profileCreated = createTestUserProfile(userProfileName);

        // Ensure we can retrieve the same user profile from DB
        IUserProfileDO profileToCompare = this.profileManagerToTest.retrieveUserProfileByName(userProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now try to delete
        this.profileManagerToTest.deleteUserProfile(profileCreated.getId());

        // Ensure that we can't retrieve it now
        boolean exThrown = false;
        try {
            profileToCompare = this.profileManagerToTest.retrieveUserProfileByName(userProfileName);
        } catch (ProfileNotFoundException exception) {
            exThrown = true;
        }
        assertTrue("Should throw ProfileNotFoundException", exThrown);
    }

    public final void testDeleteCommProfile() throws URISyntaxException, DataSourceException, ProfileNotFoundException, PersistenceException, InvalidIDException {
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        
        String commProfileName = "testCreateProfile";
        URI dabsLocation = new URI("http://www.foo.com");
        IAgentType agentType = desktopAgentType;
        int heartBeatFrequency = 10;
        int logExpiration = 20;
        int logLimit = 30;
        int logFrequency = 40;
        boolean pushEnabled = true;
        int defaultPushPort = 2000;
		String password = "password";
        boolean secure = false;

        ICommProfileDO profileCreated = createTestCommProfile(commProfileName, dabsLocation, agentType, heartBeatFrequency, logExpiration, logLimit, logFrequency, pushEnabled, defaultPushPort, password, secure);

        // Ensure we can retrieve the same comm profile from DB
        ICommProfileDO profileToCompare = this.profileManagerToTest.retrieveCommProfileByName(commProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now try to delete
        this.profileManagerToTest.deleteCommProfile(profileCreated.getId());

        // Ensure that we can't retrieve it now
        boolean exThrown = false;
        try {
            profileToCompare = this.profileManagerToTest.retrieveCommProfileByName(commProfileName);
        } catch (ProfileNotFoundException exception) {
            exThrown = true;
        }
        assertTrue("Should throw ProfileNotFoundException", exThrown);
        
        // Create another profile.  This time, assign to agent and try to delete again
        profileCreated = createTestCommProfile(commProfileName, dabsLocation, agentType, heartBeatFrequency, logExpiration, logLimit, logFrequency, pushEnabled, defaultPushPort, password, secure);

        // Ensure we can retrieve the same comm profile from DB
        profileToCompare = this.profileManagerToTest.retrieveCommProfileByName(commProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created two.", profileCreated, profileToCompare);
                
        MockAgentRegistrationData mockAgentRegData = new MockAgentRegistrationData("A", desktopAgentType);
        IAgentStartupConfiguration agentConfig = this.agentManager.registerAgent(mockAgentRegData);
        this.agentManager.setCommProfile(agentConfig.getId(), profileCreated.getName());
        
        this.profileManagerToTest.deleteCommProfile(profileCreated.getId());
        // Ensure agent has defalt profile
        assertEquals("Ensure agent which had profile to delete is assigned to default profile", this.profileManagerToTest.getDefaultCommProfile(desktopAgentType), this.agentManager.getAgentById(agentConfig.getId()).getCommProfile());
        
        // Clean up
        this.agentManager.unregisterAgent(agentConfig.getId());
    }

    public final void testDeleteAgentProfile() throws ProfileNotFoundException, ProfileNotFoundException, DataSourceException {
        String agentProfileName = "testCreateProfile";
        boolean logViewingEnabled = true;
        boolean trayIconEnabled = true;

        IAgentProfileDO profileCreated = createTestAgentProfile(agentProfileName, logViewingEnabled, trayIconEnabled);

        // Ensure we can retrieve the same agent profile from DB
        IAgentProfileDO profileToCompare = this.profileManagerToTest.retrieveAgentProfileByName(agentProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now try to delete
        this.profileManagerToTest.deleteAgentProfile(profileCreated.getId());

        // Ensure that we can't retrieve it now
        boolean exThrown = false;
        try {
            profileToCompare = this.profileManagerToTest.retrieveAgentProfileByName(agentProfileName);
        } catch (ProfileNotFoundException exception) {
            exThrown = true;
        }
        assertTrue("Should throw ProfileNotFoundException", exThrown);
    }

    /**
     * @param agentProfileName
     * @param logViewingEnabled
     * @param trayIconEnabled
     * @return
     * @throws DataSourceException
     */
    private IAgentProfileDO createTestAgentProfile(String agentProfileName, boolean logViewingEnabled, boolean trayIconEnabled) throws DataSourceException {
        IAgentProfileData agentProfileData = new AgentProfileDataImpl(agentProfileName, logViewingEnabled, trayIconEnabled);
        IAgentProfileDO profileCreated = this.profileManagerToTest.createAgentProfile(agentProfileData);
        this.agentProfilesToClean.add(profileCreated);
        return profileCreated;
    }

    /**
     * @param userProfileName
     * @return
     * @throws DataSourceException
     */
    private IUserProfileDO createTestUserProfile(String userProfileName) throws DataSourceException {
        IUserProfileDO profileCreated = this.profileManagerToTest.createUserProfile(new UserProfileDataImpl(userProfileName));
        this.userProfilesToClean.add(profileCreated);
        return profileCreated;
    }

    /**
     * @param commProfileName
     * @param dabsLocation
     * @param heartBeatFrequency
     * @param logExpiration
     * @param logLimit
     * @param logFequency
     * @param agentPushParameters
     * @param secure
     * @return
     * @throws DataSourceException
     */
    private ICommProfileDO createTestCommProfile(String commProfileName, URI dabsLocation, IAgentType agentType, int heartBeatFrequency, int logExpiration, int logLimit, int logFrequency, boolean pushEnabled, int defaultPushPort, String password, boolean secure) throws DataSourceException {
        ICommProfileData commProfileData = new CommProfileDataImpl(commProfileName, dabsLocation, agentType, new TimeInterval(heartBeatFrequency, TimeInterval.TimeUnit.SECONDS), logExpiration, logLimit, new TimeInterval(logFrequency, TimeInterval.TimeUnit.SECONDS),
                pushEnabled, defaultPushPort, password, secure);
        ICommProfileDO profileCreated = this.profileManagerToTest.createCommProfile(commProfileData);
        this.commProfilesToClean.add(profileCreated);
        return profileCreated;
    }

    /**
     * @return
     */
    public static Test getTestSuite() {
        TestSuite suiteToReturn = new TestSuite("Profile Manager Test");
        suiteToReturn.addTest(new HibernateProfileManagerTest("testCreateUserProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testCreateCommProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testCreateAgentProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testUpdateUserProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testUpdateCommProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testUpdateAgentProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testGetDefaultCommProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testGetDefaultAgentProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveUserProfiles"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveCommProfiles"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveAgentProfiles"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveUserProfileByName"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveCommProfileByName"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveAgentProfileByName"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveUserProfileByID"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveCommProfileByID"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testRetrieveAgentProfileByID"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testDeleteUserProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testDeleteCommProfile"));
        suiteToReturn.addTest(new HibernateProfileManagerTest("testDeleteAgentProfile"));

        return suiteToReturn;
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

    private class UserProfileDataImpl extends BaseProfileDataImpl implements IUserProfileData {

        /**
         * Constructor
         * 
         * @param the
         *            name of the profile
         */
        public UserProfileDataImpl(String name) {
            super(name);
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
         * @see com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData#isLogViewingEnabled()
         */
        public boolean isLogViewingEnabled() {
            return this.isLogViewingEnabled;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData#isTrayIconEnabled()
         */
        public boolean isTrayIconEnabled() {
            return this.isTrayIconEnabled;
        }
    }

    private class CommProfileDataImpl extends BaseProfileDataImpl implements ICommProfileData {

        private URI dabsLocation;
        private IAgentType agentType;
        private TimeInterval heartBeatFrequency;
        private int logExpiration;
        private int logLimit;
        private TimeInterval logFequency;
        private boolean pushEnabled;
        private int defaultPushPort;
        private boolean secure;
		private String password;
		
        /**
         * Constructor
         * 
         * @param agentPushParameters
         * @param commProfileName
         * @param dabsLocation
         * @param heartBeatFrequency
         * @param logExpiration
         * @param logFrequency
         * @param logLimit
         * @param secure
         */
        public CommProfileDataImpl(String commProfileName, URI dabsLocation, IAgentType agentType, TimeInterval heartBeatFrequency, int logExpiration, int logLimit, TimeInterval logFrequency, boolean pushEnabled, int defaultPushPort, String password, boolean secure) {
            super(commProfileName);

            this.dabsLocation = dabsLocation;
            this.agentType = agentType;
            this.heartBeatFrequency = heartBeatFrequency;
            this.logExpiration = logExpiration;
            this.logLimit = logLimit;
            this.logFequency = logFrequency;
            this.pushEnabled = pushEnabled;
            this.defaultPushPort = defaultPushPort;
            this.secure = secure;
			this.password = password;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getDABSLocation()
         */
        public URI getDABSLocation() {
            return this.dabsLocation;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getHeartBeatFrequency()
         */
        public TimeInterval getHeartBeatFrequency() {
            return this.heartBeatFrequency;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getLogExpiration()
         */
        public int getLogExpiration() {
            return this.logExpiration;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getLogLimit()
         */
        public int getLogLimit() {
            return this.logLimit;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getLogFrequency()
         */
        public TimeInterval getLogFrequency() {
            return this.logFequency;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#isSecure()
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
         * @return the agentType.
         */
        public IAgentType getAgentType() {
            return this.agentType;
        }

		public String getPassword() {
			return this.password;
		}
    }
}
