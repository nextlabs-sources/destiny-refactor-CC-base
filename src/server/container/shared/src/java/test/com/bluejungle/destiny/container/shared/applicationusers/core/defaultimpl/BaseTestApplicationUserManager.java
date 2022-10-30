/*
 * Created on Sep 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import com.bluejungle.destiny.container.shared.applicationusers.core.AccessGroupCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserImportFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.remoteevent.ActionTypeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.remoteevent.ChangeEventUtil;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/TestApplicationUserManager.java#1 $
 */

public abstract class BaseTestApplicationUserManager extends BaseContainerSharedTestCase {

    private IHibernateRepository dataSourceForTest;
    protected IApplicationUserManager appUserManagerToTest;
    private MockEventManagerImpl mockEventManagerForTest;
    protected Set usersToCleanup = new HashSet();
    private Set groupsToCleanup = new HashSet();
    private MockUserManagementObserverImpl mockObserverForTest = new MockUserManagementObserverImpl();

    public static void main(String[] args) {
        junit.textui.TestRunner.run(BaseTestApplicationUserManager.class);
    }

    /**
     * Constructor for TestApplicationUserManager.
     * 
     * @param name
     */
    public BaseTestApplicationUserManager(String name) {
        super(name);
    }

    /*
     * The setup task also verifies at the end that the appropriate DCC event
     * has been setup/registered-for by the user mgmt system
     * 
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        // Debug.setTrace(Debug.all, true);

        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        this.dataSourceForTest = (IHibernateRepository) manager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        // Setup the mock shared context locator and event managers:
        ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocatorImpl.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) manager.getComponent(locatorInfo);
        this.mockEventManagerForTest = (MockEventManagerImpl) locator.getSharedContext().getEventManager();

        // Setup the user management component:
        HashMapConfiguration applicationUserManagerFactoryConfig = new HashMapConfiguration();
        applicationUserManagerFactoryConfig.setProperty(IApplicationUserManagerFactory.APPLICATION_USER_CONFIGURATION, new MockApplicationUserManagerConfigurationImpl(getAuthenticationModeForTest()));
        applicationUserManagerFactoryConfig.setProperty(IApplicationUserManagerFactory.MANAGEMENT_REPOSITORY, this.dataSourceForTest);
        IApplicationUserManagerFactory appUserMgrFactory = (IApplicationUserManagerFactory) manager.getComponent(ApplicationUserManagerFactoryImpl.class, applicationUserManagerFactoryConfig);
        this.appUserManagerToTest = appUserMgrFactory.getSingleton();

        // Register the observer:
        this.appUserManagerToTest.addListener(this.mockObserverForTest);

        // Verify that the appropriate DCC event was registered for:
        assertNotNull("An event should have been registered for", this.mockEventManagerForTest.getLastEventRegisteredFor());
        assertNotNull("An event should have been registered for by someone", this.mockEventManagerForTest.getLastEventRegisteredBy());
        assertEquals("An event of the right type should have been registered for", ChangeEventUtil.EVENT_NAME, this.mockEventManagerForTest.getLastEventRegisteredFor());
        assertSame("The right listener should have registered for the event", this.appUserManagerToTest, this.mockEventManagerForTest.getLastEventRegisteredBy());
    }

    /**
     * Determines whether this test case corresponds to local/hybrid auth
     * 
     * @return type of authentication being used
     */
    protected abstract AuthenticationModeEnumType getAuthenticationModeForTest();

    /**
     * Tests the initialization of the application user manager component
     *  
     */
    public void testInitialState() throws Exception {
        assertNotNull("User management component should not be null", this.appUserManagerToTest);
        assertEquals("User management component should be of the right class", this.appUserManagerToTest.getClass(), ApplicationUserManagerImpl.class);
        assertNotNull("Default domain name should never be null", this.appUserManagerToTest.getDefaultLocalDomainName());
        assertEquals("Default domain name is wrong", this.appUserManagerToTest.getDefaultLocalDomainName(), "Local");

        Collection existingAccessGroups = this.appUserManagerToTest.getAccessGroups(null, 0);
        assertNotNull("Collection of access groups should never be null", existingAccessGroups);
        assertEquals("No access groups should exist in initial state", 0, existingAccessGroups.size());

        Collection existingApplicationUsers = this.appUserManagerToTest.getApplicationUsers(null, 0);
        assertNotNull("Collection of existing users should never be null", existingApplicationUsers);
        assertEquals("No users must exist at this stage", 0, existingApplicationUsers.size());

        Collection existingDomainNames = this.appUserManagerToTest.getDomainNames();
        assertNotNull("Collection of domain names should never be null");
        assertTrue("At least one domain must exist in initial state", existingDomainNames.size() > 0);
        assertTrue("The defalut local domain must exist", existingDomainNames.contains(this.appUserManagerToTest.getDefaultLocalDomainName()));

        // Make sure superuser is in the default local domain:
        IApplicationUserDomain localDomain = this.appUserManagerToTest.getApplicationUserDomain(this.appUserManagerToTest.getDefaultLocalDomainName());
        assertNotNull("Local domain should not be null", localDomain);
        assertEquals("Local domain shoudl have the right name", localDomain.getName(), this.appUserManagerToTest.getDefaultLocalDomainName());

        // Retrieve the super user and verify that all types of retrieval return
        // an equivalent object:
        IApplicationUser superUser = this.appUserManagerToTest.getApplicationUser(IApplicationUserManager.RESERVED_SUPER_USER_ID);
        assertNotNull("Super user entry retrieved via login should not be null", superUser);
        assertNotNull("Super user must have an id", superUser.getDestinyId());
        IApplicationUser superUser2 = this.appUserManagerToTest.getApplicationUser(superUser.getUniqueName());
        assertNotNull("Super user entry retrieved via login should not be null", superUser2);
        assertNotNull("Super user must have an id", superUser2.getDestinyId());
        assertTrue("All super user entries retrieved must be equivalent", superUser.equals(superUser2));

        // Make sure nobody else is in the default domain:
        Collection usersInLocalDomain = localDomain.getApplicationUsers();
        assertNotNull("Collection of users in local domain should not be null", usersInLocalDomain);
        assertEquals("No user must exist in the local domain", 0, usersInLocalDomain.size());
    }

