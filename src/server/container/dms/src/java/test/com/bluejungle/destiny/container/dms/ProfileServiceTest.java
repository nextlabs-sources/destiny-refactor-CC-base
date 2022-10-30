/*
 * Created on Jan 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis.types.Token;
import org.apache.axis.types.UnsignedShort;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.framework.types.CommitFault;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.TimeIntervalDTO;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.framework.types.UniqueConstraintViolationFault;
import com.bluejungle.destiny.services.management.AgentProfileDTOQuery;
import com.bluejungle.destiny.services.management.CommProfileDTOQuery;
import com.bluejungle.destiny.services.management.ProfileServiceIF;
import com.bluejungle.destiny.services.management.ProfileServiceLocator;
import com.bluejungle.destiny.services.management.UserProfileDTOQuery;
import com.bluejungle.destiny.services.management.types.ActivityJournalingSettingsDTO;
import com.bluejungle.destiny.services.management.types.ActivityJournalingSettingsDTOList;
import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.AgentProfileDTOList;
import com.bluejungle.destiny.services.management.types.AgentProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.AgentProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.AgentProfileDTOQueryTermSet;
import com.bluejungle.destiny.services.management.types.AgentProfileInfo;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTOList;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTermSet;
import com.bluejungle.destiny.services.management.types.CommProfileInfo;
import com.bluejungle.destiny.services.management.types.UserProfileDTO;
import com.bluejungle.destiny.services.management.types.UserProfileDTOList;
import com.bluejungle.destiny.services.management.types.UserProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.UserProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.UserProfileDTOQueryTermSet;
import com.bluejungle.destiny.services.management.types.UserProfileInfo;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.domain.types.AgentTypeDTO;
import com.bluejungle.framework.utils.CryptUtils;

/**
 * Unit test to verify function of Profile Web Service
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/tests/ProfileServiceTest.java#1 $
 */
