/*
 * Created on Jul 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import junit.framework.TestCase;

import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.GroupSearchSpecImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.MockApplicationUserManagerConfigurationImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.UserSearchSpecImpl;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/TestLDAPAccessProvider.java#2 $
 */

public class TestLDAPAccessProvider extends TestCase {

    private LDAPAccessProvider ldapAccessProviderToTest;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestLDAPAccessProvider.class);
    }

    /**
     * Constructor for TestLDAPAccessProvider.
     * 
     * @param arg0
     */
    public TestLDAPAccessProvider(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        this.ldapAccessProviderToTest = new LDAPAccessProvider();
        this.ldapAccessProviderToTest.initialize(MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_ACCESS_PROPS_FOR_TEST);
    }

    /**
     * Tests the getExternalUsers() api
     *  
     */
   // public void testGetExternalUsers() {
  //      IUserSearchSpec[] s1 = new IUserSearchSpec[] { new UserSearchSpecImpl("Linc") };
  //      IUserSearchSpec[] s2 = new IUserSearchSpec[] { new UserSearchSpecImpl("Cart") };
  //      IUserSearchSpec[] s3 = null;

  //      Collection s1Results;
  //      Collection s2Results;
  //      Collection s3Results;
   //     try {
   //         s1Results = this.ldapAccessProviderToTest.getExternalUsers(s1, 0);
   //         assertNotNull("s1 should return results", s1Results);
    //        assertEquals("s1 should return expected # of users", 1, s1Results.size());
    //        IExternalUser s1User = (IExternalUser) s1Results.iterator().next();
   //         assertEquals("domain name should be correct", MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, s1User.getDomainName());
   //         assertEquals("first name must be correct", "Abraham", s1User.getFirstName());
   //         assertEquals("last name must be correct", "Lincoln", s1User.getLastName());
   //         assertEquals("login name must be correct", "abraham.lincoln", s1User.getLogin());
   //         assertEquals("unique name must be correct", "abraham.lincoln@" + MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, s1User.getUniqueName());

    //        s2Results = this.ldapAccessProviderToTest.getExternalUsers(s2, -5);
    //        assertNotNull("s2 should return results", s2Results);
    //        assertEquals("s2 should return expected # of users", 1, s2Results.size());
    //        IExternalUser s2User = (IExternalUser) s2Results.iterator().next();
    //        assertEquals("domain name should be correct", MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, s1User.getDomainName());
     //       assertEquals("first name must be correct", "Jimmy", s2User.getFirstName());
     //       assertEquals("last name must be correct", "Carter", s2User.getLastName());
     //       assertEquals("login name must be correct", "jimmy.carter", s2User.getLogin());
     //       assertEquals("unique name must be correct", "jimmy.carter@" + MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, s2User.getUniqueName());

     //       s3Results = this.ldapAccessProviderToTest.getExternalUsers(s3, -1);
      //      assertNotNull("s3 should return results", s3Results);
      //      //assertEquals("s3 should return expected # of users", 44, s3Results.size());
     //   } catch (ExternalUserAccessException e) {
    //        fail("Could not access external user repository");
    //    }