    /**
     * Tests the creation of an internal group and retrieval by different
     * mechanism. Also verify uniqueness enforcement.
     * 
     * @throws Exception
     */
    public void testInternalGroupCreation() throws Exception {
        String GROUP_NAME = "InternalTestGroup1";
        String GROUP_DESCRIPTION = "Description for " + GROUP_NAME;
        String ACL = "Test ACL for " + GROUP_NAME;
        IInternalAccessGroup group = this.appUserManagerToTest.createAccessGroup(GROUP_NAME, GROUP_DESCRIPTION);
        this.groupsToCleanup.add(group);

        // Verify group contents:
        assertNotNull("Created group should not be null", group);
        assertNotNull("Group should have a non-null title", group.getTitle());
        assertEquals("Group should have the right title", GROUP_NAME, group.getTitle());
        assertTrue("Access control for group should be null or empty", ((group.getApplicableAccessControl() == null) || group.getApplicableAccessControl().equals("")));
        assertEquals("Group should have the right description", GROUP_DESCRIPTION, group.getDescription());
        assertNotNull("Group should have a non-null destiny id", group.getDestinyId());
        assertNotNull("Group should have a non-null domain name", group.getDomainName());
        assertEquals("Group should be in the local domain", this.appUserManagerToTest.getDefaultLocalDomainName(), group.getDomainName());

        // Set some random access control on this group:
        this.appUserManagerToTest.setDefaultAccessControlAssignmentForGroup(group.getDestinyId().longValue(), ACL);

        // Now query for the group again and verify all the fields again:
        IAccessGroup groupRetrievedViaID = this.appUserManagerToTest.getAccessGroup(group.getDestinyId().longValue());
        assertNotNull("Re-query for newly created group should return a non-null group entry", groupRetrievedViaID);
        assertTrue("Group should be an internal group", (groupRetrievedViaID instanceof IInternalAccessGroup));
        assertNotNull("Group should have a non-null title", groupRetrievedViaID.getTitle());
        assertEquals("Group should have the right title", group.getTitle(), groupRetrievedViaID.getTitle());
        assertNotNull("Access control for group should not null or empty", groupRetrievedViaID.getApplicableAccessControl());
        assertEquals("Access control for group should be as expected", ACL, groupRetrievedViaID.getApplicableAccessControl());
        assertEquals("Group should have the right description", group.getDescription(), groupRetrievedViaID.getDescription());
        assertNotNull("Group should have a non-null destiny id", groupRetrievedViaID.getDestinyId());
        assertNotNull("Group should have a non-null domain name", groupRetrievedViaID.getDomainName());
        assertEquals("Group should be in the local domain", group.getDomainName(), groupRetrievedViaID.getDomainName());
        assertEquals("Groups must be equivalent", group, groupRetrievedViaID);

        // Now re-set the access control on this group to null:
        this.appUserManagerToTest.setDefaultAccessControlAssignmentForGroup(group.getDestinyId().longValue(), null);

        // Now query for the group again and verify all the fields again:
        IAccessGroup groupRetrievedViaSearchSpec = (IAccessGroup) this.appUserManagerToTest.getAccessGroups(new IGroupSearchSpec[] { new GroupSearchSpecImpl(GROUP_NAME) }, 0).iterator().next();
        assertNotNull("Re-query for newly created group should return a non-null group entry", groupRetrievedViaSearchSpec);
        assertTrue("Group should be an internal group", (groupRetrievedViaSearchSpec instanceof IInternalAccessGroup));
        assertNotNull("Group should have a non-null title", groupRetrievedViaSearchSpec.getTitle());
        assertEquals("Group should have the right title", group.getTitle(), groupRetrievedViaSearchSpec.getTitle());
        assertTrue("Access control for group should be null or empty", ((groupRetrievedViaSearchSpec.getApplicableAccessControl() == null) || groupRetrievedViaSearchSpec.getApplicableAccessControl().equals("")));
        assertEquals("Group should have the right description", group.getDescription(), groupRetrievedViaSearchSpec.getDescription());
        assertNotNull("Group should have a non-null destiny id", groupRetrievedViaSearchSpec.getDestinyId());
        assertNotNull("Group should have a non-null domain name", groupRetrievedViaSearchSpec.getDomainName());
        assertEquals("Group should be in the local domain", group.getDomainName(), groupRetrievedViaSearchSpec.getDomainName());
        assertEquals("Groups must be equivalent", group, groupRetrievedViaSearchSpec);

        // Try creating the same group again:
        try {
            IInternalAccessGroup groupTest2 = this.appUserManagerToTest.createAccessGroup(GROUP_NAME, GROUP_DESCRIPTION);
            this.groupsToCleanup.add(groupTest2);
            fail("Should not be able to re-create a group with the same title: '" + GROUP_NAME + "'");
        } catch (GroupAlreadyExistsException ignore) {
        }
    }

