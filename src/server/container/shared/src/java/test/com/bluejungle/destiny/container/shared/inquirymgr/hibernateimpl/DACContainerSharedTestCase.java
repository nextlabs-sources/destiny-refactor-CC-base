/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.MockApplicationUserManagerConfigurationImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.tools.MockSharedContextLocator;

/**
 * This class simulates the DAC container to support the test cases.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/DACContainerSharedTestCase.java#1 $
 */

public class DACContainerSharedTestCase extends BaseDCCComponentTestCase {

    private static final Set REQUIRED_DATA_REPOSITORIES = new HashSet();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.ACTIVITY_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.MANAGEMENT_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
    }

    private static final String COMPONENT_NAME = "dac";

    /**
     * Constructor
     *  
     */
    public DACContainerSharedTestCase() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public DACContainerSharedTestCase(String testName) {
        super(testName);
    }

    /**
     * Returns the data source for the activity repository
     * 
     * @return the data source for the activity repository
     */
    protected IHibernateRepository getActivityDateSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
        return dataSource;
    }

    /**
     * Returns the data source for the management repository
     * 
     * @return the data source for the management repository
     */
    protected IHibernateRepository getManagementDateSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        return dataSource;
    }

    /**
     * Returns the data source for the policy framework repository
     * 
     * @return the data source for the policy framework repository
     */
    protected IHibernateRepository getPolicyDataSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
        return dataSource;
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#getComponentName()
     */
    protected String getComponentName() {
        return COMPONENT_NAME;
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#getDataRepositories()
     */
    protected Set getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }

    /**
     * Sets up the various component required for PF / DDIF
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        // Setup the mock shared context locator:
        ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(locatorInfo);

        //Setup the user management component:
        HashMapConfiguration applicationUserManagerFactoryConfig = new HashMapConfiguration();
        applicationUserManagerFactoryConfig.setProperty(IApplicationUserManagerFactory.APPLICATION_USER_CONFIGURATION, new MockApplicationUserManagerConfigurationImpl(AuthenticationModeEnumType.LOCAL));
        applicationUserManagerFactoryConfig.setProperty(IApplicationUserManagerFactory.MANAGEMENT_REPOSITORY, getManagementDateSource());
        IApplicationUserManagerFactory appUserMgrFactory = (IApplicationUserManagerFactory) compMgr.getComponent(ApplicationUserManagerFactoryImpl.class, applicationUserManagerFactoryConfig);
    }

}
