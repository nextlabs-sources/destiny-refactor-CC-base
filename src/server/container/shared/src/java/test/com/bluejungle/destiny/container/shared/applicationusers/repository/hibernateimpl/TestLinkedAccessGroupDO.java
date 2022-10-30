/*
 * Created on Jul 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.MockExternalDomainManagerImpl;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.MockExternalGroupImpl;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.MockExternalUserImpl;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.MockUserAccessProviderImpl;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;

import java.util.Collection;


/**
 * Test case for LinkedAccessGroupDO
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/TestLinkedAccessGroupDO.java#1 $
 */

public class TestLinkedAccessGroupDO extends BaseContainerSharedTestCase {
    private LinkedAccessGroupDO groupToTest = new LinkedAccessGroupDO();
    
    private HibernateApplicationUserRepository hibernateUserRepository;
    private MockExternalDomainManagerImpl mockExternalDomainManager;

    private MockExternalGroupImpl mockExternalGroup;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();        

        this.hibernateUserRepository = new HibernateApplicationUserRepository();
        this.mockExternalDomainManager = new MockExternalDomainManagerImpl();
        this.mockExternalGroup = new MockExternalGroupImpl(new byte[]{1}, "SomeTitle", MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        MockUserAccessProviderImpl accessProvider = (MockUserAccessProviderImpl) this.mockExternalDomainManager.getExternalDomain(MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME).getUserAccessProvider();
        accessProvider.setSingletonGroup(this.mockExternalGroup);
        MockExternalUserImpl externalUser = new MockExternalUserImpl("login", "fn", "ln", MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        accessProvider.setSingletonUserInGroup(externalUser);
        
        this.hibernateUserRepository.initialize(null, null, this.mockExternalDomainManager);        
        AccessDomainDO accessDomain = null;
        try {
            accessDomain = (AccessDomainDO) this.hibernateUserRepository.getApplicationUserDomain(MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        } catch (DomainNotFoundException exception) {
            accessDomain = (AccessDomainDO) this.hibernateUserRepository.createDomain(MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        }
        
        this.groupToTest = new LinkedAccessGroupDO(this.mockExternalGroup.getExternalId(), this.mockExternalGroup.getTitle(), accessDomain);
    }

    
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        this.hibernateUserRepository.deleteDomain(MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        super.tearDown();
    }


    /**
     * Test method for {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.LinkedAccessGroupDO#getExternalId()} and {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.LinkedAccessGroupDO#setExternalId(byte[])}.
     */
    public void testGetSetExternalId() {
        assertEquals("testGetSetExternalId - Ensure external id initially as expected.", this.mockExternalGroup.getExternalId(), this.groupToTest.getExternalId());
        byte[] externalIdToSet = {59, 4};
        this.groupToTest.setExternalId(externalIdToSet);
        assertEquals("testGetSetExternalId - Ensure external id set as expected.", externalIdToSet, this.groupToTest.getExternalId());

        // Test NPE
        try {
            this.groupToTest.setExternalId(null);
            fail("testGetSetExternalId - Should throw NPE when specifying null external id to setExternalId()");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.LinkedAccessGroupDO#getExternalMembers()}.
     * @throws DomainNotFoundException 
     * @throws ExternalUserAccessException 
     */
    public void testGetExternalMembers() throws DomainNotFoundException, ExternalUserAccessException {
        Collection externalMembers = this.groupToTest.getExternalMembers();

        IUserAccessProvider accessProvider = this.mockExternalDomainManager.getExternalDomain(MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME).getUserAccessProvider();
        Collection expectedExternalMembers = accessProvider.getUsersInExternalGroup(this.mockExternalGroup);
        
        assertTrue("testGetQualifiedExternalName - Ensure external members collection as expected", expectedExternalMembers.equals(externalMembers));
    }

    /**
     * Test method for {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.LinkedAccessGroupDO#getQualifiedExternalName()}.
     */
    public void testGetQualifiedExternalName() {
        assertEquals("testGetQualifiedExternalName - Ensure qualified external name as expected", this.mockExternalGroup.getQualifiedExternalName(), this.groupToTest.getQualifiedExternalName());
    }

    /**
     * Test method for {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.LinkedAccessGroupDO#isOrphaned()}.
     * @throws DomainNotFoundException 
     */
    public void testIsOrphaned() throws DomainNotFoundException {
        assertTrue("testIsOrphaned - Ensure linked access group not originally orphaned", !this.groupToTest.isOrphaned());
        
        MockUserAccessProviderImpl accessProvider = (MockUserAccessProviderImpl) this.mockExternalDomainManager.getExternalDomain(MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME).getUserAccessProvider();
        MockExternalGroupImpl anotherGroup  = new MockExternalGroupImpl(new byte[]{4}, "SomeTitle", MockExternalDomainManagerImpl.SINGLETON_DOMAIN_NAME);
        accessProvider.setSingletonGroup(anotherGroup);

        assertTrue("testIsOrphaned - Ensure linked access group not orphaned after external group is not longer accessible", this.groupToTest.isOrphaned());        
    }

}