    /**
     * Tests the creation of an internal user and retrieval by different
     * mechanism. Also verify uniqueness enforcement.
     * 
     * @throws Exception
     */
    public void testInternalUserCreation() throws Exception {
        String FN1 = "FN1";
        String LN1 = "LN1";
        String LOGIN1 = "LOGIN1";
        String PASSWORD1 = "PWD1";
        IApplicationUser user = this.appUserManagerToTest.createUser(LOGIN1, FN1, LN1, PASSWORD1);
        this.usersToCleanup.add(user);

        // Verify group contents:
        assertNotNull("Created user should not be null", user);
        assertNotNull("User should have a non-null login", user.getLogin());
        assertEquals("User should have the right login", LOGIN1, user.getLogin());
        assertEquals("User should have the right first name", FN1, user.getFirstName());
        assertEquals("User should have the right last name", LN1, user.getLastName());
        assertNotNull("User should have a non-null destiny id", user.getDestinyId());
        assertNotNull("User should have a non-null domain name", user.getDomainName());
        assertEquals("User should be in the local domain", this.appUserManagerToTest.getDefaultLocalDomainName(), user.getDomainName());

        // Now query for the group again and verify all the fields again:
        IApplicationUser userRetrievedViaID = this.appUserManagerToTest.getApplicationUser(user.getDestinyId().longValue());
        assertNotNull("Re-query for newly created user should return a non-null user entry", userRetrievedViaID);
        assertNotNull("User should have a non-null title", userRetrievedViaID.getLogin());
        assertEquals("User should have the right title", user.getLogin(), userRetrievedViaID.getLogin());
        assertEquals("User should have the right first name", user.getFirstName(), userRetrievedViaID.getFirstName());
        assertEquals("User should have the right last name", user.getLastName(), userRetrievedViaID.getLastName());
        assertNotNull("User should have a non-null destiny id", userRetrievedViaID.getDestinyId());
        assertNotNull("User should have a non-null domain name", userRetrievedViaID.getDomainName());
        assertEquals("User should be in the local domain", user.getDomainName(), userRetrievedViaID.getDomainName());
        assertEquals("Users must be equivalent", user, userRetrievedViaID);

        // Now query for the group again and verify all the fields again:
        IApplicationUser userRetrievedViaSearchSpec = (IApplicationUser) this.appUserManagerToTest.getApplicationUsers(new IUserSearchSpec[] { new UserSearchSpecImpl(LN1) }, 0).iterator().next();
        assertNotNull("Re-query for newly created user should return a non-null user entry", userRetrievedViaSearchSpec);
        assertNotNull("User should have a non-null title", userRetrievedViaSearchSpec.getLogin());
        assertEquals("User should have the right title", user.getLogin(), userRetrievedViaSearchSpec.getLogin());
        assertEquals("User should have the right first name", user.getFirstName(), userRetrievedViaSearchSpec.getFirstName());
        assertEquals("User should have the right last name", user.getLastName(), userRetrievedViaSearchSpec.getLastName());
        assertNotNull("User should have a non-null destiny id", userRetrievedViaSearchSpec.getDestinyId());
        assertNotNull("User should have a non-null domain name", userRetrievedViaSearchSpec.getDomainName());
        assertEquals("User should be in the local domain", user.getDomainName(), userRetrievedViaSearchSpec.getDomainName());
        assertEquals("Users must be equivalent", user, userRetrievedViaSearchSpec);

        // Try creating the same user again:
        try {
            IApplicationUser user2 = this.appUserManagerToTest.createUser(LOGIN1, FN1, LN1, PASSWORD1);
            this.usersToCleanup.add(user2);
            fail("Should not be able to re-create a user with the same login: '" + LOGIN1 + "'");
        } catch (UserAlreadyExistsException ignore) {
        }
    }

