/*
 * Created on Jul 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.DomainAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;

import junit.framework.TestCase;

/**
 * Test case for BaseApplicationUserDO
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/TestBaseApplicationUserDO.java#1 $
 */

public class TestBaseApplicationUserDO extends BaseContainerSharedTestCase {

    private BaseApplicationUserDO userToTest;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.userToTest = new ApplicationUserDOImplToTest();
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getId() and {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#setId(java.lang.Long)}.
     */
    public void testGetSetId() {
        assertNull("testGetSetId - Ensure ID initially null.", this.userToTest.getId());
        Long idToSet = new Long(55);
        this.userToTest.setId(idToSet);
        assertEquals("testGetSetId - Ensure id set as expected.", idToSet, this.userToTest.getId());

        // Test NPE
        try {
            this.userToTest.setId(null);
            fail("testGetSetId - Should throw NPE when specifying null id to setId()");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getDestinyId()}.
     */
    public void testGetDestinyId() {
        // Destiny id should be the same as ID in Hibernate service provider
        // case
        assertNull("testGetDestinyId - Ensure Destiny ID initially null.", this.userToTest.getDestinyId());
        Long idToSet = new Long(55);
        this.userToTest.setId(idToSet);
        assertEquals("testGetDestinyId - Ensure Destiny ID is that which has been set", idToSet, this.userToTest.getDestinyId());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getFirstName()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#setFirstName(java.lang.String)}..
     */
    public void testGetSetFirstName() {
        assertNull("testGetSetFirstName - Ensure first name initially null.", this.userToTest.getFirstName());
        String firstNameToSet = "MyFirstName";
        this.userToTest.setFirstName(firstNameToSet);
        assertEquals("testGetSetFirstName - Ensure first name set as expected.", firstNameToSet, this.userToTest.getFirstName());

        // Test NPE
        try {
            this.userToTest.setFirstName(null);
            fail("testGetSetFirstName - Should throw NPE when specifying null first name to setFirstName()");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getLastName()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#setLastName(java.lang.String)}.
     */
    public void testGetSetLastName() {
        assertNull("testGetSetLastName - Ensure last name initially null.", this.userToTest.getLastName());
        String lastNameToSet = "MyLastName";
        this.userToTest.setLastName(lastNameToSet);
        assertEquals("testGetSetLastName - Ensure last name set as expected.", lastNameToSet, this.userToTest.getLastName());

        // Test NPE
        try {
            this.userToTest.setLastName(null);
            fail("testGetSetLastName - Should throw NPE when specifying null last name to setLastName()");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getLogin()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#setLogin(java.lang.String)}.
     */
    public void testGetSetLogin() {
        assertNull("testGetSetLogin - Ensure login initially null.", this.userToTest.getLogin());
        String loginToSet = "MyLogin";
        this.userToTest.setLogin(loginToSet);
        assertEquals("testGetSetLogin - Ensure login set as expected.", loginToSet, this.userToTest.getLogin());

        // Test NPE
        try {
            this.userToTest.setLogin(null);
            fail("testGetSetLogin - Should throw NPE when specifying null login to setLogin()");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getDomainName()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#setAccessDomain(com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.AccessDomainDO)}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getAccessDomain()}.
     */
    public void testGetDomainNameGetSetAccessDomain() throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException, DomainNotFoundException {
        // Ensure that the access domain is initially null
        assertNull("testGetDomainNameGetSetAccessDomain - Ensure access domain is initially null", this.userToTest.getAccessDomain());

        String domainName = "fofofofo";
        HibernateApplicationUserRepository repository = new HibernateApplicationUserRepository();        
        AccessDomainDO accessDomain = (AccessDomainDO) repository.createDomain(domainName);
        this.userToTest.setAccessDomain(accessDomain);

        // Ensure that the access domain was set as expected
        assertEquals("testGetDomainNameGetSetAccessDomain - Ensure that the access domain was set as expected", accessDomain, this.userToTest.getAccessDomain());

        // Ensure that the domain name is as expected
        assertEquals("testGetDomainNameGetSetAccessDomain - Ensure that the domain name is as expected", domainName, this.userToTest.getDomainName());

        // Test NPE
        try {
            this.userToTest.setAccessDomain(null);
            fail("testGetDomainNameGetSetAccessDomain - setAccessDomain() should throw NPE with null argument");
        } catch (NullPointerException exception) {
        }
        
        repository.deleteDomain(domainName);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getDisplayName()}.
     */
    public void testGetDisplayName() {
        // Display name is combination of last and first names
        String firstName = "MyFirstName";
        String lastName = "MyLastName";
        this.userToTest.setFirstName(firstName);
        this.userToTest.setLastName(lastName);
        assertEquals("testGetDisplayName - Ensure display name is as expected", lastName + ", " + firstName, this.userToTest.getDisplayName());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getUniqueName()}.
     */
    public void testGetUniqueName() {
        String domainName = "fofofofo";
        AccessDomainDO accessDomain = new AccessDomainDO(domainName);
        this.userToTest.setAccessDomain(accessDomain);

        String userName = "myUserName";
        this.userToTest.setLogin(userName);
        assertEquals("testGetUniqueName - Ensure Unique name as expected", userName + "@" + domainName, this.userToTest.getUniqueName());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getPrimaryAccessGroup()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#hasPrimaryAccessGroup()}.
     */
    public void testGetSetHasClearPrimaryAccessGroup() throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException, DomainNotFoundException {
        assertFalse("testGetSetHasPrimaryAccessGroup - Ensure primary initially not set.", this.userToTest.hasPrimaryAccessGroup());
        // Ensure illegal state is thrown when getPrimaryAccessGroup() invoked when primary access group is not set
        try {
            this.userToTest.getPrimaryAccessGroup();
            fail("Should throw IllegalStateException when primary access group not set");
        } catch (IllegalStateException exception) {
            
        }
        
        HibernateApplicationUserRepository repository = new HibernateApplicationUserRepository();
        IApplicationUserDomain domain = repository.getApplicationUserDomain(repository.getDefaultAdminDomainName());        
        IAccessGroup primaryAccessGroup = domain.createAccessGroup("foo", "foo");
        this.userToTest.setPrimaryAccessGroup(primaryAccessGroup);
        assertTrue("testGetSetHasPrimaryAccessGroup - Ensure hasPrimaryAccessGroup returns true when one is set", this.userToTest.hasPrimaryAccessGroup());
        assertEquals("testGetSetPrimaryAccessGroup - Ensure access group set as expected.", primaryAccessGroup, this.userToTest.getPrimaryAccessGroup());

        // Clear is
        this.userToTest.clearPrimaryAccessGroup();
        assertFalse("testGetSetHasPrimaryAccessGroup - Ensure primary not set after clear.", this.userToTest.hasPrimaryAccessGroup());
        // Ensure illegal state is thrown when getPrimaryAccessGroup() invoked when primary access group is not set
        try {
            this.userToTest.getPrimaryAccessGroup();
            fail("Should throw IllegalStateException when primary access group cleared");
        } catch (IllegalStateException exception) {
            
        }
        
        // Test NPE
        try {
            this.userToTest.setPrimaryAccessGroup(null);
            fail("testGetSetPrimaryAccessGroup - Should throw NPE when specifying null primary to setPrimaryAccessGroup()");
        } catch (NullPointerException exception) {
        }
        
        domain.deleteAccessGroup(primaryAccessGroup);
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#setHibernatePrimaryAccessGroup()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#getHibernatePrimaryAccessGroup()}.
     */
    public void testGetSetHibernatePrimaryAccessGroup() throws ApplicationUserRepositoryAccessException, DomainNotFoundException, GroupAlreadyExistsException {
        assertNull("testGetSetHibernatePrimaryAccessGroup - Ensure primary initially not set.", this.userToTest.getHibernatePrimaryAccessGroup());
        
        HibernateApplicationUserRepository repository = new HibernateApplicationUserRepository();
        IApplicationUserDomain domain = repository.getApplicationUserDomain(repository.getDefaultAdminDomainName());        
        IAccessGroup primaryAccessGroup = domain.createAccessGroup("foo", "foo");
        this.userToTest.setHibernatePrimaryAccessGroup(primaryAccessGroup);
        assertEquals("testGetSetHibernatePrimaryAccessGroup - Ensure access group set as expected.", primaryAccessGroup, this.userToTest.getHibernatePrimaryAccessGroup());
        
        domain.deleteAccessGroup(primaryAccessGroup);
    }
    
    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#isManuallyCreated()}.
     */
    public void testIsManuallyCreated() {
        assertFalse("testIsManuallyCreated - Ensure return false", this.userToTest.isManuallyCreated());
    }

    /**
     * @author sgoldstein
     */
    private class ApplicationUserDOImplToTest extends BaseApplicationUserDO {

    }
}
