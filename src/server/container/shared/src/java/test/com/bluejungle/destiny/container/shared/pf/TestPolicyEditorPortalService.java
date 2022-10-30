/*
 * Created on Feb 16, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.pf;

import java.util.Set;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.MockApplicationUserManagerConfigurationImpl;
import com.bluejungle.destiny.container.shared.pf.PolicyEditorServiceImpl;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceType;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
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
import com.bluejungle.pf.destiny.lib.IPolicyEditorService;
import com.bluejungle.pf.tools.MockSharedContextLocator;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/pf/TestPolicyEditorPortalService.java#1 $
 */

public class TestPolicyEditorPortalService extends BaseContainerSharedTestCase {

    private IPolicyEditorService peService;
    private int resourceID = 0;
    
    protected void setUp() throws Exception {
        super.setUp();
        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        // Setup the mock shared context
        ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) cm.getComponent(locatorInfo);
        
        // Setup dictionary 
        IDictionary dictionary = (IDictionary) cm.getComponent(Dictionary.COMP_INFO);
        
        // Set up Application user manager factory component
        IHibernateRepository managementDataSource = (IHibernateRepository) cm.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        HashMapConfiguration applicationUserManagerFactoryConfig = new HashMapConfiguration();
        applicationUserManagerFactoryConfig.setProperty(IApplicationUserManagerFactory.APPLICATION_USER_CONFIGURATION, new MockApplicationUserManagerConfigurationImpl(AuthenticationModeEnumType.LOCAL));
        applicationUserManagerFactoryConfig.setProperty(IApplicationUserManagerFactory.MANAGEMENT_REPOSITORY, managementDataSource);
        IApplicationUserManagerFactory appUserMgrFactory = (IApplicationUserManagerFactory) cm.getComponent(ApplicationUserManagerFactoryImpl.class, applicationUserManagerFactoryConfig);

        // Initialize the policy editor service:
        peService = (IPolicyEditorService) cm.getComponent(PolicyEditorServiceImpl.COMP_INFO);
        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetDictionaryEnrollmentRealms() throws Exception {
        Set<Realm> names = peService.getDictionaryEnrollmentRealms();
        assertNotNull(names);
        assert(names.size()>3);
    }
    
    public void testCreateDataSource() throws Exception {
        ExternalDataSourceConnectionInfo info = new ExternalDataSourceConnectionInfo();
        info.setUserName("Administrator");
        info.setPassWord("123blue!");
        info.setDomain("sharepoint2007");
        info.setType(ExternalDataSourceType.SHAREPOINT);
        info.setURL("http://sharepoint2007/");
        resourceID = peService.createExternalDataSource(info);
    }
    

}
