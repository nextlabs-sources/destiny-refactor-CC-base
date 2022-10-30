/*
 * Created on Jul 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserImportFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.GroupLinkAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import net.sf.hibernate.Session;

/**
 * Test case for AccessDomainDO
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/TestAccessDomainDO.java#1 $
 */

public class TestAccessDomainDO extends BaseContainerSharedTestCase {

	private static final String ADMINISTRATOR_PASSWORD = "administratorpassword";
	
    private static final String ACCESS_DOMAIN_NAME = "hehehe";
    private static final String SECOND_DOMAIN_NAME = "second";

    private AccessDomainDO accessDomainToTest;
    private AccessDomainDO secondDomain;
    private HibernateApplicationUserRepository userRepository;
    private IHibernateRepository hibernateRepository;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.userRepository = new HibernateApplicationUserRepository();

		this.accessDomainToTest = (AccessDomainDO) this.userRepository.createDomain(ACCESS_DOMAIN_NAME);
		this.secondDomain = (AccessDomainDO) this.userRepository.createDomain(SECOND_DOMAIN_NAME);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
    	Session hs = null;
    	try {
    		hs = getDataSource().getCountedSession();
    		
			this.accessDomainToTest.onDelete(hs);
			this.secondDomain.onDelete(hs);
			
			this.userRepository.deleteDomain(this.accessDomainToTest.getName());
			this.userRepository.deleteDomain(this.secondDomain.getName());
		} finally {
			if(hs != null){
				hs.close();
			}
			super.tearDown();
		}
    }
    
    
    /**
     * Returns a data source object that can be used to create sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
    	if(this.hibernateRepository != null){
    		return hibernateRepository;
    	}
    	
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for Access Domain.");
        }

        this.hibernateRepository = dataSource;
        return dataSource;
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#AccessDomainDO(java.lang.String)}.
     */
    public void testAccessDomainDOString() {
        String domainName = "fofoof";
        AccessDomainDO accessDomainCreated = new AccessDomainDO(domainName);
        assertEquals("testAccessDomainDOString - Ensure domain name set as expected", domainName, accessDomainCreated.getName());

        // Test NPE
        try {
            new AccessDomainDO(null);
            fail("testAccessDomainDOString - Calling AccesDomainDO constructor with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#getId()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#setId(java.lang.Long)}.
     */
    public void testGetSetId() {
        assertNotNull("testGetSetId - Ensure ID initially not null.", this.accessDomainToTest.getId());
        Long idToSet = new Long(55);
        this.accessDomainToTest.setId(idToSet);
        assertEquals("testGetSetId - Ensure id set as expected.", idToSet, this.accessDomainToTest.getId());

        // Test NPE
        try {
            this.accessDomainToTest.setId(null);
            fail("testGetSetId - Should throw NPE when specifying null id to setId()");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#getName()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#setName(java.lang.String)}.
     */
    public void testGetSetName() {
        assertEquals("testGetSetName - Ensure name initially as set in constructor.", ACCESS_DOMAIN_NAME, this.accessDomainToTest.getName());
        String nameToSet = "MyName";
        this.accessDomainToTest.setName(nameToSet);
        assertEquals("testGetSetName - Ensure name is set as expected", nameToSet, this.accessDomainToTest.getName());

        // Test NPE
        try {
            this.accessDomainToTest.setName(null);
            fail("testGetSetName - setName() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }

        // Return to original name to avoid problems in teardown();
        this.accessDomainToTest.setName(ACCESS_DOMAIN_NAME);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#addUsersToGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup, java.util.Collection)}
     * and *
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#removeUsersFromGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup, java.util.Collection)}.
     */
    public void testAddRemoveUsersToGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, ApplicationUserCreationFailedException, UserAlreadyExistsException {
        IInternalAccessGroup accessGroup = this.accessDomainToTest.createAccessGroup("MyGroup", "My Group Description");
        assertNotNull("testAddRemoveUsersToGroup - Ensure group created as expected.", accessGroup);

        assertTrue("testAddRemoveUsersToGroup - Ensure initially no users in access group", this.userRepository.getUsersInAccessGroup(accessGroup).isEmpty());

        IApplicationUser newUserOne = this.accessDomainToTest.createNewUser("MyLogin", "MyFirstName", "MyLastName", "MyPassword");
        IApplicationUser newUserTwo = this.accessDomainToTest.createNewUser("MyLoginTwo", "MyFirstNameTwo", "MyLastNameTwo", "MyPasswordTwo");
        Set<IApplicationUser> userSet = new HashSet<IApplicationUser>();
        userSet.add(newUserOne);
        userSet.add(newUserTwo);
        this.accessDomainToTest.addUsersToGroup(accessGroup, userSet);

        SortedSet userInGroup = this.userRepository.getUsersInAccessGroup(accessGroup);
        assertEquals("testAddRemoveUsersToGroup - Ensure 2 users added as expected", 2, userInGroup.size());
        assertTrue("testAddRemoveUsersToGroup - Ensure users in group set contains first user added", userInGroup.contains(newUserOne));
        assertTrue("testAddRemoveUsersToGroup - Ensure users in group set contains second user added", userInGroup.contains(newUserTwo));

        // remove one and try again
        this.accessDomainToTest.removeUsersFromGroup(accessGroup, Collections.singleton(newUserOne));
        userInGroup = this.userRepository.getUsersInAccessGroup(accessGroup);
        assertEquals("testAddRemoveUsersToGroup - Ensure 1 user is group as expected", 1, userInGroup.size());
        assertTrue("testAddRemoveUsersToGroup - Ensure users in group set contains second user added", userInGroup.contains(newUserTwo));

        // Try adding the same user
        this.accessDomainToTest.addUsersToGroup(accessGroup, Collections.singleton(newUserTwo));
        userInGroup = this.userRepository.getUsersInAccessGroup(accessGroup);
        assertEquals("testAddRemoveUsersToGroup - Ensure 1 user is group as expected after adding second user again", 1, userInGroup.size());
        assertTrue("testAddRemoveUsersToGroup - Ensure users in group set contains second user added after adding second user again", userInGroup.contains(newUserTwo));

        try {
            this.accessDomainToTest.addUsersToGroup(null, userSet);
            fail("testAddRemoveUsersToGroup - Calling addUsersToGroup with null group argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.addUsersToGroup(accessGroup, null);
            fail("testAddRemoveUsersToGroup - Calling addUsersToGroup with null user set argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.removeUsersFromGroup(null, userSet);
            fail("testAddRemoveUsersToGroup - Calling removeUsersFromGroup with null group argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.addUsersToGroup(accessGroup, null);
            fail("testAddRemoveUsersToGroup - Calling removeUsersFromGroup with null user set argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#createAccessGroup(java.lang.String, java.lang.String)}.
     */
    public void testCreateAccessGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, GroupNotFoundException {
        String groupName = "foo";
        String description = "Check out this group";
        IAccessGroup accessGroup = this.accessDomainToTest.createAccessGroup(groupName, description);
        assertNotNull("testCreateAccessGroup - Ensure group created as expected.", accessGroup);
        assertEquals("testCreateAccessGroup - Ensure group created has name expected", groupName, accessGroup.getTitle());
        assertEquals("testCreateAccessGroup - Ensure group created has description expected", description, accessGroup.getDescription());

        // Retrieve the group and test it's properties again
        IAccessGroup retrievedAccessGroup = this.accessDomainToTest.getAccessGroup(groupName);
        assertNotNull("testCreateAccessGroup - Ensure group retrieved as expected.", retrievedAccessGroup);
        assertEquals("testCreateAccessGroup - Ensure group retrieved has name expected", groupName, retrievedAccessGroup.getTitle());
        assertEquals("testCreateAccessGroup - Ensure group retrieved has description expected", description, retrievedAccessGroup.getDescription());

        try {
            this.accessDomainToTest.createAccessGroup(groupName, "Another description");
            fail("testCreateAccessGroup - Calling create access group with name of existing group should throw GroupAlreadyExistsException");
        } catch (GroupAlreadyExistsException exception) {
        }

        // Try deleteing it and creating it again
        this.accessDomainToTest.deleteAccessGroup(retrievedAccessGroup);
        // Create group with same name in the second domain. Shouldn't cause a
        // problem
        IAccessGroup secondDomainGroup = this.secondDomain.createAccessGroup(groupName, description);
        accessGroup = this.accessDomainToTest.createAccessGroup(groupName, "Another description 2");

        try {
            this.accessDomainToTest.createAccessGroup(null, description);
            fail("testCreateAccessGroup - Calling createAccessGroup with null title should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.createAccessGroup(groupName, null);
            fail("testCreateAccessGroup - Calling createAccessGroup with null description should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#createNewUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     */
    public void testCreateNewUser() throws ApplicationUserCreationFailedException, UserAlreadyExistsException, ApplicationUserRepositoryAccessException, UserNotFoundException {
        String login = "MyLogin";
        String firstName = "MyFirstName";
        String lastName = "MyLastName";
        String password = "MyPassword";

        IApplicationUser createdUser = this.accessDomainToTest.createNewUser(login, firstName, lastName, password);
        assertNotNull("testCreateNewUser - Ensure create user is not null.", createdUser);
        assertEquals("testCreateNewUser - Ensure login as expected", login, createdUser.getLogin());
        assertEquals("testCreateNewUser - Ensure first name as expected", firstName, createdUser.getFirstName());
        assertEquals("testCreateNewUser - Ensure last name as expected", lastName, createdUser.getLastName());
        // FIX ME - Is there a way to test that the password was set?

        // Retrieve from DB and verify properties again
        IApplicationUser retrievedUser = this.accessDomainToTest.getUser(login);
        assertNotNull("testCreateNewUser - Ensure retrieved user is not null.", createdUser);
        assertEquals("testCreateNewUser - Ensure retrieved user's login as expected", login, retrievedUser.getLogin());
        assertEquals("testCreateNewUser - Ensure retrieved user's first name as expected", firstName, retrievedUser.getFirstName());
        assertEquals("testCreateNewUser - Ensure retrieved user's last name as expected", lastName, retrievedUser.getLastName());

        // Test UserAlreadyExistsException
        try {
            this.accessDomainToTest.createNewUser(login, "AnotherFirstName", "AnotherLastName", "AnotherPassword");
            fail("testCreateNewUser - Expected UserAlreadyExistsException when creating a user with the same login as an existing user.");
        } catch (UserAlreadyExistsException exception) {
        }

        try {
            this.accessDomainToTest.createNewUser(IApplicationUserManager.SUPER_USER_USERNAME, "DupSuperUser", "DupSuperUser", "DupSuperUser");
            fail("testCreateNewUser - Expected UserAlreadyExistsException when creating a user with the same login as the super user.");
        } catch (UserAlreadyExistsException exception) {
        }

        // Try deleteing it and creating it again
        this.accessDomainToTest.deleteApplicationUser(retrievedUser);
        // Create a user with same name in the second domain. Shouldn't cause a
        // problem
        IApplicationUser secondDomainUser = this.secondDomain.createNewUser(login, firstName, lastName, password);
        createdUser = this.accessDomainToTest.createNewUser(login, "AnotherFirstName", "AnotherLastName", "AnotherPassword");

        // test null pointer
        try {
            this.accessDomainToTest.createNewUser(null, firstName, lastName, password);
            fail("testCreateNewUser - Calling testCreateNewUser with null login should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.createNewUser("AnotherLogin", null, lastName, password);
            fail("testCreateNewUser - Calling testCreateNewUser with null first name should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.createNewUser("AnotherLogin", "Another First Name", null, password);
            fail("testCreateNewUser - Calling testCreateNewUser with null last name should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.createNewUser("AnotherLogin", "Another First Name", "Another Last Name", null);
            fail("testCreateNewUser - Calling testCreateNewUser with null last name should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#deleteAccessGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup)}.
     */
    public void testDeleteAccessGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, GroupNotFoundException {
        String groupName = "foo";
        String description = "Check out this group";
        IAccessGroup accessGroup = this.accessDomainToTest.createAccessGroup(groupName, description);
        assertNotNull("testDeleteAccessGroup - Ensure group created as expected.", accessGroup);

        // Create group in second domain to make sure it doesn't cause any
        // problems
        IAccessGroup secondDomainGroup = this.secondDomain.createAccessGroup(groupName, description);

        this.accessDomainToTest.deleteAccessGroup(accessGroup);

        // Make sure we can't retrieve it
        try {
            this.accessDomainToTest.getAccessGroup(groupName);
            fail("testDeleteAccessGroup - Expected GroupNotFoundException after group was deleted.");
        } catch (GroupNotFoundException exception) {
        }

        // Make sure we can retrieve second domain group
        IAccessGroup secondDomainGroupRetrieved = this.secondDomain.getAccessGroup(groupName);
        assertEquals("testDeleteAccessGroup - Ensure second domain group is not affected by delete", secondDomainGroup, secondDomainGroupRetrieved);

        // Make sure trying to delete is again doesn't cause any problems
        // FIX ME - Doesn't work in Hibernate. ARG!
        // this.accessDomainToTest.deleteAccessGroup(accessGroup);

        try {
            this.accessDomainToTest.deleteAccessGroup(null);
            fail("testDeleteAccessGroup - Calling deleteAccessGroup with null group should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#deleteApplicationUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser)}.
     */
    public void testDeleteApplicationUser() throws ApplicationUserCreationFailedException, UserAlreadyExistsException, ApplicationUserRepositoryAccessException, UserNotFoundException {
        String login = "MyLogin";
        String firstName = "MyFirstName";
        String lastName = "MyLastName";
        String password = "MyPassword";
         
        IApplicationUser createdUser = this.accessDomainToTest.createNewUser(login, firstName, lastName, password);
        assertNotNull("testCreateNewUser - Ensure create user is not null.", createdUser);

        // Create user in second domain to make sure if it not affected
        IApplicationUser secondDomainUser = this.secondDomain.createNewUser(login, firstName, lastName, password);

        this.accessDomainToTest.deleteApplicationUser(createdUser);

        // Try to retrieve it
        try {
            this.accessDomainToTest.getUser(login);
            fail("testDeleteApplicationUser - Expected UserNotFoundException");
        } catch (UserNotFoundException exception) {
        }

        // Try to retrieve second domain user
        IApplicationUser retrievedSecondDomainUser = this.secondDomain.getUser(login);
        assertEquals("testDeleteApplicationUser - Ensure second domain user not affected by delete", secondDomainUser, retrievedSecondDomainUser);

        // Try with Super User
        IApplicationUser superUser = this.userRepository.getSuperUser();
        try {
            this.accessDomainToTest.deleteApplicationUser(superUser);
            fail("testDeleteApplicationUser - Calling deleteApplicationUser with super user should throw IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            
        }

        // Test NPE
        try {
            this.accessDomainToTest.deleteApplicationUser(null);
            fail("testDeleteApplicationUser - Calling deleteApplicationUser with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#getAccessGroup(java.lang.String)}.
     */
    public void testGetAccessGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, GroupNotFoundException {
        String groupName = "foo";
        String description = "Check out this group";
        IAccessGroup accessGroup = this.accessDomainToTest.createAccessGroup(groupName, description);
        assertNotNull("testGetAccessGroup - Ensure group created as expected.", accessGroup);

        // Create second domain group to make sure there is not conflict
        IAccessGroup secondDomainGroup = this.secondDomain.createAccessGroup(groupName, description);

        // Retrieve the group and test it's properties again
        IAccessGroup retrievedAccessGroup = this.accessDomainToTest.getAccessGroup(groupName);
        assertNotNull("testGetAccessGroup - Ensure group retrieved as expected.", retrievedAccessGroup);
        assertEquals("testGetAccessGroup - Ensure group retrieved has name expected", groupName, retrievedAccessGroup.getTitle());
        assertEquals("testGetAccessGroup - Ensure group retrieved has description expected", description, retrievedAccessGroup.getDescription());

        // Delete it and try to retrieve it
        this.accessDomainToTest.deleteAccessGroup(accessGroup);
        try {
            this.accessDomainToTest.getAccessGroup(groupName);
            fail("testGetAccessGroup - Calling get access group with name of non-existant group should throw GroupNotFoundException");
        } catch (GroupNotFoundException exception) {
        }

        try {
            this.accessDomainToTest.getAccessGroup(null);
            fail("testGetAccessGroup - Calling getAccessGroup with null group name should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#getApplicationUsers()}.
     */
    public void testGetApplicationUsers() throws ApplicationUserRepositoryAccessException, ApplicationUserCreationFailedException, UserAlreadyExistsException, UserNotFoundException {
        // Ensure it's initially empty
        SortedSet applicationUsers = this.accessDomainToTest.getApplicationUsers();
        assertTrue("testGetApplicationUsers - ", applicationUsers.isEmpty());

        // Create a second domain user to make sure it is not retrieved
        IApplicationUser secondDomainUser = this.secondDomain.createNewUser("login", "firstName", "lastName", "password");

        // Add a user and try again
        IApplicationUser createdUser = this.accessDomainToTest.createNewUser("login", "firstName", "lastName", "password");
        applicationUsers = this.accessDomainToTest.getApplicationUsers();
        assertEquals("testGetApplicationUsers - Ensure application users retrieve is of size 1", 1, applicationUsers.size());
        assertTrue("testGetApplicationUsers - Ensure application users retrieve contains created user", applicationUsers.contains(createdUser));

        // create another one
        IApplicationUser secondCreatedUser = this.accessDomainToTest.createNewUser("Anotherlogin", "anotherFirstName", "anotherLastName", "anotherPassword");
        applicationUsers = this.accessDomainToTest.getApplicationUsers();
        assertEquals("testGetApplicationUsers - Ensure application users retrieve is of size 2", 2, applicationUsers.size());
        assertTrue("testGetApplicationUsers - Ensure application users retrieve contains first created user", applicationUsers.contains(createdUser));
        assertTrue("testGetApplicationUsers - Ensure application users retrieve contains second created user", applicationUsers.contains(secondCreatedUser));

        // delete them both and make sure users returned is empty
        this.accessDomainToTest.deleteApplicationUser(createdUser);
        this.accessDomainToTest.deleteApplicationUser(secondCreatedUser);
        applicationUsers = this.accessDomainToTest.getApplicationUsers();
        assertTrue("testGetApplicationUsers - Ensure applicaiton users is empty", applicationUsers.isEmpty());

    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#getLinkedGroupByExternalId(byte[])}.
     */
    public void testGetLinkedGroupByExternalId() throws GroupAlreadyExistsException, GroupLinkAlreadyExistsException, ApplicationUserRepositoryAccessException, GroupNotFoundException {
        String groupName = "foo";
        byte[] externalId = { 0x5 };
        IAccessGroup accessGroup = this.accessDomainToTest.linkExternalGroup(externalId, groupName);
        assertNotNull("testGetLinkedGroupByExternalId - Ensure group created as expected.", accessGroup);

        // Retrieve the group by external id and test it's properties again
        IAccessGroup retrievedAccessGroup = this.accessDomainToTest.getLinkedGroupByExternalId(externalId);
        assertNotNull("testGetLinkedGroupByExternalId - Ensure group retrieved as expected.", retrievedAccessGroup);
        assertEquals("testGetLinkedGroupByExternalId- Ensure group retrieved has name expected", groupName, retrievedAccessGroup.getTitle());
        assertTrue("testGetLinkedGroupByExternalId - Ensure group retrieved has external ID expected", Arrays.equals(externalId, ((ILinkedAccessGroup) retrievedAccessGroup).getExternalId()));

        try {
            byte[] anotherExternalId = { 20 };
            this.accessDomainToTest.getLinkedGroupByExternalId(anotherExternalId);
            fail("testGetLinkedGroupByExternalId - Calling getLinkedGroupByExternalId with non-existent external group id should throw GroupNotFoundException");
        } catch (GroupNotFoundException exception) {
        }

        try {
            this.accessDomainToTest.getLinkedGroupByExternalId(null);
            fail("testGetLinkedGroupByExternalId - Calling getLinkedGroupByExternalId with null external Id should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#getUser(java.lang.String)}.
     */
    public void testGetUser() throws ApplicationUserCreationFailedException, UserAlreadyExistsException, ApplicationUserRepositoryAccessException, UserNotFoundException {
        String login = "MyLogin";
        String firstName = "MyFirstName";
        String lastName = "MyLastName";
        String password = "MyPassword";

        IApplicationUser createdUser = this.accessDomainToTest.createNewUser(login, firstName, lastName, password);
        assertNotNull("testGetUser - Ensure create user is not null.", createdUser);

        // Create second user to make sure there is not conflict
        IApplicationUser secondDomainUser = this.secondDomain.createNewUser(login, firstName, lastName, password);

        IApplicationUser retrievedUser = this.accessDomainToTest.getUser(login);
        assertNotNull("testGetUser - Ensure retrieved user is not null.", createdUser);
        assertEquals("testGetUser - Ensure retrieved user's login as expected", login, retrievedUser.getLogin());
        assertEquals("testGetUser - Ensure retrieved user's first name as expected", firstName, retrievedUser.getFirstName());
        assertEquals("testGetUser - Ensure retrieved user's last name as expected", lastName, retrievedUser.getLastName());

        // delete it and make sure we get user not found exception
        this.accessDomainToTest.deleteApplicationUser(retrievedUser);
        try {
            this.accessDomainToTest.getUser(login);
            fail("testGetUser - Expected UserNotFoundException.");
        } catch (UserNotFoundException exception) {
        }

        try {
            this.accessDomainToTest.getUser(null);
            fail("testGetUser - Calling getUser with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#importExternalUser(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser)}.
     * 
     * @throws ApplicationUserCreationFailedException
     */
    public void testImportExternalUser() throws ApplicationUserImportFailedException, UserAlreadyExistsException, ApplicationUserRepositoryAccessException, UserNotFoundException, ApplicationUserCreationFailedException {
        IExternalUser userToImport = new MockExternalUser();
        IApplicationUser importedUser = this.accessDomainToTest.importExternalUser(userToImport);
        assertNotNull("testImportExternalUser - Ensure imported user it not null", importedUser);
        assertEquals("testImportExternalUser - Ensure login of imported user as expected", importedUser.getLogin(), userToImport.getLogin());
        assertEquals("testImportExternalUser - Ensure first name of imported user as expected", importedUser.getFirstName(), userToImport.getFirstName());
        assertEquals("testImportExternalUser - Ensure last name of imported user as expected", importedUser.getLastName(), userToImport.getLastName());

        IApplicationUser retrievedUser = this.accessDomainToTest.getUser(userToImport.getLogin());
        assertEquals("testImportExternalUser - Ensure imported user can be retrieve from database", importedUser, retrievedUser);

        try {
            this.accessDomainToTest.importExternalUser(userToImport);
            fail("testImportExternalUser - Expected to catch UserAlreadyExistsException");
        } catch (UserAlreadyExistsException exception) {
        }

        // Delete it and try again
        this.accessDomainToTest.deleteApplicationUser(retrievedUser);

        // Create second user in second domain to test for conflict
        IApplicationUser secondDomainUser = this.secondDomain.createNewUser(userToImport.getLogin(), userToImport.getFirstName(), userToImport.getLastName(), "password");

        importedUser = this.accessDomainToTest.importExternalUser(userToImport);

        try {
            this.accessDomainToTest.importExternalUser(null);
            fail("testImportExternalUser - Calling importExternalUser with null argument should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#linkExternalGroup(byte[], java.lang.String)}.
     */
    public void testLinkExternalGroup() throws GroupAlreadyExistsException, GroupLinkAlreadyExistsException, ApplicationUserRepositoryAccessException, GroupNotFoundException {
        String groupName = "foo";
        byte[] externalId = { 5 };
        IAccessGroup accessGroup = this.accessDomainToTest.linkExternalGroup(externalId, groupName);
        assertNotNull("testLinkExternalGroup - Ensure group created as expected.", accessGroup);
        assertEquals("testLinkExternalGroup - Ensure group created has name expected", groupName, accessGroup.getTitle());
        assertEquals("testLinkExternalGroup - Ensure group created has external ID expected", externalId, ((ILinkedAccessGroup) accessGroup).getExternalId());

        // Retrieve the group and test it's properties again
        IAccessGroup retrievedAccessGroup = this.accessDomainToTest.getAccessGroup(groupName);
        assertNotNull("testLinkExternalGroup - Ensure group retrieved as expected.", retrievedAccessGroup);
        assertEquals("testLinkExternalGroup - Ensure group retrieved has name expected", groupName, retrievedAccessGroup.getTitle());
        assertTrue("testLinkExternalGroup - Ensure group retrieved has external ID expected", Arrays.equals(externalId, ((ILinkedAccessGroup) retrievedAccessGroup).getExternalId()));

        try {
            this.accessDomainToTest.linkExternalGroup(externalId, "Another Name");
            //TODO need to fix bug 6403 first
            // fail("testLinkExternalGroup - Calling link access group with external id of existing group should throw GroupAlreadyExistsException");
        } catch (GroupAlreadyExistsException exception) {
        }

        try {
            byte[] anotherExternalId = { 20 };
            this.accessDomainToTest.linkExternalGroup(anotherExternalId, groupName);
            fail("testLinkExternalGroup - Calling link access group with name of existing group should throw GroupAlreadyExistsException");
        } catch (GroupAlreadyExistsException exception) {
        }

        // Try deleteing it and creating it again
        this.accessDomainToTest.deleteAccessGroup(retrievedAccessGroup);

        // Create a second group and make sure there is not conflict
        IAccessGroup secondDomainGroup = this.secondDomain.createAccessGroup(groupName, "description");

        accessGroup = this.accessDomainToTest.linkExternalGroup(externalId, groupName);

        try {
            this.accessDomainToTest.linkExternalGroup(null, "Another Title 2");
            fail("testLinkExternalGroup - Calling linkExternalGroup with null external Id should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            byte[] anotherExternalId = { 50 };
            this.accessDomainToTest.linkExternalGroup(anotherExternalId, null);
            fail("testLinkExternalGroup - Calling createAccessGroup with null name should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#setApplicableAccessControlForGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup, java.lang.String)}.
     */
    public void testSetApplicableAccessControlForGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, GroupNotFoundException {
        String groupName = "foo";
        String description = "Check out this group";
        IAccessGroup accessGroup = this.accessDomainToTest.createAccessGroup(groupName, description);
        assertNotNull("testSetApplicableAccessControlForGroup - Ensure group created as expected.", accessGroup);
        assertEquals("testSetApplicableAccessControlForGroup - Ensure original access control is empty", "access_control ", accessGroup.getApplicableAccessControl());

        String newApplicableAccessControl = "MyFancyNewAccessControl";
        this.accessDomainToTest.setApplicableAccessControlForGroup(accessGroup, newApplicableAccessControl);
        assertEquals("testSetApplicableAccessControlForGroup - Ensure new access control as expected", newApplicableAccessControl, accessGroup.getApplicableAccessControl());

        // Try retrieving it and testing new description
        IAccessGroup retrievedAccessGroup = this.accessDomainToTest.getAccessGroup(groupName);
        assertEquals("testSetApplicableAccessControlForGroup - Ensure new access control as expected on retrieved group.", newApplicableAccessControl, retrievedAccessGroup.getApplicableAccessControl());

        try {
            this.accessDomainToTest.setApplicableAccessControlForGroup(null, "sdkfhksdhfds");
            fail("testSetApplicableAccessControlForGroup - Calling setApplicableAccessControlForGroup with null group should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.setApplicableAccessControlForGroup(accessGroup, null);
            fail("testSetApplicableAccessControlForGroup - Calling setApplicableAccessControlForGroup with null access control should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#setDescriptionForGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup, java.lang.String)}.
     */
    public void testSetDescriptionForGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, GroupNotFoundException {
        String groupName = "foo";
        String description = "Check out this group";
        IAccessGroup accessGroup = this.accessDomainToTest.createAccessGroup(groupName, description);
        assertNotNull("testSetDescriptionForGroup - Ensure group created as expected.", accessGroup);
        assertEquals("testSetDescriptionForGroup - Ensure original descriptiorn as expected", description, accessGroup.getDescription());

        String newDescription = "MyFancyNewDescription";
        this.accessDomainToTest.setDescriptionForGroup(accessGroup, newDescription);
        assertEquals("testSetDescriptionForGroup - Ensure new description as expected", newDescription, accessGroup.getDescription());

        // Try retrieving it and testing new description
        IAccessGroup retrievedAccessGroup = this.accessDomainToTest.getAccessGroup(groupName);
        assertEquals("testSetDescriptionForGroup - Ensure new description as expected on retrieved group.", newDescription, retrievedAccessGroup.getDescription());

        try {
            this.accessDomainToTest.setDescriptionForGroup(null, "sdkfhksdhfds");
            fail("testSetDescriptionForGroup - Calling setDescriptionForGroup with null group should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.setDescriptionForGroup(accessGroup, null);
            fail("testSetDescriptionForGroup - Calling setDescriptionForGroup with null description should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#updateUser(IApplicationUser, java.lang.String)}.
     */
    public void testUpdateUser() throws ApplicationUserCreationFailedException, UserAlreadyExistsException, ApplicationUserRepositoryAccessException, UserNotFoundException {
        String login = "MyLogin";
        String firstName = "MyFirstName";
        String lastName = "MyLastName";
        String password = "MyPassword";

        IApplicationUser createdUser = this.accessDomainToTest.createNewUser(login, firstName, lastName, password);
        assertNotNull("testUpdateUser - Ensure create user is not null.", createdUser);
        assertEquals("testUpdateUser - Ensure login as expected", login, createdUser.getLogin());
        assertEquals("testUpdateUser - Ensure first name as expected", firstName, createdUser.getFirstName());
        assertEquals("testUpdateUser - Ensure last name as expected", lastName, createdUser.getLastName());

        // Try updating it
        String updatedFirstName = "MyFirstNameUpdated";
        String updatedLastName = "MyLastNameUpdated";
        IApplicationUser updateUser = new UpdateApplicationUser(login, updatedFirstName, updatedLastName);
        this.accessDomainToTest.updateUser(updateUser, null);
        IApplicationUser retrievedUser = this.accessDomainToTest.getUser(login);
        assertEquals("testUpdateUser - Ensure first name set as expected", updatedFirstName, retrievedUser.getFirstName());
        assertEquals("testUpdateUser - Ensure last name as expected", updatedLastName, retrievedUser.getLastName());

        // Try updating just the first name
        String secondUpdateOfFirstName = "MyFirstNameUpdatedAgain";
        updateUser = new UpdateApplicationUser(login, secondUpdateOfFirstName, null);
        this.accessDomainToTest.updateUser(updateUser, null);
        retrievedUser = this.accessDomainToTest.getUser(login);
        assertEquals("testUpdateUser - Ensure first name set as expected on second update", secondUpdateOfFirstName, retrievedUser.getFirstName());
        assertEquals("testUpdateUser - Ensure last name not updated as expected on second update", updatedLastName, retrievedUser.getLastName());

        // Now, try updating the password
        String updatedPassword = "MyPasswordUpdated";
        this.accessDomainToTest.updateUser(updateUser, updatedPassword);
        // FIX ME - How do we test if the password was updated!!!!!!!!!!

        // Make sure we can update the super user password
        IApplicationUser superUser = this.userRepository.getSuperUser();
        this.accessDomainToTest.updateUser(superUser, "foobar");
        //set it back for other tests
        this.accessDomainToTest.updateUser(superUser, "123blue!");
        
        // test user not found exception
        updateUser = new UpdateApplicationUser("foobar", null, null);
        try {
            this.accessDomainToTest.updateUser(updateUser, null);
            fail("testUpdateUser - Calling updateUser with bad user should throw UserNotFoundException");
        } catch (UserNotFoundException exception) {
        }

        // test null pointer
        try {
            this.accessDomainToTest.updateUser(null, password);
            fail("testUpdateUser - Calling updateUser with null user should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#setPrimaryAccessGroupForUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser, com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup)}.
     */
    public void testSetPrimaryAccessGroupForUser() throws ApplicationUserCreationFailedException, UserAlreadyExistsException, ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, IllegalStateException, UserManagementAccessException {
        IApplicationUser appUser = this.accessDomainToTest.createNewUser("MyLogin", "MyFirstName", "MyLastName", "MyPassword");
        assertFalse("testSetPrimaryAccessGroupForUser - Ensure app user initially doesn't have primary access group", ((BaseApplicationUserDO) appUser).hasPrimaryAccessGroup());

        IAccessGroup accessGroup = this.accessDomainToTest.createAccessGroup("MyTitle", "MyDescription");
        this.accessDomainToTest.setPrimaryAccessGroupForUser(appUser, accessGroup);

        assertTrue("testSetPrimaryAccessGroupForUser - Ensure user now has primary access group", ((BaseApplicationUserDO) appUser).hasPrimaryAccessGroup());
        assertEquals("testSetPrimaryAccessGroupForUser - Ensure primary access group set as expected", accessGroup, ((BaseApplicationUserDO) appUser).getPrimaryAccessGroup());

        // Now, clear the primary access group
        this.accessDomainToTest.setPrimaryAccessGroupForUser(appUser, null);
        assertFalse("testSetPrimaryAccessGroupForUser - Ensure app user doesn't have primary access group after clearing it", ((BaseApplicationUserDO) appUser).hasPrimaryAccessGroup());

        try {
            this.accessDomainToTest.setPrimaryAccessGroupForUser(null, accessGroup);
            fail("testSetPrimaryAccessGroupForUser - Calling testSetPrimaryAccessGroupForUser with null user should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO#setTitleForGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup, java.lang.String)}.
     */
    public void testSetTitleForGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, GroupNotFoundException {
        String groupName = "foo";
        String description = "Check out this group";
        IAccessGroup accessGroup = this.accessDomainToTest.createAccessGroup(groupName, description);
        assertNotNull("testSetTitleForGroup - Ensure group created as expected.", accessGroup);
        assertEquals("testSetTitleForGroup - Ensure original title as expected", groupName, accessGroup.getTitle());

        String newTitle = "MyFancyNewTitle";
        this.accessDomainToTest.setTitleForGroup(accessGroup, newTitle);
        assertEquals("testSetTitleForGroup - Ensure new title as expected", newTitle, accessGroup.getTitle());

        // Try retrieving it and testing new title
        IAccessGroup retrievedAccessGroup = this.accessDomainToTest.getAccessGroup(newTitle);
        assertEquals("testSetTitleForGroup - Ensure new title as expected on retrieved group.", newTitle, retrievedAccessGroup.getTitle());

        try {
            this.accessDomainToTest.setTitleForGroup(null, "sdkfhksdhfds");
            fail("testSetTitleForGroup - Calling setTitleForGroup with null group should throw NullPointerException");
        } catch (NullPointerException exception) {
        }

        try {
            this.accessDomainToTest.setTitleForGroup(accessGroup, null);
            fail("testSetTitleForGroup - Calling setTitleForGroup with null title should throw NullPointerException");
        } catch (NullPointerException exception) {
        }
    }

    private class MockExternalUser implements IExternalUser {

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getDisplayName()
         */
        public String getDisplayName() {
            // TODO Auto-generated method stub
            return getLastName() + ", " + getFirstName();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getFirstName()
         */
        public String getFirstName() {
            return "Jimmy";
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLastName()
         */
        public String getLastName() {
            return "Carter";
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLogin()
         */
        public String getLogin() {
            return "jimmy.carter";
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getUniqueName()
         */
        public String getUniqueName() {
            return getLogin() + "@" + getDomainName();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomainEntity#getDomainName()
         */
        public String getDomainName() {
            return "test.bluejungle.com";
        }
    }

    private class UpdateApplicationUser implements IApplicationUser {

        private String login;
        private String firstName;
        private String lastName;

        /**
         * Create an instance of UpdateApplicationUser
         * 
         * @param firstName
         * @param lastName
         */
        private UpdateApplicationUser(String login, String firstName, String lastName) {
            super();
            this.login = login;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#getDestinyId()
         */
        public Long getDestinyId() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#getPrimaryAccessGroup()
         */
        public IAccessGroup getPrimaryAccessGroup() throws IllegalStateException, UserManagementAccessException {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#hasPrimaryAccessGroup()
         */
        public boolean hasPrimaryAccessGroup() {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#isManuallyCreated()
         */
        public boolean isManuallyCreated() {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getDisplayName()
         */
        public String getDisplayName() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getFirstName()
         */
        public String getFirstName() {
            return this.firstName;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLastName()
         */
        public String getLastName() {
            return this.lastName;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLogin()
         */
        public String getLogin() {
            return this.login;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getUniqueName()
         */
        public String getUniqueName() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomainEntity#getDomainName()
         */
        public String getDomainName() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