public class ProfileServiceTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(ProfileServiceTest.class.getName());

    private static final int MAX_NAME_LENGTH = 128;

    private ProfileServiceIF profileServiceToTest;

    private List userProfilesToClean = new LinkedList();
    private List commProfilesToClean = new LinkedList();
    private List agentProfilesToClean = new LinkedList();

    /**
     * Constructor for ProfileServiceTest.
     * 
     * @param testName
     */
    public ProfileServiceTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ProfileServiceTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws ServiceException {
        ProfileServiceLocator profileServiceLocator = new ProfileServiceLocator();
        profileServiceLocator.setProfileServiceIFPortEndpointAddress("http://localhost:8081/dms/services/ProfileServiceIFPort");
        this.profileServiceToTest = profileServiceLocator.getProfileServiceIFPort();
        assertNotNull("ProfileService created properly", this.profileServiceToTest);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();

        Iterator userProfilesToCleanIterator = userProfilesToClean.iterator();
        while (userProfilesToCleanIterator.hasNext()) {
            try {
                UserProfileDTO nextUserProfile = (UserProfileDTO) userProfilesToCleanIterator.next();
                profileServiceToTest.removeUserProfile(nextUserProfile.getId());
            } catch (RemoteException exception) {
                LOG.warn("Failed to clean up created user profile", exception);
            }
        }

        Iterator commProfilesToCleanIterator = commProfilesToClean.iterator();
        while (commProfilesToCleanIterator.hasNext()) {
            try {
                CommProfileDTO nextCommProfile = (CommProfileDTO)commProfilesToCleanIterator.next();
                profileServiceToTest.removeCommProfile(nextCommProfile.getId());
            } catch (RemoteException exception) {
                LOG.warn("Failed to clean up created comm profile", exception);
            }
        }

        Iterator agentProfilesToCleanIterator = agentProfilesToClean.iterator();
        while (agentProfilesToCleanIterator.hasNext()) {
            try {
                AgentProfileDTO nextAgentProfile = (AgentProfileDTO)agentProfilesToCleanIterator.next();
                profileServiceToTest.removeAgentProfile(nextAgentProfile.getId());
            } catch (RemoteException exception) {
                LOG.warn("Failed to clean up created agent profile", exception);
            }
        }
    }

    public void testAddUserProfile() throws RemoteException {
        String userProfileInfoName = "testAddUserProfileInfo";

        UserProfileInfo userProfileInfoToAdd = new UserProfileInfo();
        userProfileInfoToAdd.setName(new Token(userProfileInfoName));

        UserProfileDTO profileCreated = profileServiceToTest.addUserProfile(userProfileInfoToAdd);
        userProfilesToClean.add(profileCreated);
        assertNotNull("Ensure created user profile is not null.", profileCreated);
        assertNotNull("Ensure created date is not null", profileCreated.getCreatedDate());
        assertNotNull("Ensure modified date is not null", profileCreated.getModifiedDate());
        assertEquals("Ensure user profile name is as provided", userProfileInfoName, profileCreated.getName().toString());

        // Ensure we can retrieve the same user profile from DB
        UserProfileDTO profileToCompare = retrieveUserProfileDTOByName(userProfileInfoName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Test with an existent name and ensure that the proper exception is thrown
        UniqueConstraintViolationFault expectedFault = null;
        try
        {
            profileCreated = profileServiceToTest.addUserProfile(userProfileInfoToAdd);
            userProfilesToClean.add(profileCreated);
        }
        catch (UniqueConstraintViolationFault exception) {
            expectedFault = exception;            
        }        
        assertNotNull("testAddUserProfile - Ensure Unique Constraint Violation Fault was thrown.", expectedFault);

        boolean exThrown = false;
        try {
            StringBuffer nameBuffer = new StringBuffer();
            for (int i = 0; i < MAX_NAME_LENGTH + 1; i++) {
                nameBuffer.append("s");
            }

            userProfileInfoToAdd.setName(new Token(nameBuffer.toString()));
            profileCreated = profileServiceToTest.addUserProfile(userProfileInfoToAdd);
            profileServiceToTest.removeUserProfile(profileCreated.getId());

        } catch (RemoteException exception) {
            exThrown = true;
        }
        assertTrue("Should throw IllegalArgumentException is name is too long", exThrown);
    }

    public void testAddCommProfile() throws CommitFault, UnauthorizedCallerFault, RemoteException, URISyntaxException, MalformedURIException, NoSuchAlgorithmException {
        String commProfileInfoName = "testAddCommProfileInfo";
        URI dabsLocation = new URI("http://www.foo.com");
        AgentTypeDTO agentType = AgentTypeDTO.DESKTOP;
        int heartBeatFrequency = 10;        
        int logLimit = 30;
        int logFrequency = 40;
        boolean pushEnabled = true;
        short defaultPort = 80;
        boolean secure = false;
        String password = "fooPassword";
        String activityJournalingName = "Extended";

        CommProfileInfo commProfileInfoToAdd = new CommProfileInfo();
        commProfileInfoToAdd.setName(new Token(commProfileInfoName));
        commProfileInfoToAdd.setDABSLocation(new org.apache.axis.types.URI(dabsLocation.toString()));
        commProfileInfoToAdd.setAgentType(agentType);
        commProfileInfoToAdd.setHeartBeatFrequency(new TimeIntervalDTO(new UnsignedShort(heartBeatFrequency), TimeUnits.fromString("seconds")));
        commProfileInfoToAdd.setLogLimit(new UnsignedShort(logLimit));
        commProfileInfoToAdd.setLogFrequency(new TimeIntervalDTO(new UnsignedShort(logFrequency), TimeUnits.fromString("seconds")));
        commProfileInfoToAdd.setPushEnabled(new Boolean(pushEnabled));
        commProfileInfoToAdd.setDefaultPushPort(new UnsignedShort(defaultPort));        
        commProfileInfoToAdd.setPassword(password);
        commProfileInfoToAdd.setAssignedActivityJournalingName(activityJournalingName);

        CommProfileDTO profileCreated = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
        commProfilesToClean.add(profileCreated);
        assertNotNull("Ensure created comm profile is not null.", profileCreated);
        assertNotNull("Ensure created date is not null", profileCreated.getCreatedDate());
        assertNotNull("Ensure modified date is not null", profileCreated.getModifiedDate());
        assertEquals("Ensure comm profile name is as provided", commProfileInfoName, profileCreated.getName().toString());
        assertEquals("Ensure DABS Location is as provided", dabsLocation.toString(), profileCreated.getDABSLocation().toString());
        assertEquals("Ensure Agent type is as provided", agentType, profileCreated.getAgentType());
        assertEquals("Ensure heart beat rate is as provided", heartBeatFrequency, profileCreated.getHeartBeatFrequency().getTime().intValue());
        assertEquals("Ensure log limit is as provided", logLimit, profileCreated.getLogLimit().intValue());
        assertEquals("Ensure log limit is as provided", logFrequency, profileCreated.getLogFrequency().getTime().intValue());
        assertEquals("Ensure push enabled is as provided", pushEnabled, profileCreated.isPushEnabled());
        assertEquals("Ensure default push port is as provided", defaultPort, profileCreated.getDefaultPushPort().shortValue());
        assertTrue("Ensure password hash is as expected for provided password", Arrays.equals(CryptUtils.digest(password, "SHA1", 0), profileCreated.getPasswordHash()));
        
        // Ensure we can retrieve the same comm profile from DB
        CommProfileDTO profileToCompare = retrieveCommProfileDTOByName(commProfileInfoName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated.getId(), profileToCompare.getId());

        // Test with an existent name and ensure that the proper exception is thrown
        UniqueConstraintViolationFault expectedFault = null;
        try
        {
            profileCreated = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
            commProfilesToClean.add(profileCreated);
        }
        catch (UniqueConstraintViolationFault exception) {
            expectedFault = exception;            
        }        
        assertNotNull("testAddUserProfile - Ensure Unique Constraint Violation Fault was thrown.", expectedFault);

        boolean exThrown = false;
        try {
            StringBuffer nameBuffer = new StringBuffer();
            for (int i = 0; i < MAX_NAME_LENGTH + 1; i++) {
                nameBuffer.append("s");
            }

            commProfileInfoToAdd.setName(new Token(nameBuffer.toString()));
            profileCreated = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
            profileServiceToTest.removeCommProfile(profileCreated.getId());
        } catch (RemoteException exception) {
            exThrown = true;
        }
        assertTrue("Should throw RemoteException is name is too long", exThrown);

        //Test with bad DABSLocation
        exThrown = false;
        try {
            commProfileInfoToAdd.setName(new Token(commProfileInfoName));
            commProfileInfoToAdd.setDABSLocation(null);
            profileCreated = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
            profileServiceToTest.removeCommProfile(profileCreated.getId());
        } catch (RemoteException exception) {
            exThrown = true;
        }
        assertTrue("Should throw RemoteException if DABSLocation is null", exThrown);

        // Test with bad default push port 
        exThrown = false;
        try {
            commProfileInfoToAdd.setName(new Token(commProfileInfoName));
            commProfileInfoToAdd.setDABSLocation(new org.apache.axis.types.URI(dabsLocation.toString()));
            commProfileInfoToAdd.setDefaultPushPort(null);
            profileCreated = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
            profileServiceToTest.removeCommProfile(profileCreated.getId());
        } catch (RemoteException exception) {
            exThrown = true;
        }
        assertTrue("Should throw RemoteException if default push port is null", exThrown);
    }

    public void testAddAgentProfile() throws CommitFault, UnauthorizedCallerFault, RemoteException {
        String agentProfileInfoName = "testAddAgentProfileInfo";
        boolean logViewingEnabled = true;
        boolean trayIconEnabled = true;

        AgentProfileInfo agentProfileInfoToAdd = new AgentProfileInfo();
        agentProfileInfoToAdd.setName(new Token(agentProfileInfoName));
        agentProfileInfoToAdd.setLogViewingEnabled(logViewingEnabled);
        agentProfileInfoToAdd.setTrayIconEnabled(trayIconEnabled);

        AgentProfileDTO profileCreated = profileServiceToTest.addAgentProfile(agentProfileInfoToAdd);
        agentProfilesToClean.add(profileCreated);
        assertNotNull("Ensure created agent profile is not null.", profileCreated);
        assertNotNull("Ensure created date is not null", profileCreated.getCreatedDate());
        assertNotNull("Ensure modified date is not null", profileCreated.getModifiedDate());
        assertEquals("Ensure agent profile name is as provided", agentProfileInfoName, profileCreated.getName().toString());
        assertEquals("Ensure logViewingEnabled is as provided", logViewingEnabled, profileCreated.isLogViewingEnabled());
        assertEquals("Ensure trayIconEnabled is as provided", trayIconEnabled, profileCreated.isTrayIconEnabled());

        // Ensure we can retrieve the same agent profile from DB
        AgentProfileDTO profileToCompare = retrieveAgentProfileDTOByName(agentProfileInfoName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Test with an existent name and ensure that the proper exception is thrown
        UniqueConstraintViolationFault expectedFault = null;
        try
        {     
            profileCreated = profileServiceToTest.addAgentProfile(agentProfileInfoToAdd);
            agentProfilesToClean.add(profileCreated);
        }
        catch (UniqueConstraintViolationFault exception) {
            expectedFault = exception;            
        }        
        assertNotNull("testAddUserProfile - Ensure Unique Constraint Violation Fault was thrown.", expectedFault);
        
        boolean exThrown = false;
        try {
            StringBuffer nameBuffer = new StringBuffer();
            for (int i = 0; i < MAX_NAME_LENGTH + 1; i++) {
                nameBuffer.append("s");
            }

            agentProfileInfoToAdd.setName(new Token(nameBuffer.toString()));
            profileCreated = profileServiceToTest.addAgentProfile(agentProfileInfoToAdd);
            profileServiceToTest.removeAgentProfile(profileCreated.getId());
        } catch (RemoteException exception) {
            exThrown = true;
        }
        assertTrue("Should throw RemoteException is name is too long", exThrown);
    }

    public void testUpdateUserProfile() throws CommitFault, UnauthorizedCallerFault, RemoteException {
        String userProfileInfoName = "testUpdateProfile";

        UserProfileInfo userProfileInfoToAdd = new UserProfileInfo();
        userProfileInfoToAdd.setName(new Token(userProfileInfoName));

        UserProfileDTO profileCreated = profileServiceToTest.addUserProfile(userProfileInfoToAdd);
        userProfilesToClean.add(profileCreated);

        // Ensure we can retrieve the same user profile from DB
        UserProfileDTO profileToCompare = retrieveUserProfileDTOByName(userProfileInfoName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now change it
        String changedName = "testUpdateProfileChanged";
        profileCreated.setName(new Token(changedName));
        profileServiceToTest.updateUserProfile(profileCreated);

        // Ensure we can retrieve the same user profile from DB
        profileToCompare = retrieveUserProfileDTOByName(changedName);
        assertEquals("Ensure name has changed.", changedName, profileToCompare.getName().toString());
    }

    public void testUpdateAgentProfile() throws CommitFault, UnauthorizedCallerFault, RemoteException {
        String agentProfileInfoName = "testUpdateProfile";

        AgentProfileInfo agentProfileInfoToAdd = new AgentProfileInfo();
        agentProfileInfoToAdd.setName(new Token(agentProfileInfoName));
        agentProfileInfoToAdd.setLogViewingEnabled(true);

        AgentProfileDTO profileCreated = profileServiceToTest.addAgentProfile(agentProfileInfoToAdd);
        agentProfilesToClean.add(profileCreated);

        // Ensure we can retrieve the same agent profile from DB
        AgentProfileDTO profileToCompare = retrieveAgentProfileDTOByName(agentProfileInfoName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now change it
        String changedName = "testUpdateProfileChanged";
        profileCreated.setName(new Token(changedName));
        profileServiceToTest.updateAgentProfile(profileCreated);

        // Ensure we can retrieve the same agent profile from DB
        profileToCompare = retrieveAgentProfileDTOByName(changedName);
        assertEquals("Ensure name has changed.", changedName, profileToCompare.getName().toString());
    }

    public void testUpdateCommProfile() throws CommitFault, UnauthorizedCallerFault, RemoteException, MalformedURIException, NoSuchAlgorithmException {
        String commProfileInfoName = "testUpdateProfile";

        CommProfileInfo commProfileInfoToAdd = new CommProfileInfo();
        commProfileInfoToAdd.setName(new Token(commProfileInfoName));
        commProfileInfoToAdd.setDABSLocation(new org.apache.axis.types.URI("http://www.foo.com"));
        commProfileInfoToAdd.setAgentType(AgentTypeDTO.DESKTOP);
        commProfileInfoToAdd.setHeartBeatFrequency(new TimeIntervalDTO(new UnsignedShort(10), TimeUnits.fromString("seconds")));
        commProfileInfoToAdd.setLogLimit(new UnsignedShort(30));
        commProfileInfoToAdd.setLogFrequency(new TimeIntervalDTO(new UnsignedShort(40), TimeUnits.fromString("seconds")));
        commProfileInfoToAdd.setPushEnabled(Boolean.FALSE);
        commProfileInfoToAdd.setDefaultPushPort(new UnsignedShort(9));
        commProfileInfoToAdd.setAssignedActivityJournalingName("Default");
        commProfileInfoToAdd.setPassword("password");

        CommProfileDTO profileCreated = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
        commProfilesToClean.add(profileCreated);

        // Ensure we can retrieve the same comm profile from DB
        CommProfileDTO profileToCompare = retrieveCommProfileDTOByName(commProfileInfoName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated.getId(), profileToCompare.getId());

        // Now change it
        String changedName = "testUpdateProfileChanged";
        profileCreated.setName(new Token(changedName));
        profileServiceToTest.updateCommProfile(profileCreated);

        // Ensure we can retrieve the same comm profile from DB
        profileToCompare = retrieveCommProfileDTOByName(changedName);
        assertEquals("Ensure name has changed.", changedName, profileToCompare.getName().toString());
        
        // Try updating the password
        String changedPassword = "changedPassword";
        profileToCompare.setPassword(changedPassword);
        profileServiceToTest.updateCommProfile(profileToCompare);

        profileToCompare = retrieveCommProfileDTOByName(changedName);
        assertTrue("Ensure password hash has changed.", Arrays.equals(CryptUtils.digest(changedPassword, "SHA1", 0), profileToCompare.getPasswordHash()));
        
    }

    public void testGetUserProfiles() throws CommitFault, UnauthorizedCallerFault, RemoteException {
        UserProfileDTO[] userProfileDataSet = new UserProfileDTO[3];

        UserProfileInfo userProfileInfoToAdd = new UserProfileInfo();

        userProfileInfoToAdd.setName(new Token("testRetrieveProfileOne"));
        UserProfileDTO profileCreatedOne = profileServiceToTest.addUserProfile(userProfileInfoToAdd);
        userProfileDataSet[0] = profileCreatedOne;
        userProfilesToClean.add(profileCreatedOne);

        userProfileInfoToAdd.setName(new Token("testRetrieveProfileTwo"));
        UserProfileDTO profileCreatedTwo = profileServiceToTest.addUserProfile(userProfileInfoToAdd);
        userProfileDataSet[1] = profileCreatedTwo;
        userProfilesToClean.add(profileCreatedTwo);

        userProfileInfoToAdd.setName(new Token("testRetrieveProfileThree"));
        UserProfileDTO profileCreatedThree = profileServiceToTest.addUserProfile(userProfileInfoToAdd);
        userProfileDataSet[2] = profileCreatedThree;
        userProfilesToClean.add(profileCreatedThree);

        UserProfileDTOQueryTermSet emptyQueryTermSet = new UserProfileDTOQueryTermSet();
        UserProfileDTOQuery emptyUserProfileDTOQuery = new UserProfileDTOQuery();
        emptyUserProfileDTOQuery.setUserProfileDTOQueryTermSet(emptyQueryTermSet);
        emptyUserProfileDTOQuery.setFetchSize(5);

        //  Test with an empty query term list to see if we get all of them back
        UserProfileDTOList profilesRetrieved = profileServiceToTest.getUserProfiles(emptyUserProfileDTOQuery);
        assertTrue("Ensure full list of profiles is retrieved.", Arrays.equals(userProfileDataSet, profilesRetrieved.getUserProfileDTO()));

        //      Test with a single query term
        UserProfileDTOQueryTerm[] queryTerms = { new UserProfileDTOQueryTerm(new UserProfileDTOQueryField("name"), profileCreatedOne.getName().toString()) };
        UserProfileDTOQueryTermSet singleTermQueryTermSet = new UserProfileDTOQueryTermSet();
        singleTermQueryTermSet.setUserProfileDTOQueryTerm(queryTerms);
        UserProfileDTOQuery singleTermDTOQuery = new UserProfileDTOQuery();
        singleTermDTOQuery.setUserProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(5);

        profilesRetrieved = profileServiceToTest.getUserProfiles(singleTermDTOQuery);
        UserProfileDTO[] profilesRetrievedArray = profilesRetrieved.getUserProfileDTO();
        assertEquals("Ensure one profile returned", 1, profilesRetrievedArray.length);
        assertEquals("Ensure correct one profile returned", profileCreatedOne, profilesRetrievedArray[0]);

        // Test paging - This test paging mechanism, sort criteria, and fetch
        // size
        // First page
        emptyUserProfileDTOQuery.setFetchSize(2);
        emptyUserProfileDTOQuery.setSortField(new UserProfileDTOQueryField("name"));
        profilesRetrieved = profileServiceToTest.getUserProfiles(emptyUserProfileDTOQuery);
        profilesRetrievedArray = profilesRetrieved.getUserProfileDTO();
        assertEquals("Ensure two profiles returned in first page", 2, profilesRetrievedArray.length);
        assertEquals("Ensure correct profiles in first page One", profileCreatedOne, profilesRetrievedArray[0]);
        assertEquals("Ensure correct profiles in first page One", profileCreatedThree, profilesRetrievedArray[1]);

        // Second Page
        emptyUserProfileDTOQuery.setFromResult(profilesRetrievedArray[1]);
        profilesRetrieved = profileServiceToTest.getUserProfiles(emptyUserProfileDTOQuery);
        profilesRetrievedArray = profilesRetrieved.getUserProfileDTO();
        assertEquals("Ensure one profile returned in second page", 1, profilesRetrievedArray.length);
        assertEquals("Ensure last profile returned in second page", profileCreatedTwo, profilesRetrievedArray[0]);
    }

    public void testGetAgentProfiles() throws CommitFault, UnauthorizedCallerFault, RemoteException {
        AgentProfileDTO[] agentProfileDataSet = new AgentProfileDTO[3];

        AgentProfileInfo agentProfileInfoToAdd = new AgentProfileInfo();
        agentProfileInfoToAdd.setLogViewingEnabled(true);
        agentProfileInfoToAdd.setTrayIconEnabled(false);

        agentProfileInfoToAdd.setName(new Token("testRetrieveProfileOne"));
        AgentProfileDTO profileCreatedOne = profileServiceToTest.addAgentProfile(agentProfileInfoToAdd);
        agentProfileDataSet[0] = profileCreatedOne;
        agentProfilesToClean.add(profileCreatedOne);

        agentProfileInfoToAdd.setName(new Token("testRetrieveProfileTwo"));
        AgentProfileDTO profileCreatedTwo = profileServiceToTest.addAgentProfile(agentProfileInfoToAdd);
        agentProfileDataSet[1] = profileCreatedTwo;
        agentProfilesToClean.add(profileCreatedTwo);

        agentProfileInfoToAdd.setName(new Token("testRetrieveProfileThree"));
        AgentProfileDTO profileCreatedThree = profileServiceToTest.addAgentProfile(agentProfileInfoToAdd);
        agentProfileDataSet[2] = profileCreatedThree;
        agentProfilesToClean.add(profileCreatedThree);

        AgentProfileDTOQueryTermSet emptyQueryTermSet = new AgentProfileDTOQueryTermSet();
        AgentProfileDTOQuery emptyAgentProfileDTOQuery = new AgentProfileDTOQuery();
        emptyAgentProfileDTOQuery.setAgentProfileDTOQueryTermSet(emptyQueryTermSet);
        emptyAgentProfileDTOQuery.setFetchSize(5);

        //  Test with an empty query term list to see if we get all of them back
        AgentProfileDTOList profilesRetrieved = profileServiceToTest.getAgentProfiles(emptyAgentProfileDTOQuery);
        List profilesRetrievedList = Arrays.asList(profilesRetrieved.getAgentProfileDTO());

        // The list should contain the profiles created and the default profile
        assertTrue("Ensure list contains all profiles add.", profilesRetrievedList.contains(profileCreatedOne) && profilesRetrievedList.contains(profileCreatedTwo) && profilesRetrievedList.contains(profileCreatedTwo));
        assertEquals("Ensure full list is correct size.", 4, profilesRetrievedList.size());

        //      Test with a single query term
        AgentProfileDTOQueryTerm[] queryTerms = { new AgentProfileDTOQueryTerm(new AgentProfileDTOQueryField("name"), profileCreatedOne.getName().toString()) };
        AgentProfileDTOQueryTermSet singleTermQueryTermSet = new AgentProfileDTOQueryTermSet();
        singleTermQueryTermSet.setAgentProfileDTOQueryTerm(queryTerms);
        AgentProfileDTOQuery singleTermDTOQuery = new AgentProfileDTOQuery();
        singleTermDTOQuery.setAgentProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(5);

        profilesRetrieved = profileServiceToTest.getAgentProfiles(singleTermDTOQuery);
        AgentProfileDTO[] profilesRetrievedArray = profilesRetrieved.getAgentProfileDTO();
        assertEquals("Ensure one profile returned", 1, profilesRetrievedArray.length);
        assertEquals("Ensure correct one profile returned", profileCreatedOne, profilesRetrievedArray[0]);

        // Test paging - This test paging mechanism, sort criteria, and fetch
        // size
        // First page
        emptyAgentProfileDTOQuery.setFetchSize(2);
        emptyAgentProfileDTOQuery.setSortField(new AgentProfileDTOQueryField("name"));
        profilesRetrieved = profileServiceToTest.getAgentProfiles(emptyAgentProfileDTOQuery);
        profilesRetrievedArray = profilesRetrieved.getAgentProfileDTO();
        assertEquals("Ensure two profiles returned in first page", 2, profilesRetrievedArray.length);
        // The first element is the default profile. The second is
        // profileCreatedOne
        assertEquals("Ensure correct profiles in first page", profileCreatedOne, profilesRetrievedArray[1]);

        // Second Page
        emptyAgentProfileDTOQuery.setFromResult(profilesRetrievedArray[1]);
        profilesRetrieved = profileServiceToTest.getAgentProfiles(emptyAgentProfileDTOQuery);
        profilesRetrievedArray = profilesRetrieved.getAgentProfileDTO();
        assertEquals("Ensure one profile returned in second page", 2, profilesRetrievedArray.length);
        assertEquals("Ensure last profile returned in second page 1", profileCreatedThree, profilesRetrievedArray[0]);
        assertEquals("Ensure last profile returned in second page 2", profileCreatedTwo, profilesRetrievedArray[1]);
    }

    public void testGetCommProfiles() throws CommitFault, UnauthorizedCallerFault, RemoteException, MalformedURIException {
        CommProfileDTO[] commProfileDataSet = new CommProfileDTO[3];

        CommProfileInfo commProfileInfoToAdd = new CommProfileInfo();
        commProfileInfoToAdd.setDABSLocation(new org.apache.axis.types.URI("http://www.foo.com"));
        commProfileInfoToAdd.setAgentType(AgentTypeDTO.DESKTOP);
        commProfileInfoToAdd.setHeartBeatFrequency(new TimeIntervalDTO(new UnsignedShort(10), TimeUnits.fromString("seconds")));
        commProfileInfoToAdd.setLogLimit(new UnsignedShort(30));
        commProfileInfoToAdd.setLogFrequency(new TimeIntervalDTO(new UnsignedShort(40), TimeUnits.fromString("seconds")));
        commProfileInfoToAdd.setPushEnabled(Boolean.FALSE);
        commProfileInfoToAdd.setDefaultPushPort(new UnsignedShort(9));
        commProfileInfoToAdd.setAssignedActivityJournalingName("Default");
        commProfileInfoToAdd.setPassword("password");
        
        commProfileInfoToAdd.setName(new Token("testRetrieveProfileOne"));
        CommProfileDTO profileCreatedOne = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
        commProfileDataSet[0] = profileCreatedOne;
        commProfilesToClean.add(profileCreatedOne);

        commProfileInfoToAdd.setName(new Token("testRetrieveProfileTwo"));
        CommProfileDTO profileCreatedTwo = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
        commProfileDataSet[1] = profileCreatedTwo;
        commProfilesToClean.add(profileCreatedTwo);

        commProfileInfoToAdd.setName(new Token("testRetrieveProfileThree"));
        CommProfileDTO profileCreatedThree = profileServiceToTest.addCommProfile(commProfileInfoToAdd);
        commProfileDataSet[2] = profileCreatedThree;
        commProfilesToClean.add(profileCreatedThree);

        CommProfileDTOQueryTermSet emptyQueryTermSet = new CommProfileDTOQueryTermSet();
        CommProfileDTOQuery emptyCommProfileDTOQuery = new CommProfileDTOQuery();
        emptyCommProfileDTOQuery.setCommProfileDTOQueryTermSet(emptyQueryTermSet);
        emptyCommProfileDTOQuery.setFetchSize(6);

        //  Test with an empty query term list to see if we get all of them back
        CommProfileDTOList profilesRetrieved = profileServiceToTest.getCommProfiles(emptyCommProfileDTOQuery);
        List profilesRetrievedList = Arrays.asList(profilesRetrieved.getCommProfileDTO());

        // The list should contain the profiles created and the default profile
        assertEquals("Ensure full list is correct size.", 6, profilesRetrievedList.size());
        long elementOneId = ((CommProfileDTO)profilesRetrievedList.get(0)).getId();
        long elementTwoId = ((CommProfileDTO)profilesRetrievedList.get(1)).getId();
        long elementThreeId = ((CommProfileDTO)profilesRetrievedList.get(2)).getId();
        long elementFourId = ((CommProfileDTO)profilesRetrievedList.get(3)).getId();
        long elementFiveId = ((CommProfileDTO)profilesRetrievedList.get(4)).getId();
        long elementSixId = ((CommProfileDTO)profilesRetrievedList.get(5)).getId();
        assertTrue("Ensure list contains all profiles added 1.", (elementOneId == profileCreatedOne.getId()) || (elementTwoId == profileCreatedOne.getId()) || (elementThreeId == profileCreatedOne.getId()) || (elementFourId == profileCreatedOne.getId()) || (elementFiveId == profileCreatedOne.getId()) || (elementSixId == profileCreatedOne.getId()));
        assertTrue("Ensure list contains all profiles added 2.", (elementOneId == profileCreatedTwo.getId()) || (elementTwoId == profileCreatedTwo.getId()) || (elementThreeId == profileCreatedTwo.getId()) || (elementFourId == profileCreatedTwo.getId()) || (elementFiveId == profileCreatedTwo.getId()) || (elementSixId == profileCreatedTwo.getId()));
        assertTrue("Ensure list contains all profiles added 3.", (elementOneId == profileCreatedThree.getId()) || (elementTwoId == profileCreatedThree.getId()) || (elementThreeId == profileCreatedThree.getId()) || (elementFourId == profileCreatedThree.getId()) || (elementFiveId == profileCreatedThree.getId()) || (elementSixId == profileCreatedThree.getId()));
        

        //      Test with a single query term
        CommProfileDTOQueryTerm[] queryTerms = { new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, profileCreatedOne.getName().toString()) };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(5);

        profilesRetrieved = profileServiceToTest.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO[] profilesRetrievedArray = profilesRetrieved.getCommProfileDTO();
        assertEquals("Ensure one profile returned", 1, profilesRetrievedArray.length);
        assertEquals("Ensure correct one profile returned", profileCreatedOne.getId(), profilesRetrievedArray[0].getId());

        // Test paging - This test paging mechanism, sort criteria, and fetch
        // size
        // First page
        emptyCommProfileDTOQuery.setFetchSize(4);
        emptyCommProfileDTOQuery.setSortField(CommProfileDTOQueryField.name);
        profilesRetrieved = profileServiceToTest.getCommProfiles(emptyCommProfileDTOQuery);
        profilesRetrievedArray = profilesRetrieved.getCommProfileDTO();
        assertEquals("Ensure four profiles returned in first page", 4, profilesRetrievedArray.length);
        // The first two elements are the default profiles. The third is
        // profileCreatedOne
        assertEquals("Ensure correct profiles in first page", profileCreatedOne.getId(), profilesRetrievedArray[3].getId());

        // Second Page
        emptyCommProfileDTOQuery.setFromResult(profilesRetrievedArray[3]);
        profilesRetrieved = profileServiceToTest.getCommProfiles(emptyCommProfileDTOQuery);
        profilesRetrievedArray = profilesRetrieved.getCommProfileDTO();
        assertEquals("Ensure one profile returned in second page", 2, profilesRetrievedArray.length);
        assertEquals("Ensure last profile returned in second page 1", profileCreatedThree.getId(), profilesRetrievedArray[0].getId());
        assertEquals("Ensure last profile returned in second page 2", profileCreatedTwo.getId(), profilesRetrievedArray[1].getId());
    }

    public void testRemoveUserProfile() throws CommitFault, UnauthorizedCallerFault, RemoteException {
        String userProfileName = "testDeleteProfile";

        UserProfileInfo userProfileInfoToAdd = new UserProfileInfo();
        userProfileInfoToAdd.setName(new Token(userProfileName));

        UserProfileDTO profileCreated = profileServiceToTest.addUserProfile(userProfileInfoToAdd);

        // Ensure we can retrieve the same user profile from DB
        UserProfileDTO profileToCompare = retrieveUserProfileDTOByName(userProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now try to delete
        profileServiceToTest.removeUserProfile(profileCreated.getId());

        // Ensure that we can't retrieve it now
        assertNull("Ensure user profile was deleted", retrieveUserProfileDTOByName(userProfileName));
    }

    public void testRemoveAgentProfile() throws CommitFault, UnauthorizedCallerFault, RemoteException {
        String agentProfileName = "testDeleteProfile";

        AgentProfileInfo agentProfileInfoToAdd = new AgentProfileInfo();
        agentProfileInfoToAdd.setName(new Token(agentProfileName));
        agentProfileInfoToAdd.setLogViewingEnabled(true);
        agentProfileInfoToAdd.setTrayIconEnabled(true);

        AgentProfileDTO profileCreated = profileServiceToTest.addAgentProfile(agentProfileInfoToAdd);

        // Ensure we can retrieve the same agent profile from DB
        AgentProfileDTO profileToCompare = retrieveAgentProfileDTOByName(agentProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated, profileToCompare);

        // Now try to delete
        profileServiceToTest.removeAgentProfile(profileCreated.getId());

        // Ensure that we can't retrieve it now
        assertNull("Ensure agent profile was deleted", retrieveAgentProfileDTOByName(agentProfileName));
    }

    public void testRemoveCommProfile() throws MalformedURIException, UnauthorizedCallerFault, RemoteException, RemoteException {
        String commProfileName = "testDeleteProfile";

        CommProfileInfo commProfileInfoToAdd = new CommProfileInfo();
        commProfileInfoToAdd.setName(new Token(commProfileName));
        commProfileInfoToAdd.setDABSLocation(new org.apache.axis.types.URI("http://www.foo.com"));
        commProfileInfoToAdd.setAgentType(AgentTypeDTO.DESKTOP);
        commProfileInfoToAdd.setHeartBeatFrequency(new TimeIntervalDTO(new UnsignedShort(10), TimeUnits.fromString("seconds")));
        commProfileInfoToAdd.setLogLimit(new UnsignedShort(30));
        commProfileInfoToAdd.setLogFrequency(new TimeIntervalDTO(new UnsignedShort(40), TimeUnits.fromString("seconds")));
        commProfileInfoToAdd.setPushEnabled(Boolean.FALSE);
        commProfileInfoToAdd.setDefaultPushPort(new UnsignedShort(9));        
        commProfileInfoToAdd.setAssignedActivityJournalingName("Default");
        commProfileInfoToAdd.setPassword("password");

        CommProfileDTO profileCreated = profileServiceToTest.addCommProfile(commProfileInfoToAdd);

        // Ensure we can retrieve the same comm profile from DB
        CommProfileDTO profileToCompare = retrieveCommProfileDTOByName(commProfileName);
        assertEquals("Ensure value retrieved from database is equals to that created.", profileCreated.getId(), profileToCompare.getId());

        // Now try to delete
        profileServiceToTest.removeCommProfile(profileCreated.getId());

        // Ensure that we can't retrieve it now
        assertNull("Ensure comm profile was deleted", retrieveCommProfileDTOByName(commProfileName));
    }

    public void testGetActivityJournalingSettings() throws ServiceNotReadyFault, RemoteException {
        ActivityJournalingSettingsDTOList settingsList = this.profileServiceToTest.getActivityJournalingSettings(AgentTypeEnumType.DESKTOP.getName());
        assertNotNull("testGetActityJournalingSettings - Ensure journaling settings list is not null", settingsList);
        ActivityJournalingSettingsDTO[] settingsRetrieved = settingsList.getActivityJournalingSettings();
        assertEquals("testGetActityJournalingSettings - Ensure journaling settings list is of right size", 3, settingsRetrieved.length);
        assertEquals("testGetActityJournalingSettings - Ensure journaling settings list item 1 is as expectd", "Extended", settingsRetrieved[0].getName());
        assertEquals("testGetActityJournalingSettings - Ensure journaling settings list item 2 is as expectd", "Default", settingsRetrieved[1].getName());
        assertEquals("testGetActityJournalingSettings - Ensure journaling settings list item 3 is as expectd", "Minimal", settingsRetrieved[2].getName());
    }
    
    /**
     * @param userProfileInfoName
     * @return
     * @throws RemoteException
     * @throws UnauthorizedCallerFault
     */
    private UserProfileDTO retrieveUserProfileDTOByName(String userProfileInfoName) throws UnauthorizedCallerFault, RemoteException {
        UserProfileDTOQuery userProfileDTOQuery = new UserProfileDTOQuery();

        UserProfileDTOQueryTerm[] queryTerms = { new UserProfileDTOQueryTerm(new UserProfileDTOQueryField("name"), userProfileInfoName) };
        UserProfileDTOQueryTermSet queryTermsSet = new UserProfileDTOQueryTermSet();
        queryTermsSet.setUserProfileDTOQueryTerm(queryTerms);

        userProfileDTOQuery.setUserProfileDTOQueryTermSet(queryTermsSet);
        userProfileDTOQuery.setFetchSize(10);
        UserProfileDTOList userProfilesRetrieved = profileServiceToTest.getUserProfiles(userProfileDTOQuery);

        UserProfileDTO[] userProfilesRetrievedArray = userProfilesRetrieved.getUserProfileDTO();
        if ((userProfilesRetrievedArray == null) || (userProfilesRetrievedArray.length == 0)) {
            return null;
        } else {
            return userProfilesRetrievedArray[0];
        }
    }

    /**
     * @param commProfileInfoName
     * @return
     * @throws RemoteException
     * @throws UnauthorizedCallerFault
     */
    private CommProfileDTO retrieveCommProfileDTOByName(String commProfileInfoName) throws UnauthorizedCallerFault, RemoteException {
        CommProfileDTOQuery commProfileDTOQuery = new CommProfileDTOQuery();

        CommProfileDTOQueryTerm[] queryTerms = { new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, commProfileInfoName) };
        CommProfileDTOQueryTermSet queryTermsSet = new CommProfileDTOQueryTermSet();
        queryTermsSet.setCommProfileDTOQueryTerm(queryTerms);

        commProfileDTOQuery.setCommProfileDTOQueryTermSet(queryTermsSet);
        commProfileDTOQuery.setFetchSize(10);
        CommProfileDTOList commProfilesRetrieved = profileServiceToTest.getCommProfiles(commProfileDTOQuery);

        CommProfileDTO[] commProfilesRetrievedArray = commProfilesRetrieved.getCommProfileDTO();
        if ((commProfilesRetrievedArray == null) || (commProfilesRetrievedArray.length == 0)) {
            return null;
        } else {
            return commProfilesRetrievedArray[0];
        }
    }

    /**
     * @param agentProfileInfoName
     * @return
     * @throws RemoteException
     * @throws UnauthorizedCallerFault
     */
    private AgentProfileDTO retrieveAgentProfileDTOByName(String agentProfileInfoName) throws UnauthorizedCallerFault, RemoteException {
        AgentProfileDTOQuery agentProfileDTOQuery = new AgentProfileDTOQuery();

        AgentProfileDTOQueryTerm[] queryTerms = { new AgentProfileDTOQueryTerm(new AgentProfileDTOQueryField("name"), agentProfileInfoName) };
        AgentProfileDTOQueryTermSet queryTermsSet = new AgentProfileDTOQueryTermSet();
        queryTermsSet.setAgentProfileDTOQueryTerm(queryTerms);

        agentProfileDTOQuery.setAgentProfileDTOQueryTermSet(queryTermsSet);
        agentProfileDTOQuery.setFetchSize(10);
        AgentProfileDTOList agentProfilesRetrieved = profileServiceToTest.getAgentProfiles(agentProfileDTOQuery);

        AgentProfileDTO[] agentProfilesRetrievedArray = agentProfilesRetrieved.getAgentProfileDTO();
        if ((agentProfilesRetrievedArray == null) || (agentProfilesRetrievedArray.length == 0)) {
            return null;
        } else {
            return agentProfilesRetrievedArray[0];
        }
    }

    /**
     * Retrieve a JUnit Test Suite for all of the methods in this test class
     * 
     * @return
     */
    public static Test getTestSuite() {
        TestSuite suiteToReturn = new TestSuite("Profile Service Test");
        suiteToReturn.addTest(new ProfileServiceTest("testAddUserProfile"));
        suiteToReturn.addTest(new ProfileServiceTest("testAddCommProfile"));
        suiteToReturn.addTest(new ProfileServiceTest("testAddAgentProfile"));
        suiteToReturn.addTest(new ProfileServiceTest("testUpdateUserProfile"));
        suiteToReturn.addTest(new ProfileServiceTest("testUpdateCommProfile"));
        suiteToReturn.addTest(new ProfileServiceTest("testUpdateAgentProfile"));
        suiteToReturn.addTest(new ProfileServiceTest("testGetUserProfiles"));
        suiteToReturn.addTest(new ProfileServiceTest("testGetCommProfiles"));
        suiteToReturn.addTest(new ProfileServiceTest("testGetAgentProfiles"));
        suiteToReturn.addTest(new ProfileServiceTest("testRemoveUserProfile"));
        suiteToReturn.addTest(new ProfileServiceTest("testRemoveCommProfile"));
        suiteToReturn.addTest(new ProfileServiceTest("testRemoveAgentProfile"));

        return suiteToReturn;
    }
}
