/*
 * Created on Jun 30, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test class for BaseAccessGroupDO
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/TestBaseAccessGroupDO.java#2 $
 */

public class TestBaseAccessGroupDO extends BaseContainerSharedTestCase {

    private BaseAccessGroupDO groupToTest;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.groupToTest = new AccessGroupImplToTest();
    }

    /*
     * Test method for
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.getId()'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.setId()'
     */
    public void testGetSetId() {
        assertNull("testGetSetId - Ensure ID initially null.", this.groupToTest.getId());
        Long idToSet = new Long(55);
        this.groupToTest.setId(idToSet);
        assertEquals("testGetSetId - Ensure id set as expected.", idToSet, this.groupToTest.getId());

        // Test NPE
        try {
            this.groupToTest.setId(null);
            fail("testGetSetId - Should throw NPE when specifying null id to setId()");
        } catch (NullPointerException exception) {
        }
        ;
    }

    /*
     * Test method for
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.getDestinyId()'
     */
    public void testGetDestinyId() {
        // Destiny id should be the same as ID in Hibernate service provider
        // case
        assertNull("testGetDestinyId - Ensure Destiny ID initially null.", this.groupToTest.getDestinyId());
        Long idToSet = new Long(55);
        this.groupToTest.setId(idToSet);
        assertEquals("testGetDestinyId - Ensure Destiny ID is that which has been set", idToSet, this.groupToTest.getDestinyId());
    }

    /*
     * Test method for
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.getTitle()'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.setTitle()'
     */
    public void testGetSetTitle() {
        assertNull("testGetSetTitle - Ensure title initially null.", this.groupToTest.getTitle());
        String titleToSet = "MyTitle";
        this.groupToTest.setTitle(titleToSet);
        assertEquals("testGetSetTitle - Ensure title is set as expected", titleToSet, this.groupToTest.getTitle());

        // Try alternate constructor
        BaseAccessGroupDO groupCreated = new AccessGroupImplToTest(titleToSet, "Some Description", new AccessDomainDO());
        assertEquals("testGetSetTitle - Ensure title passed to constructor is set as expected", titleToSet, groupCreated.getTitle());

        // Test NPE
        // SDG - 4/24/07 - Taken out to work around Oracle empty string to null issue.  Should be put back in at a later date
        /*try {
            this.groupToTest.setTitle(null);
            fail("testGetSetTitle - setTitle() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }*/
        
    }

    /*
     * Test method for
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.getDescription()'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.setDescription()'
     */
    public void testGetSetDescription() {
        assertNull("testGetSetDescription - Ensure description initially null.", this.groupToTest.getDescription());
        String descriptionToSet = "MyDescription";
        this.groupToTest.setDescription(descriptionToSet);
        assertEquals("testGetSetDescription - Ensure description is set as expected", descriptionToSet, this.groupToTest.getDescription());

        // Try alternate constructor
        BaseAccessGroupDO groupCreated = new AccessGroupImplToTest("Some Title", descriptionToSet, new AccessDomainDO());
        assertEquals("testGetSetDescription - Ensure description passed to constructor is set as expected", descriptionToSet, groupCreated.getDescription());

        // Test NPE
        // SDG - 4/24/07 - Taken out to work around Oracle empty string to null issue.  Should be put back in at a later date
        /*try {
            this.groupToTest.setDescription(null);
            fail("testGetSetDescription - setDescription() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }*/
    }

    /*
     * Test method for
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.getDomainName()'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.getAccessDomain()'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.setAccessDomain()'
     */
    public void testGetDomainNameGetSetAccessDomain() throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException, DomainNotFoundException {
        // Ensure that the access domain is initially null
        assertNull("testGetDomainNameGetSetAccessDomain - Ensure that the access domain is initially null", this.groupToTest.getAccessDomain());

        String domainName = "fofofofo";
        HibernateApplicationUserRepository repository = new HibernateApplicationUserRepository();        
        AccessDomainDO accessDomain = (AccessDomainDO) repository.createDomain(domainName);
        this.groupToTest.setAccessDomain(accessDomain);

        // Ensure that the access domain was set as expected
        assertEquals("testGetDomainNameGetSetAccessDomain - Ensure that the access domain was set as expected", accessDomain, this.groupToTest.getAccessDomain());

        // Ensure that the domain name is as expected
        assertEquals("testGetDomainNameGetSetAccessDomain - Ensure that the domain name is as expected", domainName, this.groupToTest.getDomainName());

        // Test NPE
        try {
            this.groupToTest.setAccessDomain(null);
            fail("testGetDomainNameGetSetAccessDomain - setAccessDomain() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }
        
        repository.deleteDomain(domainName);
    }

    /*
     * Test method for
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.getMembers()'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.setMembers(Set)'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.addMember(BaseApplicationUserDO)' *
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.addMembers(BaseApplicationUserDO)'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.deleteMember(BaseApplicationUserDO)' *
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.deleteMembers(BaseApplicationUserDO)'
     */
    public void testGetSetMembersAddMemberAddMembersDeleteMemberDeleteMembers() throws ApplicationUserCreationFailedException, UserAlreadyExistsException, ApplicationUserRepositoryAccessException, DomainNotFoundException, UserNotFoundException {
        // Ensure member set initial empty
        Set members = this.groupToTest.getMembers();
        assertTrue("testGetSetMembersAddMemberDeleteMember - Ensure member set initially empty", members.isEmpty());

        // Add a member
        HibernateApplicationUserRepository repository = new HibernateApplicationUserRepository();
        IApplicationUserDomain domain = repository.getApplicationUserDomain(repository.getDefaultAdminDomainName());
        BaseApplicationUserDO applicationUser = (BaseApplicationUserDO) domain.createNewUser("foo", "foo", "foo", "foo");
        this.groupToTest.addMember(applicationUser);
        members = this.groupToTest.getMembers();
        assertTrue("testGetSetMembersAddMemberDeleteMember - Ensure added member is in member set", members.contains(applicationUser));
        assertEquals("testGetSetMembersAddMemberDeleteMember - Ensure member set is of size 1", 1, members.size());

        this.groupToTest.deleteMember(applicationUser);
        members = this.groupToTest.getMembers();
        assertTrue("testGetSetMembersAddMemberDeleteMember - Ensure member set empty after deleting user", members.isEmpty());

        // Try to delete it again and make sure there are no problems
        this.groupToTest.deleteMember(applicationUser);

        // Now, try to set a complete set of members
        Set myMemberSet = new HashSet();
        BaseApplicationUserDO applicationUserSetMember = (BaseApplicationUserDO) domain.createNewUser("bar", "bar", "bar", "bar");
        myMemberSet.add(applicationUserSetMember);
        this.groupToTest.setMembers(myMemberSet);
        members = this.groupToTest.getMembers();
        assertTrue("testGetSetMembersAddMemberDeleteMember - Ensure member set contains those in set Set", members.contains(applicationUserSetMember));
        assertEquals("testGetSetMembersAddMemberDeleteMember - Ensure member set is of size of set Set", myMemberSet.size(), members.size());

        // Try to add a collection of members at one
        Set membersToAdd = new HashSet();
        BaseApplicationUserDO applicationUserToAdd = (BaseApplicationUserDO) domain.createNewUser("cheese", "cheese", "cheese", "cheese");
        membersToAdd.add(applicationUserToAdd);
        this.groupToTest.addMembers(membersToAdd);
        members = this.groupToTest.getMembers();
        assertTrue("testGetSetMembersAddMemberDeleteMember - Ensure member set contains those in the original set", members.contains(applicationUserSetMember));
        assertTrue("testGetSetMembersAddMemberDeleteMember - Ensure member set contains those just added", members.contains(applicationUserToAdd));
        assertEquals("testGetSetMembersAddMemberDeleteMember - Ensure member set is of size expected", 2, members.size());

        // Try removing all of them
        Set membersToDelete = new HashSet();
        membersToDelete.addAll(myMemberSet);
        membersToDelete.addAll(membersToAdd);
        this.groupToTest.deleteMembers(membersToDelete);
        assertTrue("testGetSetMembersAddMemberDeleteMember - Ensure member set is now empty after removing all", members.isEmpty());

        // Test NPE
        try {
            this.groupToTest.setMembers(null);
            fail("testGetSetMembersAddMemberDeleteMember - setMembers() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }

        try {
            this.groupToTest.addMember(null);
            fail("testGetSetMembersAddMemberDeleteMember - addMember() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }

        try {
            this.groupToTest.addMembers(null);
            fail("testGetSetMembersAddMemberDeleteMember - addMembers() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }

        try {
            this.groupToTest.deleteMember(null);
            fail("testGetSetMembersAddMemberDeleteMember - deleteMember() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }

        try {
            this.groupToTest.deleteMembers(null);
            fail("testGetSetMembersAddMemberDeleteMember - deleteMembers() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }
        
        domain.deleteApplicationUser(applicationUserToAdd);
        domain.deleteApplicationUser(applicationUserSetMember);
        domain.deleteApplicationUser(applicationUser);
    }

    /*
     * Test method for
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.getApplicableAccessControl()'
     * 'com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseAccessGroupDO.setApplicableAccessControl()'
     */
    public void testGetSetApplicableAccessControl() {
        // Ensure access controls string initially empty
        assertEquals("testGetSetApplicableAccessControl - Ensure access control string initially as expected", "access_control ", this.groupToTest.getApplicableAccessControl());

        // Ensure that is can be set as expected
        String expectedAccessControlString = "My acess control string which normally would not look like this";
        this.groupToTest.setApplicableAccessControl(expectedAccessControlString);
        assertEquals("testGetSetApplicableAccessControl - Ensure applicable access control can be set as expected", expectedAccessControlString, this.groupToTest.getApplicableAccessControl());

        // Test NPE
        try {
            this.groupToTest.setApplicableAccessControl(null);
            fail("testGetSetApplicableAccessControl - setApplicableAccessControl() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }
    }

    private class AccessGroupImplToTest extends BaseAccessGroupDO {

        /**
         * Create an instance of AccessGroupImplToTest
         * 
         * @param titleToSet
         * @param string
         * @param domainDO
         */
        public AccessGroupImplToTest(String title, String description, AccessDomainDO accessDomain) {
            super(title, description, accessDomain);
        }

        /**
         * Create an instance of AccessGroupImplToTest
         */
        public AccessGroupImplToTest() {
            // TODO Auto-generated constructor stub
        }
    }
}