    /**
     * Tests the linking of an external group as well as the import of external
     * users. Also verifies the auto-renaming feature and the uniqueness
     * enforcement based on the external id.
     * 
     * @throws Exception
     */
    public void testExternalGroupLinkAndExternalUserImport() throws Exception {
        String TOASTMASTERS_CLUB = "Toastmasters Club";
        IGroupSearchSpec toastmastersClubSearchSpec = new GroupSearchSpecImpl(TOASTMASTERS_CLUB);
        IExternalDomain externalDomain = (IExternalDomain) this.appUserManagerToTest.getExternalDomains().iterator().next();
        assertEquals("ensure we're working with the right domain", MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, externalDomain.getName());

        // Create a link to an external group:
        IExternalGroup toastmastersClub = (IExternalGroup) externalDomain.getUserAccessProvider().getExternalGroups(new IGroupSearchSpec[] { toastmastersClubSearchSpec }).iterator().next();
        assertNotNull("external group should exist", toastmastersClub);
        ExternalGroupLinkDataImpl linkToToastmastersClub = new ExternalGroupLinkDataImpl(toastmastersClub.getDomainName(), toastmastersClub.getTitle(), toastmastersClub.getExternalId());
        this.appUserManagerToTest.linkExternalAccessGroups(new IExternalGroupLinkData[] { linkToToastmastersClub });

        // Verify that a domain of the same name was created internally:
        IApplicationUserDomain internalDomain = this.appUserManagerToTest.getApplicationUserDomain(externalDomain.getName());
        assertNotNull("domain corresponding to the external group should exist internally now", internalDomain);
        assertEquals("domain shoudl have the right name", externalDomain.getName(), internalDomain.getName());

        // Retrieve the linked group:
        IAccessGroup linkedToastmastersClub = internalDomain.getAccessGroup(TOASTMASTERS_CLUB);
        assertNotNull("linked group should exist", linkedToastmastersClub);
        assertTrue("linked group should be of right type", linkedToastmastersClub instanceof ILinkedAccessGroup);
        this.groupsToCleanup.add(linkedToastmastersClub);

        // Explore the members:
        SortedSet importedUsersInLinkedToastmastersClub = this.appUserManagerToTest.getUsersInAccessGroup(linkedToastmastersClub.getDestinyId().longValue());
        assertNotNull("users collection should never be null", importedUsersInLinkedToastmastersClub);
        assertEquals("no users in this group were imported so the returned list should have nothing", 0, importedUsersInLinkedToastmastersClub.size());

        // Now import some users from this external group:
        Collection externalUsersInToastmastersClub = ((ILinkedAccessGroup) linkedToastmastersClub).getExternalMembers();
        Map uniqueNameToUserMap = new HashMap();
        assertNotNull("external users should exist in the given group", externalUsersInToastmastersClub);
        assertTrue("at least 1 external user should exist in teh given group", externalUsersInToastmastersClub.size() > 0);
        for (Iterator iter = externalUsersInToastmastersClub.iterator(); iter.hasNext();) {
            IExternalUser user = (IExternalUser) iter.next();
            IApplicationUser importedUser = this.appUserManagerToTest.importExternalUser(user);
            uniqueNameToUserMap.put(user.getUniqueName(), user);
            this.usersToCleanup.add(importedUser);
        }

        // Verify that if we query the users on the linked version, we should
        // get the same ones back:
        SortedSet internalUsersInLinkedToastmastersClub = this.appUserManagerToTest.getUsersInAccessGroup(linkedToastmastersClub.getDestinyId().longValue());
        assertNotNull("internal users should be returned", internalUsersInLinkedToastmastersClub);
        assertEquals("the internal/external users should be the same in number", externalUsersInToastmastersClub.size(), internalUsersInLinkedToastmastersClub.size());
        for (Iterator iter = internalUsersInLinkedToastmastersClub.iterator(); iter.hasNext();) {
            IApplicationUser user = (IApplicationUser) iter.next();
            IExternalUser externalUser = (IExternalUser) uniqueNameToUserMap.get(user.getUniqueName());
            assertNotNull("there should be a 1-1 correspondence between the internal/extenral users now", externalUser);

            // Also verify reverse-lookup:
            Set groupsContainingImportedUser = this.appUserManagerToTest.getAccessGroupsContainingUser(user.getDestinyId().longValue());
            assertNotNull("set of groups containing user must not be null", groupsContainingImportedUser);
            assertEquals("only 1 group should containing this user at present", 1, groupsContainingImportedUser.size());
            IAccessGroup groupContainingUser = (IAccessGroup) groupsContainingImportedUser.iterator().next();
            assertTrue("group containing user should be a linked group", groupContainingUser instanceof ILinkedAccessGroup);
            assertEquals("only expected group should contain this user", linkedToastmastersClub, groupContainingUser);
        }

        // Now try linking the same group again to verify that it fails to link:
        try {
            this.appUserManagerToTest.linkExternalAccessGroups(new IExternalGroupLinkData[] { linkToToastmastersClub });
            fail("should not have been able to re-link an existing linked group");
        } catch (AccessGroupCreationFailedException ignore) {
        }

        // Now try linking to a different group using the same title as before
        // and check auto-rename:
        String PRESIDENTIAL_SOCCER_TEAM = "Presidential Soccer Team";
        IGroupSearchSpec presidentialSoccerTeamSearchSpec = new GroupSearchSpecImpl(PRESIDENTIAL_SOCCER_TEAM);
        IExternalGroup presidentialSoccerTeam = (IExternalGroup) externalDomain.getUserAccessProvider().getExternalGroups(new IGroupSearchSpec[] { presidentialSoccerTeamSearchSpec }).iterator().next();
        assertNotNull("external group should exist", presidentialSoccerTeam);
        ExternalGroupLinkDataImpl linkToPresidentialSoccerTeam = new ExternalGroupLinkDataImpl(presidentialSoccerTeam.getDomainName(), TOASTMASTERS_CLUB, presidentialSoccerTeam.getExternalId());
        this.appUserManagerToTest.linkExternalAccessGroups(new IExternalGroupLinkData[] { linkToPresidentialSoccerTeam });

        // Verify that the group should not have the same name:
        IAccessGroup linkedPresidentialSoccerTeam = null;
        try {
            linkedPresidentialSoccerTeam = internalDomain.getAccessGroup(PRESIDENTIAL_SOCCER_TEAM);
            fail("no group should exist with this name");
        } catch (GroupNotFoundException ignore) {
        }
        linkedPresidentialSoccerTeam = internalDomain.getLinkedGroupByExternalId(presidentialSoccerTeam.getExternalId());
        assertNotNull("group should exist", linkedPresidentialSoccerTeam);
        assertTrue("group should NOT have a duplicate name", !TOASTMASTERS_CLUB.equals(linkedPresidentialSoccerTeam.getTitle()));
        this.groupsToCleanup.add(linkedPresidentialSoccerTeam);
    }