//    }

    /**
     * Tests the query for external groups
     *  
     */
    public void testGetExternalGroups() throws Exception {
        IGroupSearchSpec[] QUERY_FOR_ALL_GROUPS = new IGroupSearchSpec[] { new GroupSearchSpecImpl("") };
        IGroupSearchSpec[] QUERY_FOR_FINANCIAL_PLANNING_GROUP = new IGroupSearchSpec[] { new GroupSearchSpecImpl("Financial Planning") };
        IGroupSearchSpec[] QUERY_FOR_COMPUTERS_GROUP = new IGroupSearchSpec[] { new GroupSearchSpecImpl("Computers") };
        IGroupSearchSpec[] QUERY_FOR_NONEXISTENT_GROUPS = new IGroupSearchSpec[] { new GroupSearchSpecImpl("hhhJjjFkKHDkFllkKgdGJ") };

        Collection allGroupsCollection;
        Collection financialPlanningGroupCollection;
        Collection computersGroupCollection;
        Collection noGroupsCollection;

        try {
            // All groups:
            allGroupsCollection = this.ldapAccessProviderToTest.getExternalGroups(QUERY_FOR_ALL_GROUPS);
            assertNotNull("query for all groups should return results", allGroupsCollection);
            assertEquals("query for all groups should return expected # of groups", 23, allGroupsCollection.size());

            // "Financial Planning" group:
            financialPlanningGroupCollection = this.ldapAccessProviderToTest.getExternalGroups(QUERY_FOR_FINANCIAL_PLANNING_GROUP);
            assertNotNull("query for financial group should return results", financialPlanningGroupCollection);
            assertEquals("query for financial group should return expected # of groups", 1, financialPlanningGroupCollection.size());
            IExternalGroup financialPlanningGroup = (IExternalGroup) financialPlanningGroupCollection.iterator().next();
            assertEquals("domain name should be correct", MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, financialPlanningGroup.getDomainName());
            assertEquals("title must be correct", QUERY_FOR_FINANCIAL_PLANNING_GROUP[0].getTitleStartsWith(), financialPlanningGroup.getTitle());
            assertNotNull("external id must exist", financialPlanningGroup.getExternalId());
            IExternalGroup financialPlanningGroupTest1 = this.ldapAccessProviderToTest.getGroup(financialPlanningGroup.getExternalId());
            assertNotNull("group retrieved via external id should not be null", financialPlanningGroupTest1);
            assertEquals("group retrieved via external id should be same", financialPlanningGroup, financialPlanningGroupTest1);

            // "Computers" group:
            computersGroupCollection = this.ldapAccessProviderToTest.getExternalGroups(QUERY_FOR_COMPUTERS_GROUP);
            assertNotNull("query for computers group should return results", computersGroupCollection);
            assertEquals("query for computers group should return expected # of groups", 1, computersGroupCollection.size());
            IExternalGroup computersGroup = (IExternalGroup) computersGroupCollection.iterator().next();
            assertEquals("domain name should be correct", MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, computersGroup.getDomainName());
            assertEquals("title must be correct", QUERY_FOR_COMPUTERS_GROUP[0].getTitleStartsWith(), computersGroup.getTitle());
            assertNotNull("external id must exist", computersGroup.getExternalId());
            IExternalGroup computersGroupTest1 = this.ldapAccessProviderToTest.getGroup(computersGroup.getExternalId());
            assertNotNull("group retrieved via external id should not be null", computersGroupTest1);
            assertEquals("group retrieved via external id should be same", computersGroup, computersGroupTest1);

            // Non-existent group:
            noGroupsCollection = this.ldapAccessProviderToTest.getExternalGroups(QUERY_FOR_NONEXISTENT_GROUPS);
            assertNotNull("query for non-existent groups should return results", noGroupsCollection);
            assertEquals("query for non-existent groups should return expected # of groups", 0, noGroupsCollection.size());
        } catch (ExternalUserAccessException e) {
            fail("Could not access external user repository");
        }
    }

    /**
     * Validates that group exploration works, including nested groups. Also
     * validates that the equals() operator works on group entries.
     * 
     * @throws Exception
     */
    public void testExternalGroupExploration() throws Exception {
        IGroupSearchSpec[] QUERY_FOR_IMMIGRATION_GROUP = new IGroupSearchSpec[] { new GroupSearchSpecImpl("Immigration") };
        IExternalGroup immigrationGroup = (IExternalGroup) this.ldapAccessProviderToTest.getExternalGroups(QUERY_FOR_IMMIGRATION_GROUP).iterator().next();
        Collection immigrationGroupUsers = this.ldapAccessProviderToTest.getUsersInExternalGroup(immigrationGroup);
        assertNotNull("query for users should return a non-null collection", immigrationGroupUsers);
        assertEquals("immigration group should have the right number of users", 2, immigrationGroupUsers.size());
        for (Iterator userIter = immigrationGroupUsers.iterator(); userIter.hasNext();) {
            IExternalUser user = (IExternalUser) userIter.next();
            assertTrue("user must be from one of these", user.getLogin().equals("abraham.lincoln") || user.getLogin().equals("james.madison"));
        }

        IGroupSearchSpec[] QUERY_FOR_GROUPS_ORGANIZATION = new IGroupSearchSpec[] { new GroupSearchSpecImpl("Groups") };
        IExternalGroup groupsOrganization = (IExternalGroup) this.ldapAccessProviderToTest.getExternalGroups(QUERY_FOR_GROUPS_ORGANIZATION).iterator().next();
        Collection groupsOrganizationUsers = this.ldapAccessProviderToTest.getUsersInExternalGroup(groupsOrganization);
        assertNotNull("query for users should return a non-null collection", groupsOrganizationUsers);
        assertEquals("groups organization should have the right number of users", 2, groupsOrganizationUsers.size());
    }

    /**
     * This method retrieves a user by login, then verifies that it is the right
     * user, then retrieves all groups that this user belongs to, and verifies
     * that they are correct
     *  
     */
    public void testUserToGroupLookup() throws Exception {
        /*
         * Verify Abraham Lincoln's membership:
         */
        String ABRAHAM_LINCOLN_LOGIN = "abraham.lincoln";
        IExternalUser abrahamLincoln = this.ldapAccessProviderToTest.getUser(ABRAHAM_LINCOLN_LOGIN);
        assertNotNull("user should be retrieved using valid login", abrahamLincoln);
        assertEquals("user should have the right login", ABRAHAM_LINCOLN_LOGIN, abrahamLincoln.getLogin());
        assertEquals("user should have the right domain", MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, abrahamLincoln.getDomainName());

        // Retrieve the groups that this user belongs to:
        Collection<IExternalGroup> groupsContainingAbrahamLincoln = this.ldapAccessProviderToTest.getExternalGroupsContainingUser(abrahamLincoln);
        assertNotNull("collection of groups containing user should not be null", groupsContainingAbrahamLincoln);
        
        Set<String> expectedGroupNames = new HashSet<String>();
        Collections.addAll(expectedGroupNames, 
                "All Legal Groups",
                "All Sales Groups", 
                "B2B Sales",
                "Extracurricular Groups",
                "Immigration",
                "Presidents", 
                "Toastmasters Club", 
                "Users" 
        );
        
        assertEquals("the right number of groups should contain this user", expectedGroupNames.size(), groupsContainingAbrahamLincoln.size());
        
        for (IExternalGroup group : groupsContainingAbrahamLincoln) {
            String title = group.getTitle();
            assertTrue("group should be one of the expected groups " + title, 
                    expectedGroupNames.contains(group.getTitle()));
        }

        /*
         * Verify John Kennedy's membership:
         */
        String JOHN_KENNEDY_LOGIN = "john.kennedy";
        IExternalUser johnKennedy = this.ldapAccessProviderToTest.getUser(JOHN_KENNEDY_LOGIN);
        assertNotNull("user should be retrieved using valid login", johnKennedy);
        assertEquals("user should have the right login", JOHN_KENNEDY_LOGIN, johnKennedy.getLogin());
        assertEquals("user should have the right domain", MockApplicationUserManagerConfigurationImpl.EXTERNAL_DOMAIN_NAME, johnKennedy.getDomainName());

        // Retrieve the groups that this user belongs to:
        Collection<IExternalGroup> groupsContainingJohnKennedy = this.ldapAccessProviderToTest.getExternalGroupsContainingUser(johnKennedy);
        assertNotNull("collection of groups containing user should not be null", groupsContainingJohnKennedy);
        
        expectedGroupNames = new HashSet<String>();
        Collections.addAll(expectedGroupNames, 
                "Users",
                "Presidents"
        );
        assertEquals("the right number of groups should contain this user", expectedGroupNames.size(), groupsContainingJohnKennedy.size());
        
        for (IExternalGroup group : groupsContainingJohnKennedy) {
            String title = group.getTitle();
            assertTrue("group should be one of the expected groups " + title, 
                    expectedGroupNames.contains(group.getTitle()));
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
