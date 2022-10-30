package com.bluejungle.destiny.container.shared.pf;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/pf/TestPolicyEditorService.java#1 $
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.MockApplicationUserManagerConfigurationImpl;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.IPolicyEditorService;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.utils.PQLTestUtils;
import com.bluejungle.pf.tools.MockSharedContextLocator;

import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.Dictionary;

/**
 * This tests the Policy Editor service without going
 * through the web services layer.
 */
public class TestPolicyEditorService extends BaseContainerSharedTestCase {

    private LifecycleManager lm;
    private IPolicyEditorService peService;

     /**
     * @see junit.framework.TestCase#setUp()
     */
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
        lm = (LifecycleManager) cm.getComponent(LifecycleManager.COMP_INFO);
        Session hs = ((SessionFactory) cm.getComponent( DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName() )).openSession();
        Transaction tx = hs.beginTransaction();
        try {
            hs.delete( "from DeploymentEntity" );
            hs.delete( "from DevelopmentEntity" );
            hs.delete( "from DeploymentRecord");
            tx.commit();
        } finally {
            hs.close();
        }

        Collection policyDES = lm.getEntitiesForNames(EntityType.POLICY, policyNames, LifecycleManager.MAKE_EMPTY);
        for (Iterator iter = policyDES.iterator(); iter.hasNext();) {
            DevelopmentEntity de = (DevelopmentEntity) iter.next();
            PQLTestUtils.setStatus( de, DevelopmentStatus.APPROVED );
        }

        lm.deployEntities( policyDES, POLICY_DD, DeploymentType.PRODUCTION, false, null);

        Collection specDES = lm.getEntitiesForNames(EntityType.RESOURCE, specNames, LifecycleManager.MAKE_EMPTY );
        for (Iterator iter = specDES.iterator(); iter.hasNext();) {
            DevelopmentEntity de = (DevelopmentEntity) iter.next();
            PQLTestUtils.setStatus( de, DevelopmentStatus.APPROVED);
        }

        lm.deployEntities( specDES, SPEC_DD, DeploymentType.PRODUCTION, false, null);
    }
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public final void testGetNumDeployedPolicies() throws PolicyServiceException {
        int numP = peService.getNumDeployedPolicies(CHECK_DD);
        assertEquals("number of policies must be correct", NUM_POLICIES, numP);
    }

    public final void testGetLatestDeploymentTime() throws PolicyServiceException {
        Date lastDeployment = peService.getLatestDeploymentTime(CHECK_DD);
        assertNotNull("last deployment date must not be null", lastDeployment);

        assertEquals("last deployment date must be correct", SPEC_DD.getTime() / 1000, lastDeployment.getTime()/1000);
    }

    private static final ArrayList policyNames = new ArrayList();
    private static final ArrayList specNames = new ArrayList();
    private static final long HOUR = 60*60*1000;
    private static final long SOME_TIME = System.currentTimeMillis() + HOUR * 10;
    private static final long SOME_TIME_LATER = SOME_TIME + HOUR;
    private static final long SOME_TIME_LATER_STILL = SOME_TIME_LATER + HOUR;
    private static final Date POLICY_DD = UnmodifiableDate.forTime(SOME_TIME);
    private static final Date SPEC_DD = UnmodifiableDate.forTime(SOME_TIME_LATER);
    private static final Date CHECK_DD = UnmodifiableDate.forTime(SOME_TIME_LATER_STILL);
    private static final int NUM_POLICIES = 17;


    static {
        for (int i = 0; i < NUM_POLICIES; i++) {
            policyNames.add("Policy" + i);
        }

        for (int i = 0; i < 4; i++) {
            specNames.add("Spec" + i);
        }
    }
}