    /**
     * Tests the manipulation of the title, description, acl, fields of both an
     * internal and linked group. Verifies uniqueness enforcement for title
     * renaming.
     * 
     * @throws Exception
     */
    public void testGroupManipulation() throws Exception {
        String GROUP_NAME_1 = "InternalTestGroup1";
        IInternalAccessGroup group1 = this.appUserManagerToTest.createAccessGroup(GROUP_NAME_1, null);
        this.groupsToCleanup.add(group1);

        String GROUP_NAME_2 = "InternalTestGroup2";
        IInternalAccessGroup group2 = this.appUserManagerToTest.createAccessGroup(GROUP_NAME_2, null);
        this.groupsToCleanup.add(group2);

        // Try renaming group 1 to match group 2:
        try {
            this.appUserManagerToTest.updateGroup(group1.getDestinyId().longValue(), GROUP_NAME_2, GROUP_NAME_1 + " description");
            fail("renaming title to cause uniqueness violation should throw an exception");
        } catch (GroupAlreadyExistsException ignore) {
        }

        // Retrieve group 1 again and verify that nothing was set:
        IAccessGroup group1Test1 = this.appUserManagerToTest.getAccessGroup(group1.getDestinyId().longValue());
        assertNotNull("group 1 should still exist", group1Test1);
        assertEquals("group 1 should not have any description", "", group1Test1.getDescription());
        assertEquals("group 1 title should not have changed", group1.getTitle(), group1Test1.getTitle());
        assertEquals("both retrieved groups should be the same", group1, group1Test1);

        // Now try renaming group 2 to something else:
        this.appUserManagerToTest.updateGroup(group2.getDestinyId().longValue(), "TestTitle", "TestDescription");
        IAccessGroup group2Test1 = this.appUserManagerToTest.getAccessGroup(group2.getDestinyId().longValue());
        assertNotNull("group 2 should still exist", group2Test1);
        assertEquals("group 2 should have the new description", "TestDescription", group2Test1.getDescription());
        assertEquals("group 2 should have the new title", "TestTitle", group2Test1.getTitle());
        assertEquals("both retrieved groups should be the same", group2, group2Test1);

        // Now set back the original fields for group 2:
        this.appUserManagerToTest.updateGroup(group2.getDestinyId().longValue(), GROUP_NAME_2, null);
        IAccessGroup group2Test2 = this.appUserManagerToTest.getAccessGroup(group2.getDestinyId().longValue());
        assertNotNull("group 2 should still exist", group2Test2);
        assertEquals("group 2 should not have any description", "", group2Test2.getDescription());
        assertEquals("group 2 should have the old title", GROUP_NAME_2, group2Test2.getTitle());
        assertEquals("both retrieved groups should be the same", group2, group2Test2);

        // Verify that the right number of groups are returned:
        SortedSet existingGroups = this.appUserManagerToTest.getAccessGroups(new IGroupSearchSpec[] { new GroupSearchSpecImpl("XXInternalX"), new GroupSearchSpecImpl("InternalXTest"), new GroupSearchSpecImpl("InternalTestGroup") }, 0);
        assertNotNull("groups must exist", existingGroups);
        assertEquals("only the expected number of groups must exist", 2, existingGroups.size());
    }

