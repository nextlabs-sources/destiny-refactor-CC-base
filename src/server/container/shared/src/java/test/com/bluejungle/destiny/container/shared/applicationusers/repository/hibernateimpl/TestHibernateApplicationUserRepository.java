/*
 * Created on Jul 18, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext;
import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserDeleteFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserImportFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.GroupSearchSpecImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.UserSearchSpecImpl;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.GroupLinkAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticationDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.MockExternalDomainManagerImpl;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.MockExternalGroupImpl;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.MockExternalUserImpl;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.MockUserAccessProviderImpl;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/TestHibernateApplicationUserRepository.java#1 $
 */

public class TestHibernateApplicationUserRepository extends BaseContainerSharedTestCase {
    private static final String LOCAL_DOMAIN_NAME = "Local";
    private static final String SUPER_USER_PASSWORD = "123blue!";

    private HibernateApplicationUserRepository repositoryToTest;
    private MockExternalDomainManagerImpl mockExternalDomainManager;

    private Set usersToDelete = new HashSet();
    private Set groupsToDelete = new HashSet();
    private Set domainsToDelete = new HashSet();

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        this.repositoryToTest = new HibernateApplicationUserRepository();
        this.mockExternalDomainManager = new MockExternalDomainManagerImpl();
        this.repositoryToTest.initialize(null, null, mockExternalDomainManager);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Iterator usersToDeleteIterator = this.usersToDelete.iterator();
        while (usersToDeleteIterator.hasNext()) {
            IApplicationUser nextUser = (IApplicationUser) usersToDeleteIterator.next();
            IApplicationUserDomain domain = this.repositoryToTest.getApplicationUserDomain(nextUser.getDomainName());
            domain.deleteApplicationUser(nextUser);
        }

        Iterator groupsToDeleteIterator = this.groupsToDelete.iterator();
        while (groupsToDeleteIterator.hasNext()) {
            IAccessGroup nextGroup = (IAccessGroup) groupsToDeleteIterator.next();
            IApplicationUserDomain domain = this.repositoryToTest.getApplicationUserDomain(nextGroup.getDomainName());
            domain.deleteAccessGroup(nextGroup);
        }

        Iterator domainsToDeleteIterator = this.domainsToDelete.iterator();
        while (domainsToDeleteIterator.hasNext()) {
            IApplicationUserDomain nextDomain = (IApplicationUserDomain) domainsToDeleteIterator.next();
            this.repositoryToTest.deleteDomain(nextDomain.getName());
        }

