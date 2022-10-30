/*
 * Created on Mar 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.SampleDataMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IResourceCache;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;

/**
 * This is the test class for the policy resource cache.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/PolicyCacheTest.java#1 $
 */

public class PolicyCacheTest extends DACContainerSharedTestCase {

    private SampleDataMgr dataMgr;
    private static final int NB_POLICIES = 100;

    /**
     * Deletes deployment entity records
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             hibernate exception
     */
    protected void deleteDeploymentEntities(Session s) throws HibernateException {
        s.delete("from DeploymentEntity");
        s.delete("from DevelopmentEntity");
        s.delete("from DeploymentRecord");
    }

    /**
     * Returns an instance of the policy cache
     * 
     * @return an instance of the policy cache
     */
    private PolicyCacheImpl getPolicyCache() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IResourceCache.ACTIVITY_DATASOURCE_CONFIG_ATTR, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("policyCache4Test", PolicyCacheImpl.class.getName(), IConfigurable.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        PolicyCacheImpl cache = (PolicyCacheImpl) compMgr.getComponent(compInfo);
        return cache;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase#needLDAPAdapter()
     */
    protected boolean needLDAPAdapter() {
        return false;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.dataMgr = new SampleDataMgr();
        Session s = this.getActivityDateSource().getSession();
        this.dataMgr.setNbPolicies(NB_POLICIES);
        this.dataMgr.deletePolicies(s);
        this.dataMgr.createPolicies(s);
        HibernateUtils.closeSession(s, null);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Session s = this.getActivityDateSource().getSession();
        this.dataMgr.deletePolicies(s);
        HibernateUtils.closeSession(s, null);
        super.tearDown();
    }

    /**
     * This test verifies the basic about the policy cache class.
     */
    public void testPolicyCacheClassBasics() {
        PolicyCacheImpl policyCache = getPolicyCache();
        assertNotNull("Policy cache should be created", policyCache);
        assertTrue("Policy cache should be initializable", policyCache instanceof IInitializable);
        assertTrue("Policy cache should be logEnabled", policyCache instanceof ILogEnabled);
        assertTrue("Policy cache should be disposable", policyCache instanceof IDisposable);
        assertTrue("Policy cache should be configurable", policyCache instanceof IConfigurable);
        assertTrue("Policy cache should be manager enabled", policyCache instanceof IManagerEnabled);
        assertTrue("Policy cache should extend the basic resource cache class", policyCache instanceof BaseResourceCacheImpl);
    }

    /**
     * This test verifies that the data objects for policy are working properly.
     */
    public void testPolicyCacheDataObjects() {
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            List result = s.createCriteria(PolicyDO.class).list();
            int resultSize = result.size();
            assertEquals("Policy objects should be fetched properly", this.dataMgr.getNbPolicies(), resultSize);
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that a cached policy object name does not need to be unique.
     */
    public void testPolicyCacheDataObjectsNonUniqueName() {
        Transaction t = null;
        boolean exThrown = false;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            t = s.beginTransaction();
            final String nonUniqueName = "nonUniqueName";
            PolicyDO pol1 = new PolicyDO();
            pol1.setId(new Long(50000));
            pol1.setFullName(nonUniqueName);
            s.save(pol1);
            PolicyDO pol2 = new PolicyDO();
            pol2.setId(new Long(50001));
            pol2.setFullName(nonUniqueName);
            s.save(pol2);
            t.commit();
        } catch (HibernateException e) {
            exThrown = true;
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
        assertFalse("Duplicate policy name in policy cache should be accepted", exThrown);
    }

    /**
     * This test verifies that the PF integration works fine. If policies are
     * currently deployed, they should be stored appropriately in the policy
     * cache upon data refresh.
     */
    //    public void testIntegrationWithPF() {
    //        Session s = this.getActivityDateSource().getSession();
    //        Session cacheSession = null;
    //        Transaction t = null;
    //        try {
    //            t = s.beginTransaction();
    //            this.dataMgr.deletePolicies(s);
    //            t.commit();
    //        } catch (HibernateException e) {
    //            fail("Deletion of policies should not fail");
    //        } finally {
    //            HibernateUtils.closeSession(s, null);
    //        }
    //        s = getPolicyDataSource().getSession();
    //        try {
    //            t = s.beginTransaction();
    //            deleteDeploymentEntities(s);
    //            t.commit();
    //        } catch (HibernateException e) {
    //            fail("Deletion of deployment records should not fail");
    //        } finally {
    //            HibernateUtils.closeSession(s, null);
    //        }
    //        //Create new policies
    //        LifecycleManager lm = (LifecycleManager)
    // ComponentManagerFactory.getComponentManager().getComponent(LifecycleManager.COMP_INFO);
    //        try {
    //            List deployList = new ArrayList();
    //            Map idMap = new HashMap();
    //            final int nbDeploy = 20;
    //            for (int i = 0; i < 25; i++) {
    //                DevelopmentEntity de = lm.getEntityForName(EntityType.POLICY, "policy" +
    // i);
    //                if (i < nbDeploy) {
    //                    //Only the first 20 development entities are approved
    //                    de.setStatus(DevelopmentStatus.APPROVED);
    //                    lm.saveEntity(de);
    //                    idMap.put(de.getId(), de.getName());
    //                    deployList.add(de);
    //                } else {
    //                    lm.saveEntity(de);
    //                }
    //            }
    //            Calendar now = Calendar.getInstance();
    //            now.add(Calendar.MINUTE, 5);
    //            Date in5 = new Date(now.getTimeInMillis());
    //            lm.deployEntities(deployList, in5, DeploymentType.PRODUCTION);
    //
    //            //Invalidates the policy cache and lets it fetch the deployed
    //            // policies
    //            PolicyCacheImpl policyCache = getPolicyCache();
    //            policyCache.invalidate();
    //            cacheSession = policyCache.getDataSource().getSession();
    //            t = cacheSession.beginTransaction();
    //            policyCache.refreshData(cacheSession, in5);
    //            t.commit();
    //
    //            //Now, fetch the latest policy cache and see if the right things
    //            // are in there
    //            s = getActivityDateSource().getSession();
    //            List freshestPolicies = s.createCriteria(PolicyDO.class).list();
    //            assertEquals("The number of policies in the cache should match the number
    // of deployed policies as of now", nbDeploy, freshestPolicies.size());
    //            Iterator it = freshestPolicies.iterator();
    //            while (it.hasNext()) {
    //                PolicyDO policy = (PolicyDO) it.next();
    //                assertNotNull("The id of the deployed policies and cached policies should
    // match", idMap.get(policy.getId()));
    //                String name = (String) idMap.get(policy.getId());
    //                assertEquals("The name of the deployed policies and the cached policies
    // should match", name, policy.getName());
    //            }
    //        } catch (EntityManagementException e1) {
    //            fail("Entity manipulation should not fail");
    //        } catch (HibernateException e1) {
    //            HibernateUtils.rollbackTransation(t, null);
    //        } finally {
    //            if (cacheSession != null) {
    //                HibernateUtils.closeSession(cacheSession, null);
    //            }
    //            if (s != null) {
    //                HibernateUtils.closeSession(s, null);
    //            }
    //        }
    //    }
}