    /**
     * This tests that internal group creation works. It also verifies that both
     * internal and external users can be added as members to this group. It
     * also verifies deletion of an internal user and its impact on the internal
     * group.
     *  
     */
    public void testAllMembershipOperations() throws Exception {
        int nGroupsToCreate = 5;
        int nUsersPerGroup = 5;

        // Create some internal groups:
        String GROUP_NAME = "InternalTestGroup";
        IAccessGroup[] internalAccessGroups = new IAccessGroup[nGroupsToCreate];
        for (int i = 0; i < nGroupsToCreate; i++) {
            IAccessGroup internalGroup = this.appUserManagerToTest.createAccessGroup(GROUP_NAME + i, null);
            assertNotNull("group should have been created", internalGroup);
            assertTrue("group should be an internal group", internalGroup instanceof IInternalAccessGroup);
            assertEquals("group should be in the local domain", MockApplicationUserManagerConfigurationImpl.USER_REPOSITORY_LOCAL_DOMAIN_NAME, internalGroup.getDomainName());
            assertEquals("group should have the correct name", GROUP_NAME + i, internalGroup.getTitle());
            this.groupsToCleanup.add(internalGroup);
            internalGroup = this.appUserManagerToTest.getAccessGroup(internalGroup.getDestinyId().longValue());
            assertNotNull("group should have been created", internalGroup);
            assertTrue("group should be an internal group", internalGroup instanceof IInternalAccessGroup);
            assertEquals("group should be in the local domain", MockApplicationUserManagerConfigurationImpl.USER_REPOSITORY_LOCAL_DOMAIN_NAME, internalGroup.getDomainName());
            assertEquals("group should have the correct name", GROUP_NAME + i, internalGroup.getTitle());
            internalAccessGroups[i] = internalGroup;
        }

        // Now create some internal users:
        String LOGIN = "login";
        String FN = "fn";
        String LN = "ln";
        String PASSWORD = "password";
        int nUsersToCreate = nGroupsToCreate * nUsersPerGroup;
        IApplicationUser[] internalUsers = new IApplicationUser[nUsersToCreate];
        for (int i = 0; i < nUsersToCreate; i++) {
            IApplicationUser internalUser = this.appUserManagerToTest.createUser(LOGIN + i, FN + i, LN + i, PASSWORD + i);
            assertNotNull("user should have been created", internalUser);
            assertEquals("user should be in the right domain", MockApplicationUserManagerConfigurationImpl.USER_REPOSITORY_LOCAL_DOMAIN_NAME, internalUser.getDomainName());
            assertEquals("user should have the right login", LOGIN + i, internalUser.getLogin());
            assertEquals("user should have the right first name", FN + i, internalUser.getFirstName());
            assertEquals("user should have the right last name", LN + i, internalUser.getLastName());
            this.usersToCleanup.add(internalUser);
            internalUser = this.appUserManagerToTest.getApplicationUser(internalUser.getDestinyId().longValue());
            assertNotNull("user should exist after being created", internalUser);
            assertEquals("user should be in the right domain", MockApplicationUserManagerConfigurationImpl.USER_REPOSITORY_LOCAL_DOMAIN_NAME, internalUser.getDomainName());
            assertEquals("user should have the right login", LOGIN + i, internalUser.getLogin());
            assertEquals("user should have the right first name", FN + i, internalUser.getFirstName());
            assertEquals("user should have the right last name", LN + i, internalUser.getLastName());
            internalUsers[i] = internalUser;
        }

        // Assign memberships - in chunks of nUsersPerGroup. So first batch of
        // nUsersPerGroup will belong to the first group, second batch to the
        // second group and so on.
        Set[] groupToMemberSet = new Set[nGroupsToCreate];
        for (int i = 0; i < nUsersToCreate;) {
            int nGroupIndex = i / nUsersPerGroup;

            // Assign nUsersToCreate/nGroupsToCreate users per group:
            for (int j = 0; j < nUsersPerGroup; j++, i++) {
                IApplicationUser user = internalUsers[i];
                IAccessGroup membershipGroup = internalAccessGroups[nGroupIndex];
                if (groupToMemberSet[nGroupIndex] == null) {
                    groupToMemberSet[nGroupIndex] = new HashSet();
                }
                groupToMemberSet[nGroupIndex].add(user);
            }
        }

        // Create memberships and then verify from group->user lookup:
        for (int i = 0; i < nGroupsToCreate; i++) {
            IAccessGroup group = internalAccessGroups[i];
            long[] userIdsInGroup = new long[nUsersPerGroup];
            Set expectedUsersInGroup = groupToMemberSet[i];

            // Create a long[] of user ids from membership assignments:
            int userIndex = 0;
            for (Iterator userIter = expectedUsersInGroup.iterator(); userIter.hasNext(); userIndex++) {
                IApplicationUser user = (IApplicationUser) userIter.next();
                userIdsInGroup[userIndex] = user.getDestinyId().longValue();
            }
            this.appUserManagerToTest.addUsersToAccessGroup(group.getDestinyId().longValue(), userIdsInGroup);

            // Verify memberships created:
            SortedSet usersInGroup = this.appUserManagerToTest.getUsersInAccessGroup(group.getDestinyId().longValue());
            assertNotNull("set of users in group should not be null", usersInGroup);
            assertEquals("size should be as expected", nUsersPerGroup, usersInGroup.size());
            assertFalse("users returned should not differ from expected users", usersInGroup.retainAll(expectedUsersInGroup));
        }

        // Verify membership going from user->group. Also associate the group as
        // the primary access group for those users:
        for (int i = 0; i < nUsersToCreate; i++) {
            IApplicationUser user = internalUsers[i];
            int groupIndex = i / nUsersPerGroup;
            IAccessGroup expectedGroup = internalAccessGroups[groupIndex];

            Set groupsContainingUser = this.appUserManagerToTest.getAccessGroupsContainingUser(user.getDestinyId().longValue());
            assertNotNull("set of groups containing user must not be null", groupsContainingUser);
            assertEquals("user should only be in one group at this point", 1, groupsContainingUser.size());
            assertEquals("user should be in the expected group", expectedGroup, groupsContainingUser.iterator().next());

            // Ensure user doesn't have a default primary access group:
            IAccessGroup primaryAccessGroupForUser = this.appUserManagerToTest.getPrimaryAccessGroupForUser(user.getDestinyId().longValue());
            assertNull("no primary access group should exist on user at this time", primaryAccessGroupForUser);

            // Set single group as primary access group for user:
            this.appUserManagerToTest.setPrimaryAccessGroupForUser(user.getDestinyId().longValue(), expectedGroup.getDestinyId());
            primaryAccessGroupForUser = this.appUserManagerToTest.getPrimaryAccessGroupForUser(user.getDestinyId().longValue());
            assertNotNull("primary access group for user should have been set", primaryAccessGroupForUser);
            assertEquals("primary access group on user should be as expected", expectedGroup, primaryAccessGroupForUser);
        }

        // Remove the first nUsersPerGroup/2 users from their corresponding
        // group,
        // and verify through lookup:
        int maxIndexOfUsersToDisassociateFromGroup = nUsersPerGroup / 2;
        for (int i = 0; i < maxIndexOfUsersToDisassociateFromGroup; i++) {
            IApplicationUser user = internalUsers[i];
            IAccessGroup groupContainingUser = (IAccessGroup) this.appUserManagerToTest.getAccessGroupsContainingUser(user.getDestinyId().longValue()).iterator().next();
            IAccessGroup primaryAccessGroup = this.appUserManagerToTest.getPrimaryAccessGroupForUser(user.getDestinyId().longValue());

            // Now remove membership:
            this.appUserManagerToTest.removeUsersFromAccessGroup(groupContainingUser.getDestinyId().longValue(), new long[] { user.getDestinyId().longValue() });
            Collection groupsContainingUser = this.appUserManagerToTest.getAccessGroupsContainingUser(user.getDestinyId().longValue());
            assertNotNull("collection of groups containing user should not be null", groupsContainingUser);
            assertEquals("no groups should be containing this user", 0, groupsContainingUser.size());

            assertNull("user should not have any primary access group now", this.appUserManagerToTest.getPrimaryAccessGroupForUser(user.getDestinyId().longValue()));
        }

        // Now verify that the disassociation of few users from groups didn't
        // impact any of the remaining associations:
        for (int i = maxIndexOfUsersToDisassociateFromGroup; i < nUsersPerGroup; i++) {
            IApplicationUser user = internalUsers[i];
            int groupIndex = i / nUsersPerGroup;
            IAccessGroup expectedGroup = internalAccessGroups[groupIndex];

            IAccessGroup groupContainingUser = (IAccessGroup) this.appUserManagerToTest.getAccessGroupsContainingUser(user.getDestinyId().longValue()).iterator().next();
            assertEquals("user should be in the expected group", expectedGroup, groupContainingUser);

            IAccessGroup primaryAccessGroup = this.appUserManagerToTest.getPrimaryAccessGroupForUser(user.getDestinyId().longValue());
            assertNotNull("primary access group for user should still exist", primaryAccessGroup);
            assertEquals("primary access group on user should be as expected", expectedGroup, primaryAccessGroup);
        }

        // Now verify that the remaining

        // Add external users to internal groups and confirm via lookup

        // Delete some users entirely and confirm impact on groups via lookup:

        // Delete some groups entirely and confirm impact on users via lookup
    }

