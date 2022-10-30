/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.MockApplicationUserManagerConfigurationImpl;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.IDictionary;
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
 * Base Test case for DAC components
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/container/dac/BaseDACComponentTestCase.java#1 $
 */
public abstract class BaseDACComponentTestCase extends BaseDCCComponentTestCase {

    public static final String COMPONENT_NAME = "dac";
    public static final Set REQUIRED_DATA_REPOSITORIES = new HashSet();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.ACTIVITY_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.MANAGEMENT_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
    }

    /**
     * Main function
     * 
     * @param args
     *            no arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BaseDACComponentTestCase.class);
    }

    /**
     * Constructor for BaseDACComponentTestCase.
     * 
     * @param testName
     */
    public BaseDACComponentTestCase(String testName) {
        super(testName);
    }

    /**
     * Returns the activity data source
     * 
     * @return the activity data source
     */
    protected IHibernateRepository getActivityDataSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
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

        // Set up application user manager factory
        IHibernateRepository managementDataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        HashMapConfiguration applicationUserManagerFactoryConfig = new HashMapConfiguration();
        applicationUserManagerFactoryConfig.setProperty(IApplicationUserManagerFactory.APPLICATION_USER_CONFIGURATION, new MockApplicationUserManagerConfigurationImpl(AuthenticationModeEnumType.LOCAL));
        applicationUserManagerFactoryConfig.setProperty(IApplicationUserManagerFactory.MANAGEMENT_REPOSITORY, managementDataSource);
        IApplicationUserManagerFactory appUserMgrFactory = (IApplicationUserManagerFactory) compMgr.getComponent(ApplicationUserManagerFactoryImpl.class, applicationUserManagerFactoryConfig);
        IDictionary dictionary = (IDictionary) compMgr.getComponent(Dictionary.COMP_INFO);
    }

}