        super.tearDown();
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#authenticateSuperUser(java.lang.String)}.
     * @throws AuthenticationFailedException 
     * @throws ApplicationUserRepositoryAccessException 
     */
    public void testAuthenticateSuperUser() throws AuthenticationFailedException, ApplicationUserRepositoryAccessException {
        assertEquals("testAuthenticateSuperUser - Ensure super user is authenticated as expected", this.repositoryToTest.getSuperUser().getDestinyId(), this.repositoryToTest.authenticateSuperUser(SUPER_USER_PASSWORD).getDestinyId());
        
 
        try {
            this.repositoryToTest.authenticateSuperUser("bad password");
            fail("testAuthenticateSuperUser - Calling authenticateSuperUser with with bad password should throw AuthenticationFailedException");
        } catch (AuthenticationFailedException exception) {
        }
        
        try {
            this.repositoryToTest.authenticateSuperUser(null);
            fail("testAuthenticateSuperUser - Calling authenticateSuperUser with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getSuperUser(java.lang.String)}.
     * @throws AuthenticationFailedException 
     * @throws ApplicationUserRepositoryAccessException 
     */
    public void testGetSuperUser() throws AuthenticationFailedException, ApplicationUserRepositoryAccessException {
        IApplicationUser superUser = this.repositoryToTest.getSuperUser();
        assertNotNull("testAuthenticateSuperUser - Ensure super user is retrieved", superUser);
        assertEquals("testGetSuperUser - ", IApplicationUserManager.SUPER_USER_USERNAME, superUser.getLogin());
    }
    
    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getAllDomainNames()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#createDomain(java.lang.String)}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#deleteDomain(java.lang.String)}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getApplicationUserDomain(java.lang.String)}.
     */
    public void testCreateDomainDeleteDomainGetApplicationUserDomainGetAllDomainNames() throws ApplicationUserRepositoryAccessException, DomainNotFoundException, DomainAlreadyExistsException {
        Set allDomainNames = this.repositoryToTest.getAllDomainNames();
        assertEquals("testGetAllDomainNames - Ensure all domain names initially of size 1", 1, allDomainNames.size());
        assertTrue("testGetAllDomainNames - Ensure contains the local domain initially", allDomainNames.contains(LOCAL_DOMAIN_NAME));

        // Now, create a domain
        String domainName = "foo";
        IApplicationUserDomain createdDomain = this.repositoryToTest.createDomain(domainName);
        allDomainNames = this.repositoryToTest.getAllDomainNames();
        assertEquals("testGetAllDomainNames - Ensure all domain names now of size 2", 2, allDomainNames.size());
        assertTrue("testGetAllDomainNames - Ensure contains the local domain", allDomainNames.contains(LOCAL_DOMAIN_NAME));
        assertTrue("testGetAllDomainNames - Ensure contains the new domain", allDomainNames.contains(domainName));

        // See if we can retrieve it
        IApplicationUserDomain domainRetrieved = this.repositoryToTest.getApplicationUserDomain(domainName);
        assertEquals("testCreateDomainDeleteDomainGetApplicationUserDomainGetAllDomainNames - Ensure domain can be retrieved", createdDomain, domainRetrieved);

        // Try to create a domain with the same name
        try {
            this.repositoryToTest.createDomain(domainName);
            fail("testCreateDomainDeleteDomainGetAllDomainNames - Expected DomainAlreadyExistsException");
        } catch (DomainAlreadyExistsException exception) {
        }

        this.repositoryToTest.deleteDomain(domainName);
        allDomainNames = this.repositoryToTest.getAllDomainNames();
        assertEquals("testGetAllDomainNames - Ensure all domain names now size of 1 after deletion", 1, allDomainNames.size());
        assertTrue("testGetAllDomainNames - Ensure contains only the local domain after deletion", allDomainNames.contains(LOCAL_DOMAIN_NAME));

        try {
            this.repositoryToTest.getApplicationUserDomain(domainName);
            fail("testCreateDomainDeleteDomainGetAllDomainNames - Expected DomainNotFoundException when trying to retrieve domain which doesn't exist");
        } catch (DomainNotFoundException exception) {
        }

        try {
            this.repositoryToTest.deleteDomain(domainName);
            fail("testCreateDomainDeleteDomainGetAllDomainNames - Expected DomainNotFoundException");
        } catch (DomainNotFoundException exception) {
        }

        try {
            this.repositoryToTest.createDomain(null);
            fail("testGetDefaultAdminDomainName - Calling createDomain with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.repositoryToTest.getApplicationUserDomain(null);
            fail("testGetDefaultAdminDomainName - Calling getApplicationUserDomain with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.repositoryToTest.deleteDomain(null);
            fail("testGetDefaultAdminDomainName - Calling deleteDomain with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getDefaultDomainName()}.
     */
    public void testGetDefaultAdminDomainName() {
        assertEquals("testGetDefaultAdminDomainName - Should retrieve Local as default domain name", LOCAL_DOMAIN_NAME, this.repositoryToTest.getDefaultAdminDomainName());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getApplicationUsers(com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec[])}.
     */
    public void testGetApplicationUsers() throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException, DomainNotFoundException, ApplicationUserCreationFailedException, UserAlreadyExistsException {
        String secondDomainName = "SecondDomain";
        IApplicationUserDomain secondDomain = this.repositoryToTest.createDomain(secondDomainName);
        IApplicationUserDomain localDomain = this.repositoryToTest.getApplicationUserDomain(LOCAL_DOMAIN_NAME);

        String loginOne = "loginOne";
        String firstNameOne = "firstNameOne";
        String lastNameOne = "lastNameOne";
        String passwordOne = "passwordOne";
        IApplicationUser userOne = secondDomain.createNewUser(loginOne, firstNameOne, lastNameOne, passwordOne);

        SortedSet applicationUsers = this.repositoryToTest.getApplicationUsers(null);
        assertEquals("testGetApplicationUsers - Ensure retrieve application users is of size 1", 1, applicationUsers.size());
        assertTrue("testGetApplicationUsers - Ensure retrieved application users contains user one", applicationUsers.contains(userOne));

        // Add another user
        String loginTwo = "loginTwo";
        String firstNameTwo = "firstNameTwo";
        String lastNameTwo = "lastNameTwo";
        String passwordTwo = "passwordTwo";
        IApplicationUser userTwo = localDomain.createNewUser(loginTwo, firstNameTwo, lastNameTwo, passwordTwo);

        applicationUsers = this.repositoryToTest.getApplicationUsers(null);
        assertEquals("testGetApplicationUsers - Ensure retrieve application users is of size 2", 2, applicationUsers.size());
        assertTrue("testGetApplicationUsers - Ensure retrieved application users contains user one", applicationUsers.contains(userOne));
        assertTrue("testGetApplicationUsers - Ensure retrieved application users contains user two", applicationUsers.contains(userTwo));
        assertEquals("testGetApplicationUsers - Ensure retrieved application users is sorted as expected", userOne, applicationUsers.iterator().next());

        // Try query
        IUserSearchSpec searchSpec = new UserSearchSpecImpl("l");
        IUserSearchSpec[] searchSpecArray = { searchSpec };
        applicationUsers = this.repositoryToTest.getApplicationUsers(searchSpecArray);
        assertEquals("testGetApplicationUsers - Ensure retrieve application users from query is of size 2", 2, applicationUsers.size());
        assertTrue("testGetApplicationUsers - Ensure retrieved application users from query contains user one", applicationUsers.contains(userOne));
        assertTrue("testGetApplicationUsers - Ensure retrieved application users from query contains user two", applicationUsers.contains(userTwo));
        assertEquals("testGetApplicationUsers - Ensure retrieved application users from query is sorted as expected", userOne, applicationUsers.iterator().next());

        IUserSearchSpec searchSpecTwo = new UserSearchSpecImpl("lastNameT");
        searchSpecArray = new IUserSearchSpec[] { searchSpecTwo };
        applicationUsers = this.repositoryToTest.getApplicationUsers(searchSpecArray);
        assertEquals("testGetApplicationUsers - Ensure retrieve application users from second query is of size 1", 1, applicationUsers.size());
        assertTrue("testGetApplicationUsers - Ensure retrieved application users from second query contains user two", applicationUsers.contains(userTwo));

        searchSpecArray = new IUserSearchSpec[] { searchSpec, searchSpecTwo };
        applicationUsers = this.repositoryToTest.getApplicationUsers(searchSpecArray);
        assertEquals("testGetApplicationUsers - Ensure retrieve application users from combined query is of size 2", 2, applicationUsers.size());
        assertTrue("testGetApplicationUsers - Ensure retrieved application users from combined query contains user one", applicationUsers.contains(userOne));
        assertTrue("testGetApplicationUsers - Ensure retrieved application users from combined query contains user two", applicationUsers.contains(userTwo));
        assertEquals("testGetApplicationUsers - Ensure retrieved application users from combined query is sorted as expected", userOne, applicationUsers.iterator().next());

        // FIX ME - Test boundary conditions on search!!!!! What are the
        // expected results?!

        this.domainsToDelete.add(secondDomain);
        this.usersToDelete.add(userOne);
        this.usersToDelete.add(userTwo);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getAccessGroups(com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec[])}.
     */
    public void testGetAccessGroups() throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException, GroupAlreadyExistsException, DomainNotFoundException {
        String secondDomainName = "SecondDomain";
        IApplicationUserDomain secondDomain = this.repositoryToTest.createDomain(secondDomainName);
        IApplicationUserDomain localDomain = this.repositoryToTest.getApplicationUserDomain(LOCAL_DOMAIN_NAME);

        String titleOne = "AAMytitleOne";
        String descriptionOne = "MyDescriptionOne";
        IAccessGroup groupOne = secondDomain.createAccessGroup(titleOne, descriptionOne);

        SortedSet accessGroups = this.repositoryToTest.getAccessGroups(null);
        assertEquals("testGetAccessGroups - Ensure retrieve access groups is of size 3 - Created groups and seed groups", 3, accessGroups.size());
        assertTrue("testGetAccessGroups - Ensure retrieved access groups contains group one", accessGroups.contains(groupOne));

        String titleTwo = "AAMytitleTwo";
        String descriptionTwo = "MyDescriptionTwo";
        IAccessGroup groupTwo = localDomain.createAccessGroup(titleTwo, descriptionTwo);

        accessGroups = this.repositoryToTest.getAccessGroups(null);
        assertEquals("testGetAccessGroups - Ensure retrieve access groups is of size 4", 4, accessGroups.size());
        assertTrue("testGetAccessGroups - Ensure retrieved access groups contains group one", accessGroups.contains(groupOne));
        assertTrue("testGetAccessGroups - Ensure retrieved access groups contains group two", accessGroups.contains(groupTwo));
        assertEquals("testGetAccessGroups - Ensure retrieved access groups is sorted as expected", groupOne, accessGroups.iterator().next());

        // Try query
        IGroupSearchSpec searchSpec = new GroupSearchSpecImpl("AA");
        IGroupSearchSpec[] searchSpecArray = { searchSpec };
        accessGroups = this.repositoryToTest.getAccessGroups(searchSpecArray);
        assertEquals("testGetAccessGroups - Ensure retrieve access groups from query is of size 2", 2, accessGroups.size());
        assertTrue("testGetAccessGroups - Ensure retrieved access groups from query contains group one", accessGroups.contains(groupOne));
        assertTrue("testGetAccessGroups - Ensure retrieved access groups from query contains group two", accessGroups.contains(groupTwo));
        assertEquals("testGetAccessGroups - Ensure retrieved access groups from query is sorted as expected", groupOne, accessGroups.iterator().next());

        IGroupSearchSpec searchSpecTwo = new GroupSearchSpecImpl("AAMytitleT");
        searchSpecArray = new IGroupSearchSpec[] { searchSpecTwo };
        accessGroups = this.repositoryToTest.getAccessGroups(searchSpecArray);
        assertEquals("testGetAccessGroups - Ensure retrieve access groups from second query is of size 1", 1, accessGroups.size());
        assertTrue("testGetAccessGroups - Ensure retrieved access groups from second query contains group two", accessGroups.contains(groupTwo));

        searchSpecArray = new IGroupSearchSpec[] { searchSpec, searchSpecTwo };
        accessGroups = this.repositoryToTest.getAccessGroups(searchSpecArray);
        assertEquals("testGetAccessGroups - Ensure retrieve access groups from combined query is of size 2", 2, accessGroups.size());
        assertTrue("testGetAccessGroups - Ensure retrieved access groups from combined query contains group one", accessGroups.contains(groupOne));
        assertTrue("testGetAccessGroups - Ensure retrieved access groups from combined query contains group two", accessGroups.contains(groupTwo));
        assertEquals("testGetAccessGroups - Ensure retrieved access groups from combined query is sorted as expected", groupOne, accessGroups.iterator().next());

        // FIX ME - Test boundary conditions on search!!!!! What are the
        // expected results?!

        this.domainsToDelete.add(secondDomain);
        this.groupsToDelete.add(groupOne);
        this.groupsToDelete.add(groupTwo);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getAuthenticationDomain(java.lang.String)}.
     */
    public void testGetAuthenticationDomainString() throws ApplicationUserRepositoryAccessException, DomainNotFoundException {
        IAuthenticationDomain authenticationDomain = this.repositoryToTest.getAuthenticationDomain(LOCAL_DOMAIN_NAME);
        assertNotNull("testGetAuthenticationDomainString - Ensure local authenticaiton domain is not null", authenticationDomain);

        try {
            this.repositoryToTest.getAuthenticationDomain("foo");
            fail("testGetAuthenticationDomainString - Calling getAuthenticationDomain with invalid domain should throw DomainNotFoundException");
        } catch (DomainNotFoundException exception) {
        }

        try {
            this.repositoryToTest.getAuthenticationDomain(null);
            fail("testGetAuthenticationDomainString - Calling getAuthenticationDomain with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getAuthenticationDomain(java.lang.String, com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator)}.
     */
    public void testGetAuthenticationDomainStringIAuthenticator() throws Exception {        
        TestAuthenticator testAuthenticator = new TestAuthenticator();
        IAuthenticationDomain authenticationDomain = this.repositoryToTest.getAuthenticationDomain(LOCAL_DOMAIN_NAME, testAuthenticator);
        assertNotNull("testGetAuthenticationDomainString - Ensure local authenticaiton domain is not null", authenticationDomain);

        try {
            authenticationDomain.authenticateUser("foo", "bar");
        } catch (AuthenticationFailedException exception) {            
        }
        assertEquals("testGetAuthenticationDomainStringIAuthenticator - Ensure overide authenticator called", 1, testAuthenticator.getNumAuthenticateCalled());
       
        // Test case insensitive authentication

        IAuthenticator caseInsensitiveAutheticator = new IAuthenticator() {
            public IAuthenticationContext authenticate(String login, String password) throws AuthenticationFailedException {
                return new IAuthenticationContext() {
                    public void logoff() {}

                    public String getSubjectUsername() {
                        return "casetest";
                    }
                };
            }
            public void initialize(Properties properties) throws InvalidConfigurationException {
            }
        };

        IApplicationUserDomain userDomain = this.repositoryToTest.getApplicationUserDomain(LOCAL_DOMAIN_NAME);
        IApplicationUser createdUser = userDomain.createNewUser("casetest", "foo", "bar", "password");
        try{
            authenticationDomain = this.repositoryToTest.getAuthenticationDomain(LOCAL_DOMAIN_NAME, caseInsensitiveAutheticator);
            authenticationDomain.authenticateUser("CaSeTeSt", "password");
        } finally {
            userDomain.deleteApplicationUser(createdUser);
        }
        
        try {
            this.repositoryToTest.getAuthenticationDomain("foo", testAuthenticator);
            fail("testGetAuthenticationDomainString - Calling getAuthenticationDomain with invalid domain should throw DomainNotFoundException");
        } catch (DomainNotFoundException exception) {
        }

        try {
            this.repositoryToTest.getAuthenticationDomain(null, testAuthenticator);
            fail("testGetAuthenticationDomainString - Calling getAuthenticationDomain with null domain name should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.repositoryToTest.getAuthenticationDomain(LOCAL_DOMAIN_NAME, null);
            fail("testGetAuthenticationDomainString - Calling getAuthenticationDomain with null domain name should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getUser(long)}.
     */
    public void testGetUser() throws ApplicationUserRepositoryAccessException, DomainNotFoundException, ApplicationUserCreationFailedException, UserAlreadyExistsException, UserNotFoundException {
        String loginTwo = "loginTwo";
        String firstNameTwo = "firstNameTwo";
        String lastNameTwo = "lastNameTwo";
        String passwordTwo = "passwordTwo";
        IApplicationUserDomain localDomain = this.repositoryToTest.getApplicationUserDomain(LOCAL_DOMAIN_NAME);
        IApplicationUser userTwo = localDomain.createNewUser(loginTwo, firstNameTwo, lastNameTwo, passwordTwo);

        // Try to retrieve it
        IApplicationUser retrievedUser = this.repositoryToTest.getUser(userTwo.getDestinyId().longValue());
        assertEquals("testGetUser - Ensure user retrieved as expected", userTwo, retrievedUser);

        localDomain.deleteApplicationUser(retrievedUser);

        try {
            this.repositoryToTest.getUser(userTwo.getDestinyId().longValue());
            fail("testGetUser - Expected UserNotFoundException");
        } catch (UserNotFoundException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getUsersInBulk(java.lang.Long[])}.
     */
    public void testGetUsersInBulk() throws ApplicationUserRepositoryAccessException, ApplicationUserCreationFailedException, UserAlreadyExistsException, DomainAlreadyExistsException, DomainNotFoundException, UserNotFoundException {
        String secondDomainName = "SecondDomain";
        IApplicationUserDomain secondDomain = this.repositoryToTest.createDomain(secondDomainName);
        IApplicationUserDomain localDomain = this.repositoryToTest.getApplicationUserDomain(LOCAL_DOMAIN_NAME);

        String loginOne = "loginOne";
        String firstNameOne = "firstNameOne";
        String lastNameOne = "lastNameOne";
        String passwordOne = "passwordOne";
        IApplicationUser userOne = secondDomain.createNewUser(loginOne, firstNameOne, lastNameOne, passwordOne);

        // Add another user
        String loginTwo = "loginTwo";
        String firstNameTwo = "firstNameTwo";
        String lastNameTwo = "lastNameTwo";
        String passwordTwo = "passwordTwo";
        IApplicationUser userTwo = localDomain.createNewUser(loginTwo, firstNameTwo, lastNameTwo, passwordTwo);

        Long ids[] = { userOne.getDestinyId(), userTwo.getDestinyId() };
        Map usersRetrieved = this.repositoryToTest.getUsersInBulk(ids);
        assertEquals("testGetUsersInBulk - Ensure two users retreived.", 2, usersRetrieved.size());
        assertEquals("testGetUsersInBulk - Ensure user one in retrieved user map", usersRetrieved.get(userOne.getDestinyId()), userOne);
        assertEquals("testGetUsersInBulk - Ensure user two in retrieved user map", usersRetrieved.get(userTwo.getDestinyId()), userTwo);

        secondDomain.deleteApplicationUser(userOne);
        usersRetrieved = this.repositoryToTest.getUsersInBulk(ids);
        assertEquals("testGetUsersInBulk - Ensure 1 users retreived after deleting user one.", 1, usersRetrieved.size());
        assertEquals("testGetUsersInBulk - Ensure user two in retrieved user map after deleting user one.", usersRetrieved.get(userTwo.getDestinyId()), userTwo);

        try {
            this.repositoryToTest.getUsersInBulk(null);
            fail("testGetUsersInBulk - Calling getUsersInBulk with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        this.domainsToDelete.add(secondDomain);
        this.usersToDelete.add(userTwo);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getAccessGroup(long)}.
     */
    public void testGetAccessGroup() throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException, GroupAlreadyExistsException, GroupNotFoundException {
        String secondDomainName = "SecondDomain";
        IApplicationUserDomain secondDomain = this.repositoryToTest.createDomain(secondDomainName);

        String titleOne = "MytitleOne";
        String descriptionOne = "MyDescriptionOne";
        IAccessGroup group = secondDomain.createAccessGroup(titleOne, descriptionOne);

        // Try to retrieve it
        IAccessGroup retrievedUserGroup = this.repositoryToTest.getAccessGroup(group.getDestinyId().longValue());
        assertEquals("testGetGroup - Ensure group retrieved as expected", group, retrievedUserGroup);

        secondDomain.deleteAccessGroup(retrievedUserGroup);

        try {
            this.repositoryToTest.getAccessGroup(group.getDestinyId().longValue());
            fail("testGetGroup - Expected GroupNotFoundException");
        } catch (GroupNotFoundException exception) {

        }

        this.domainsToDelete.add(secondDomain);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getUsersInAccessGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup)}.
     */
    public void testGetUsersInAccessGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, DomainAlreadyExistsException, DomainNotFoundException, ApplicationUserCreationFailedException, UserAlreadyExistsException,
            GroupLinkAlreadyExistsException, ApplicationUserImportFailedException, UserNotFoundException {
        String secondDomainName = "SecondDomain";
        IApplicationUserDomain secondDomain = this.repositoryToTest.createDomain(secondDomainName);
        IApplicationUserDomain localDomain = this.repositoryToTest.getApplicationUserDomain(LOCAL_DOMAIN_NAME);

        String titleOne = "MytitleOne";
        String descriptionOne = "MyDescriptionOne";
        IInternalAccessGroup group = secondDomain.createAccessGroup(titleOne, descriptionOne);

        // Make sure there are no users in this group
        SortedSet usersInGroup = this.repositoryToTest.getUsersInAccessGroup(group);
        assertTrue("testGetUsersInAccessGroup - Ensure no users in group when created", usersInGroup.isEmpty());

        String loginOne = "loginOne";
        String firstNameOne = "firstNameOne";
        String lastNameOne = "lastNameOne";
        String passwordOne = "passwordOne";
        IApplicationUser userOne = secondDomain.createNewUser(loginOne, firstNameOne, lastNameOne, passwordOne);

        // Add another user
        String loginTwo = "loginTwo";
        String firstNameTwo = "firstNameTwo";
        String lastNameTwo = "lastNameTwo";
        String passwordTwo = "passwordTwo";
        IApplicationUser userTwo = localDomain.createNewUser(loginTwo, firstNameTwo, lastNameTwo, passwordTwo);

        secondDomain.addUsersToGroup(group, Collections.singleton(userOne));
        usersInGroup = this.repositoryToTest.getUsersInAccessGroup(group);
        assertEquals("testGetUsersInAccessGroup - Ensure size of users set is 1", 1, usersInGroup.size());
        assertTrue("testGetUsersInAccessGroup - Ensure users in group contains users one", usersInGroup.contains(userOne));

        secondDomain.addUsersToGroup(group, Collections.singleton(userTwo));
        usersInGroup = this.repositoryToTest.getUsersInAccessGroup(group);
        assertEquals("testGetUsersInAccessGroup - Ensure size of users set is 2", 2, usersInGroup.size());
        assertTrue("testGetUsersInAccessGroup - Ensure users in group contains users one", usersInGroup.contains(userOne));
        assertTrue("testGetUsersInAccessGroup - Ensure users in group contains users two", usersInGroup.contains(userTwo));

        // Remove them both and try again
        secondDomain.removeUsersFromGroup(group, Collections.singleton(userOne));
        secondDomain.removeUsersFromGroup(group, Collections.singleton(userTwo));
        usersInGroup = this.repositoryToTest.getUsersInAccessGroup(group);
        assertTrue("testGetUsersInAccessGroup - Ensure no users in group when users removed", usersInGroup.isEmpty());

        MockUserAccessProviderImpl userAccessProvider = (MockUserAccessProviderImpl) this.mockExternalDomainManager.getExternalDomain(MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME).getUserAccessProvider();
        byte[] externalGroupId = { 1, 2 };
        MockExternalGroupImpl externalGroup = new MockExternalGroupImpl(externalGroupId, "MyTitle", MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        MockExternalUserImpl externalUser = new MockExternalUserImpl("MyUsername", "MyFirstName", "MyLastName", MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        userAccessProvider.setSingletonGroup(externalGroup);
        userAccessProvider.setSingletonUserInGroup(externalUser);

        IApplicationUserDomain domain = this.repositoryToTest.createDomain(MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        IAccessGroup linkedGroup = domain.linkExternalGroup(externalGroup.getExternalId(), externalGroup.getTitle());
        usersInGroup = this.repositoryToTest.getUsersInAccessGroup(linkedGroup);
        assertTrue("testGetUsersInAccessGroup - Ensure no users in linked group when created", usersInGroup.isEmpty());

        // Now import users
        IApplicationUser importedUser = domain.importExternalUser(externalUser);
        usersInGroup = this.repositoryToTest.getUsersInAccessGroup(linkedGroup);
        assertEquals("testGetUsersInAccessGroup - Ensure size of users set is 1 after importing external user", 1, usersInGroup.size());
        assertTrue("testGetUsersInAccessGroup - Ensure users in group contains external user user", usersInGroup.contains(importedUser));

        domain.deleteApplicationUser(importedUser);
        usersInGroup = this.repositoryToTest.getUsersInAccessGroup(linkedGroup);
        assertTrue("testGetUsersInAccessGroup - Ensure no users in linked group after deleting user", usersInGroup.isEmpty());

        try {
            this.repositoryToTest.getUsersInAccessGroup(null);
            fail("testGetUsersInAccessGroup - Calling getUsersInAccessGroup with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        this.groupsToDelete.add(linkedGroup);
        this.groupsToDelete.add(group);
        this.usersToDelete.add(userOne);
        this.usersToDelete.add(userTwo);
        this.domainsToDelete.add(secondDomain);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getUsersWithPrimaryAccessGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup)}.
     */
    public void testGetUsersWithPrimaryAccessGroup() throws ApplicationUserRepositoryAccessException, ApplicationUserCreationFailedException, UserAlreadyExistsException, GroupAlreadyExistsException, DomainAlreadyExistsException, UserNotFoundException {
        String secondDomainName = "SecondDomain";
        IApplicationUserDomain secondDomain = this.repositoryToTest.createDomain(secondDomainName);

        String titleOne = "MytitleOne";
        String descriptionOne = "MyDescriptionOne";
        IInternalAccessGroup group = secondDomain.createAccessGroup(titleOne, descriptionOne);

        // Make sure there are no users set with this group as primary access
        // group
        Set usersRetrieved = this.repositoryToTest.getUsersWithPrimaryAccessGroup(group);
        assertTrue("testGetUsersWithPrimaryAccessGroup - Ensure no users retrieved when group created", usersRetrieved.isEmpty());

        String loginOne = "loginOne";
        String firstNameOne = "firstNameOne";
        String lastNameOne = "lastNameOne";
        String passwordOne = "passwordOne";
        IApplicationUser userOne = secondDomain.createNewUser(loginOne, firstNameOne, lastNameOne, passwordOne);

        secondDomain.setPrimaryAccessGroupForUser(userOne, group);
        usersRetrieved = this.repositoryToTest.getUsersWithPrimaryAccessGroup(group);
        assertEquals("testGetUsersWithPrimaryAccessGroup - Ensure size of retrieve users is as expected", 1, usersRetrieved.size());
        assertTrue("testGetUsersWithPrimaryAccessGroup - Ensure user one retrieved", usersRetrieved.contains(userOne));

        // Now, delete user
        secondDomain.deleteApplicationUser(userOne);
        usersRetrieved = this.repositoryToTest.getUsersWithPrimaryAccessGroup(group);
        assertTrue("testGetUsersWithPrimaryAccessGroup - Ensure no users retrieved after single user is deleted", usersRetrieved.isEmpty());

        try {
            this.repositoryToTest.getUsersWithPrimaryAccessGroup(null);
            fail("testGetUsersWithPrimaryAccessGroup - Calling getUsersWithPrimaryAccessGroup with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
        this.groupsToDelete.add(group);
        this.domainsToDelete.add(secondDomain);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getInternalGroupsContainingUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser)}.
     */
    public void testGetInternalGroupsContainingUser() throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException, GroupAlreadyExistsException, ApplicationUserCreationFailedException, UserAlreadyExistsException {
        String secondDomainName = "SecondDomain";
        IApplicationUserDomain secondDomain = this.repositoryToTest.createDomain(secondDomainName);

        String titleOne = "MytitleOne";
        String descriptionOne = "MyDescriptionOne";
        IInternalAccessGroup group = secondDomain.createAccessGroup(titleOne, descriptionOne);

        String loginOne = "loginOne";
        String firstNameOne = "firstNameOne";
        String lastNameOne = "lastNameOne";
        String passwordOne = "passwordOne";
        IApplicationUser userOne = secondDomain.createNewUser(loginOne, firstNameOne, lastNameOne, passwordOne);

        Set groupsRetrieved = this.repositoryToTest.getInternalGroupsContainingUser(userOne);
        assertTrue("testGetInternalGroupsContainingUser - Ensure not groups containing user initially", groupsRetrieved.isEmpty());

        secondDomain.addUsersToGroup(group, Collections.singleton(userOne));
        groupsRetrieved = this.repositoryToTest.getInternalGroupsContainingUser(userOne);
        assertEquals("testGetInternalGroupsContainingUser - Ensure groups retrieved is of size expected", 1, groupsRetrieved.size());
        assertTrue("testGetInternalGroupsContainingUser - Ensure groups retrieved contains user one", groupsRetrieved.contains(group));

        // Delete group
        secondDomain.deleteAccessGroup(group);
        groupsRetrieved = this.repositoryToTest.getInternalGroupsContainingUser(userOne);
        assertTrue("testGetInternalGroupsContainingUser - Ensure no groups containing user after group is delete", groupsRetrieved.isEmpty());

        try {
            this.repositoryToTest.getInternalGroupsContainingUser(null);
            fail("testGetInternalGroupsContainingUser - Calling getInternalGroupsContainingUser with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        this.usersToDelete.add(userOne);
        this.domainsToDelete.add(secondDomain);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository#getPrimaryAccessGroupForUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser)}.
     */
    public void testGetPrimaryAccessGroupForUser() throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException, GroupAlreadyExistsException, ApplicationUserCreationFailedException, UserAlreadyExistsException, UserNotFoundException {
        String secondDomainName = "SecondDomain";
        IApplicationUserDomain secondDomain = this.repositoryToTest.createDomain(secondDomainName);

        String titleOne = "MytitleOne";
        String descriptionOne = "MyDescriptionOne";
        IInternalAccessGroup group = secondDomain.createAccessGroup(titleOne, descriptionOne);

        String loginOne = "loginOne";
        String firstNameOne = "firstNameOne";
        String lastNameOne = "lastNameOne";
        String passwordOne = "passwordOne";
        IApplicationUser userOne = secondDomain.createNewUser(loginOne, firstNameOne, lastNameOne, passwordOne);

        IAccessGroup primaryAccessGroupForUser = this.repositoryToTest.getPrimaryAccessGroupForUser(userOne);
        assertNull("testGetPrimaryAccessGroupForUser - Ensure primary access group intially null", primaryAccessGroupForUser);

        secondDomain.setPrimaryAccessGroupForUser(userOne, group);
        primaryAccessGroupForUser = this.repositoryToTest.getPrimaryAccessGroupForUser(userOne);
        assertEquals("testGetPrimaryAccessGroupForUser - Ensure primary access group set as expected", group, primaryAccessGroupForUser);

        // Now, delete group
        secondDomain.deleteAccessGroup(group);
        userOne = secondDomain.getUser(loginOne);
        primaryAccessGroupForUser = this.repositoryToTest.getPrimaryAccessGroupForUser(userOne);
        assertNull("testGetPrimaryAccessGroupForUser - Ensure primary access group is null after deleting group", primaryAccessGroupForUser);

        try {
            this.repositoryToTest.getPrimaryAccessGroupForUser(null);
            fail("testGetPrimaryAccessGroupForUser - Calling getPrimaryAccessGroupForUser with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
        this.usersToDelete.add(userOne);
        this.domainsToDelete.add(secondDomain);
    }


    /**
     * @author sgoldstein
     */
    private class TestAuthenticator implements IAuthenticator {
        private int numAuthenticateCalled = 0;

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator#authenticate(java.lang.String,
         *      java.lang.String)
         */
        public IAuthenticationContext authenticate(String login, String password) throws AuthenticationFailedException {
            this.numAuthenticateCalled++;
            throw new AuthenticationFailedException("");
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator#initialize(java.util.Properties)
         */
        public void initialize(Properties properties) throws InvalidConfigurationException {
        }

        /**
         * Retrieve the authenticateCalled.
         * 
         * @return the authenticateCalled.
         */
        int getNumAuthenticateCalled() {
            return this.numAuthenticateCalled;
        }
    }
}
