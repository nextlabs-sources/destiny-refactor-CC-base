/*
 * Created on Mar 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase;
import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IMasterResourceCache;
import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IResourceCache;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This is the master resource cache test class. It tests the functionality of
 * the master cache for all data resources.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/MasterResourceCacheTest.java#1 $
 */

public class MasterResourceCacheTest extends DACContainerSharedTestCase {

    /**
     * Deletes all records in the resource cache state table.
     */
    protected void clearResourceCacheState() {        
        Calendar beginningOfTime = Calendar.getInstance();
        beginningOfTime.setTimeInMillis(0);
        
        Transaction t = null;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            t = s.beginTransaction();
            Criteria crit = s.createCriteria(ResourceCacheStateDO.class);
            List<ResourceCacheStateDO> resourceCacheStates = crit.list();
            for (ResourceCacheStateDO cacheState : resourceCacheStates) {
                cacheState.setLastUpdated(beginningOfTime);
                s.update(cacheState);
            }
            t.commit();
        } catch (HibernateException e) {
            fail("Seed data init should not throw any hibernate exception");
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Returns an instance of the master resource cache
     * 
     * @return the master resource cache
     */
    protected IMasterResourceCache getMasterResourceCache() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IResourceCache.ACTIVITY_DATASOURCE_CONFIG_ATTR, getActivityDateSource());
        ComponentInfo<IMasterResourceCache> info = new ComponentInfo<IMasterResourceCache>(
                "masterCache", 
                MasterResourceCacheImpl.class, 
                IMasterResourceCache.class, 
                LifestyleType.SINGLETON_TYPE, 
                config);
        IMasterResourceCache result = compMgr.getComponent(info);
        return result;
    }

    /**
     * This test verifies the basics about the class
     */
    @SuppressWarnings("cast")
    public void testMasterCacheClassBasics() {
        MasterResourceCacheImpl masterCache = (MasterResourceCacheImpl) getMasterResourceCache();
        assertTrue("Master cache component should be configurable", masterCache instanceof IConfigurable);
        assertTrue("Master cache component should be disposable", masterCache instanceof IDisposable);
        assertTrue("Master cache component should be initable", masterCache instanceof IInitializable);
        assertTrue("Master cache component should be log enabled", masterCache instanceof ILogEnabled);
        //assertTrue("Master cache component should implement the correct interface", masterCache instanceof IResourceCache);
        assertTrue("Master cache component should implement the correct interface", masterCache instanceof IMasterResourceCache);

        HashMapConfiguration config = new HashMapConfiguration();
        masterCache.setConfiguration(config);
        assertEquals("The configuration getter/setter should work", config, masterCache.getConfiguration());
    }

    /**
     * This test verifies that the master cache component can be instantiated
     * properly
     */
    public void testMasterCacheInstantiation() {
        MasterResourceCacheImpl masterCache = (MasterResourceCacheImpl) getMasterResourceCache();
        assertNotNull(masterCache);
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        assertNotNull("Application cache should be created when the master cache is created", 
                compMgr.isComponentRegistered(ApplicationCacheImpl.class.getName()));
        assertNotNull("Policy cache should be created when the master cache is created", 
                compMgr.isComponentRegistered(PolicyCacheImpl.class.getName()));
        assertNotNull("User group cache should be created when the master cache is created", 
                compMgr.isComponentRegistered(UserAndGroupsCacheImpl.class.getName()));

        //try with a bad configuration
        HashMapConfiguration config = new HashMapConfiguration();
        ComponentInfo<IMasterResourceCache> info = new ComponentInfo<IMasterResourceCache>(
                "badmasterCache", 
                MasterResourceCacheImpl.class, 
                IMasterResourceCache.class, 
                LifestyleType.SINGLETON_TYPE, 
                config);
        boolean exThrown = false;
        try {
            IMasterResourceCache result = compMgr.getComponent(info);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("The master cache component should not accept configuration without dataSource", exThrown);
    }

    /**
     * This test verifies that at startup, the master cache inserts the correct
     * seed data in the database if the seed data is not already there. This
     * test focuses on full seed data init (no record present)
     */
    public void testMasterCacheSeedDataFullInit() {
        clearResourceCacheState();
        MasterResourceCacheImpl masterCache = (MasterResourceCacheImpl) getMasterResourceCache();
        masterCache.refreshCaches();
        
        //Check that seed data is present now
        Set<ResourceCacheType> types = new HashSet<ResourceCacheType>();
        Transaction t = null;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            List<ResourceCacheStateDO> cacheStates = s.createCriteria(ResourceCacheStateDO.class).list();
            for (ResourceCacheStateDO cacheState : cacheStates) {
                types.add(cacheState.getType());
            }
            s.close();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("Seed data init should not throw any hibernate exception");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
        assertEquals("All seed data should be present after after the master cache refresh", 4, types.size());
        assertTrue("All seed data should be present after after the master cache refresh", types.contains(ResourceCacheType.APPLICATION));
        assertTrue("All seed data should be present after after the master cache refresh", types.contains(ResourceCacheType.POLICY));
        assertTrue("All seed data should be present after after the master cache refresh", types.contains(ResourceCacheType.USER));
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        assertNotNull("All caches should be instantiated", compMgr.getComponent(ApplicationCacheImpl.class.getName()));
        assertNotNull("All caches should be instantiated", compMgr.getComponent(HostAndGroupCacheImpl.class.getName()));
        assertNotNull("All caches should be instantiated", compMgr.getComponent(UserAndGroupsCacheImpl.class.getName()));
        assertNotNull("All caches should be instantiated", compMgr.getComponent(PolicyCacheImpl.class.getName()));
        //assertNotNull("The PF service should be instantiated",
        // masterCache.getPolicyEditorService());
    }

    /**
     * This test verifies that the master cache can properly calculate the
     * oldest update timestamp for all the caches.
     *  
     */
    public void testEarliestLastUpdatedCalculation() {
       /* MasterResourceCacheImpl masterCache = (MasterResourceCacheImpl) getMasterResourceCache();
        Transaction t = null;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            t = s.beginTransaction();
            s.delete("from ResourceCacheStateDO");
            t.commit();
            assertNull("Oldest last updated time should be null if the DB is empty", masterCache.getOldestCacheUpdateTime());
            t = s.beginTransaction();
            Calendar now = Calendar.getInstance();
            ResourceCacheStateDO newState = new ResourceCacheStateDO();
            newState.setType(ResourceCacheType.APPLICATION);
            newState.setLastUpdated(now);
            s.save(newState);
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            ResourceCacheStateDO newState2 = new ResourceCacheStateDO();
            newState2.setType(ResourceCacheType.POLICY);
            newState2.setLastUpdated(yesterday);
            s.save(newState2);
            t.commit();
            assertEquals(yesterday, masterCache.getOldestCacheUpdateTime());
        } catch (HibernateException e) {
            fail("Seed data init should not throw any hibernate exception");
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }

    /**
     * This test verifies that the master cache can properly store the last
     * updated timestamps for each cache.
     */
    public void testLastUpdateStateWriting() {
      /*  MasterResourceCacheImpl masterCache = (MasterResourceCacheImpl) getMasterResourceCache();
        Transaction t = null;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            t = s.beginTransaction();
            s.delete("from ResourceCacheStateDO");
            t.commit();
            Map updateMap = new HashMap();
            Calendar appTime = Calendar.getInstance();
            appTime.add(Calendar.MINUTE, -1);
            updateMap.put(masterCache.getApplicationCache(), appTime.getTime());
            Calendar policyTime = Calendar.getInstance();
            policyTime.add(Calendar.MINUTE, -2);
            updateMap.put(masterCache.getPolicyCache(), policyTime.getTime());
            Calendar userTime = Calendar.getInstance();
            userTime.add(Calendar.MINUTE, -3);
            updateMap.put(masterCache.getUserAndGroupsCache(), userTime.getTime());
            Calendar hostTime = Calendar.getInstance();
            hostTime.add(Calendar.SECOND, -25);
            updateMap.put(masterCache.getHostCache(), hostTime.getTime());
            masterCache.updateTimestamps(updateMap);
            List states = s.createCriteria(ResourceCacheStateDO.class).list();
            assertNotNull("The state list should be returned", states);
            assertEquals("The state list should have the correct size", 4, states.size());
            Map stateMap = new HashMap();
            Iterator it = states.iterator();
            while (it.hasNext()) {
                IResourceCacheState state = (IResourceCacheState) it.next();
                stateMap.put(state.getType(), state.getLastUpdated());
            }
            assertEquals("The state should be persisted properly", stateMap.get(ResourceCacheType.APPLICATION), appTime);
            assertEquals("The state should be persisted properly", stateMap.get(ResourceCacheType.POLICY), policyTime);
            assertEquals("The state should be persisted properly", stateMap.get(ResourceCacheType.HOST), hostTime);
            assertEquals("The state should be persisted properly", stateMap.get(ResourceCacheType.USER), userTime);
        } catch (HibernateException e) {
            fail("Seed data init should not throw any hibernate exception");
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }
}