    /**
     * Create a local user for testing purposes
     * 
     * @return a locally created user
     * @throws ApplicationUserCreationFailedException
     * @throws UserAlreadyExistsException
     */
    protected IApplicationUser createLocalUser() throws ApplicationUserCreationFailedException, UserAlreadyExistsException {
        String FN1 = "FN1";
        String LN1 = "LN1";
        String LOGIN1 = "LOGIN1";
        String PASSWORD1 = "PWD1";
        IApplicationUser user = this.appUserManagerToTest.createUser(LOGIN1, FN1, LN1, PASSWORD1);
        this.usersToCleanup.add(user);
        return user;
    }

    /**
     * Import a user for testing purposes
     * 
     * @return the imported user
     * @throws UserManagementAccessException
     * @throws UserAlreadyExistsException
     * @throws ApplicationUserImportFailedException
     */
    protected IApplicationUser importUser() throws UserManagementAccessException, ApplicationUserImportFailedException, UserAlreadyExistsException {
        // Now import some users from this external group:
        Collection externalUsers = this.appUserManagerToTest.getExternalUsers(null, 0);

        // Assume that one external user exists
        IExternalUser user = (IExternalUser) externalUsers.iterator().next();
        IApplicationUser importedUser = this.appUserManagerToTest.importExternalUser(user);
        this.usersToCleanup.add(importedUser);
        return importedUser;
    }

    protected IExternalUser getExternalUser(String login) throws Exception {
        return null;
    }

    /*
     * The teardown task also validates that the cleanup did go through, and
     * that the appropriate events were also fired by the user mgmt system
     * 
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        // Cleanup groups:
        for (Iterator groupIter = this.groupsToCleanup.iterator(); groupIter.hasNext();) {
            IAccessGroup group = (IAccessGroup) groupIter.next();
            long id = group.getDestinyId().longValue();
            this.appUserManagerToTest.deleteGroup(id);

            // Verify that the group no longer exists:
            try {
                group = this.appUserManagerToTest.getAccessGroup(id);
                fail("Group with id: '" + id + "' was not found so could not be deleted");
            } catch (GroupNotFoundException ignore) {
            }

            // Verify that the right observer event was fired:
            assertEquals("Observer should have been notified of the correct action", MockUserManagementObserverImpl.GROUP_DELETE, this.mockObserverForTest.getLastOperation());
            assertEquals("Observer should have been notified of the correct id involved", id, this.mockObserverForTest.getLastChangedId());

            // Verify that the right DCC event was fired:
            assertNotNull("DCC event should have been fired", this.mockEventManagerForTest.getLastEventFired());
            assertTrue("DCC event should have been of the right type", this.mockEventManagerForTest.getLastEventFired() instanceof IDCCServerEvent);
            final IDCCServerEvent lastEventFired = (IDCCServerEvent) this.mockEventManagerForTest.getLastEventFired();
            assertEquals("The right number of events should have been fired", 1, this.mockEventManagerForTest.getNEventsFired());
            assertEquals("DCC event should have been the right one", ChangeEventUtil.EVENT_NAME, lastEventFired.getName());
            Properties eventProps = lastEventFired.getProperties();
            assertNotNull("Event should have some properties", eventProps);
            assertEquals("Event should have the right number of properties", 2, eventProps.size());
            for (int i = 0; i < eventProps.size(); i++) {
                Enumeration enumeration = eventProps.propertyNames();
                while (enumeration.hasMoreElements()) {
                    String propName = (String) enumeration.nextElement();
                    String propValue = eventProps.getProperty(propName);
                    assertNotNull("Event attribute should be as expected", propName);
                    assertNotNull("Event attribute should have a value", propValue);
                    if (ChangeEventUtil.ACTION_PROP_NAME.equals(propName)) {
                        assertEquals("Action attribute value should be as expected", ActionTypeEnumType.GROUP_DELETE, ActionTypeEnumType.getByName(propValue));
                    } else if (ChangeEventUtil.ID_PROP_NAME.equals(propName)) {
                        assertEquals("ID attribute value should be as expected", id, Long.parseLong(propValue));
                    } else {
                        fail("Event attribute of unknown type");
                    }
                }
            }

            this.mockEventManagerForTest.reset();
            this.mockObserverForTest.reset();
        }

        // Cleanup users:
        for (Iterator userIter = this.usersToCleanup.iterator(); userIter.hasNext();) {
            IApplicationUser user = (IApplicationUser) userIter.next();
            long id = user.getDestinyId().longValue();
            this.appUserManagerToTest.deleteApplicationUser(user.getDestinyId().longValue());

            // Verify that the user no longer exists:
            try {
                user = this.appUserManagerToTest.getApplicationUser(id);
                fail("User with id: '" + id + "' did not get deleted");
            } catch (UserNotFoundException ignore) {
            }

            // Verify that the right events were fired:
            assertEquals("Observer should have been notified of the correct action", MockUserManagementObserverImpl.USER_DELETE, this.mockObserverForTest.getLastOperation());
            assertEquals("Observer should have been notified of the correct id involved", id, this.mockObserverForTest.getLastChangedId());

            // Verify that the right DCC event was fired:
            assertNotNull("DCC event should have been fired", this.mockEventManagerForTest.getLastEventFired());
            assertTrue("DCC event should have been of the right type", this.mockEventManagerForTest.getLastEventFired() instanceof IDCCServerEvent);
            IDCCServerEvent lastEventFired = (IDCCServerEvent) this.mockEventManagerForTest.getLastEventFired();
            assertEquals("The right number of events should have been fired", 1, this.mockEventManagerForTest.getNEventsFired());
            assertEquals("DCC event should have been the right one", ChangeEventUtil.EVENT_NAME, lastEventFired.getName());
            Properties eventProps = lastEventFired.getProperties();
            assertNotNull("Event should have some attributes", eventProps);
            assertEquals("Event should have the right number of attributes", 2, eventProps.size());
            Enumeration propIt = eventProps.propertyNames();
            while (propIt.hasMoreElements()) {
                final String propName = (String) propIt.nextElement();
                final String propValue = eventProps.getProperty(propName);
                assertNotNull("Event attribute should be as expected", propName);
                assertNotNull("Event attribute should have a value", propValue);
                if (ChangeEventUtil.ACTION_PROP_NAME.equals(propName)) {
                    assertEquals("Action attribute value should be as expected", ActionTypeEnumType.USER_DELETE, ActionTypeEnumType.getByName(propValue));
                } else if (ChangeEventUtil.ID_PROP_NAME.equals(propName)) {
                    assertEquals("ID attribute value should be as expected", id, Long.parseLong(propValue));
                } else {
                    fail("Event attribute of unknown type");
                }
            }

            this.mockEventManagerForTest.reset();
            this.mockObserverForTest.reset();
        }

        super.tearDown();
    }
}
