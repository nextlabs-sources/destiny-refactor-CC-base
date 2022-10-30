package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

/* Created on Mar 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HostDO;
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
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;

/**
 * This is the application cache test class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/ApplicationCacheTest.java#1 $
 */

public class HostAndGroupCacheTest extends DACContainerSharedTestCase {

    private SampleDataMgr dataMgr;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.dataMgr = new SampleDataMgr();
        Session s = this.getActivityDateSource().getSession();
        this.dataMgr.deleteHosts(s);
        HibernateUtils.closeSession(s, null);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Session s = this.getActivityDateSource().getSession();
        this.dataMgr.deleteHosts(s);
        HibernateUtils.closeSession(s, null);
        super.tearDown();
    }

    /**
     * Returns an instance of the host cache
     * 
     * @return an instance of the host cache
     */
    private HostAndGroupCacheImpl getHostCache() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IResourceCache.ACTIVITY_DATASOURCE_CONFIG_ATTR, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("hostCache4Test", HostAndGroupCacheImpl.class.getName(), IConfigurable.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        HostAndGroupCacheImpl cache = (HostAndGroupCacheImpl) compMgr.getComponent(compInfo);
        return cache;
    }

    /**
     * This test verifies the basic about the host cache class.
     */
    public void testHostCacheClassBasics() {
        HostAndGroupCacheImpl hostCache = getHostCache();
        assertNotNull("Host cache should be created", hostCache);
        assertTrue("Host cache should be initializable", hostCache instanceof IInitializable);
        assertTrue("Host cache should be logEnabled", hostCache instanceof ILogEnabled);
        assertTrue("Host cache should be disposable", hostCache instanceof IDisposable);
        assertTrue("Host cache should be configurable", hostCache instanceof IConfigurable);
        assertTrue("Host cache should extend the basic resource cache class", hostCache instanceof BaseResourceCacheImpl);
    }

    /**
     * This test verifies that the data objects for host and host groups are
     * working properly.
     */
    public void testHostCacheDataObjects() {
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.dataMgr.deleteHosts(s);
            this.dataMgr.createHosts(s);
            s.clear();
            HostDO borabora = (HostDO) s.createCriteria(HostDO.class).add(Expression.eq("name", "borabora")).uniqueResult();
            assertNotNull(borabora);
            assertEquals("HostDO name property should match", "borabora", borabora.getName());
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that a cached host object name is unique.
     */
    public void testHostCacheDataObjectsUniqueName() {
        Session s = null;
        Transaction t = null;
        boolean exThrown = false;
        try {
            s = this.getActivityDateSource().getSession();
            t = s.beginTransaction();
            final String nonUniqueName = "nonUniqueName";
            HostDO host1 = new HostDO();
            host1.setId(new Long(50000));
            host1.setName(nonUniqueName);
            s.save(host1);
            HostDO host2 = new HostDO();
            host2.setId(new Long(50001));
            host2.setName(nonUniqueName);
            s.save(host2);
            t.commit();
        } catch (HibernateException e) {
            exThrown = true;
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
        assertTrue("Duplicate host name in host cache should not be accepted", exThrown);
    }

    /**
     * This test verifies that the unknown host is properly inserted in the
     * cached table.
     */
    public void testHostCacheUnknownHost() {
        /*startLDAPAdapter();
        IResourceCache hostCache = getHostCache();
        hostCache.invalidate();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();

            //Try querying for the unknown user
            Object unknownHost = s.get(HostDO.class, IHasId.UNKNOWN_ID);
            assertNotNull("There should be an unknown host in the cache", unknownHost);

            //Clean the unknown user, make sure it gets created again
            SampleDataMgr dataMgr = new SampleDataMgr();
            dataMgr.deleteHosts(s);
            unknownHost = s.get(HostDO.class, IHasId.UNKNOWN_ID);
            assertNull("The unknown HostDO should be deleted from the the cache", unknownHost);
            hostCache.invalidate();
            s.clear();
            unknownHost = s.get(HostDO.class, IHasId.UNKNOWN_ID);
            assertNotNull("The unknown HostDO should always be in the cache", unknownHost);
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }

    /**
     * This test verifies that the PF library can fetch records correctly
     */
    public void testHostPFIntegration() {
        /*startLDAPAdapter();
        IResourceCache hostCache = getHostCache();
        HostAndGroupCacheImpl implObj = (HostAndGroupCacheImpl) hostCache;
        assertNotNull("the host cache object should be created", hostCache);
        assertNotNull("the policy domain helper should be created", implObj.getDomainObjectHelper());
        hostCache.invalidate();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            assertEquals("The correct number of hosts should be written", 79, implObj.getHostCount());
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }

    /**
     * This test verifies that the process of updating records in the Database
     * occurs properly.
     */
    public void testHostPFIntegrationMultiDataRefresh() {
        /*startLDAPAdapter();
        IResourceCache hostCache = getHostCache();
        HostAndGroupCacheImpl implObj = (HostAndGroupCacheImpl) hostCache;
        hostCache.invalidate();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();

            //Try querying for a given host
            Query hostQuery = queryForHost(s, "STBARTS.bluejungle.com");
            List result = hostQuery.list();
            assertEquals("The query for STBARTS.bluejungle.com host should return one record", 1, result.size());
            HostDO host = (HostDO) result.get(0);
            assertNotNull("There should be one record returned", host);
            final Long matchId = host.getId();

            //Deletes data and fetches again
            this.dataMgr.deleteHosts(s);
            hostCache.invalidate();
            host = (HostDO) hostQuery.uniqueResult();
            assertNotNull("There should be one record returned", host);
            assertEquals("Row Ids should match after refresh", matchId, host.getId());

            //Refreshes without deleting (real update scenario)
            hostCache.invalidate();
            host = (HostDO) hostQuery.uniqueResult();
            assertNotNull("There should be one record returned", host);
            assertEquals("Row Ids should match after refresh", matchId, host.getId());

            //Refreshes multiple times in a row
            hostCache.invalidate();
            hostCache.invalidate();
            hostCache.invalidate();
            hostCache.invalidate();
            hostCache.invalidate();
            host = (HostDO) hostQuery.uniqueResult();
            assertNotNull("There should be one record returned", host);
            assertEquals("Row Ids should match after refresh", matchId, host.getId());

        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }

    /**
     * This test verifies that host cache data gets updated properly, on top of
     * previous data that could be different.
     *  
     */
    public void testHostPFIntegrationNewDataRefresh() {
       /* startLDAPAdapter();
        IResourceCache hostCache = getHostCache();
        HostAndGroupCacheImpl implObj = (HostAndGroupCacheImpl) hostCache;
        hostCache.invalidate();
        Transaction t = null;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            //Try querying for a given host
            Query hostQuery = queryForHost(s, "STBARTS.bluejungle.com");
            HostDO host = (HostDO) hostQuery.uniqueResult();
            assertNotNull("The query for STBARTS.bluejungle.com should return a record", host);
            //Take stbarts off the group and change its name
            t = s.beginTransaction();
            host.setName("myNewHostName");
            s.update(host);
            t.commit();
            final Long matchId = host.getId();

            //Invalidate - This will reload fresh data
            hostCache.invalidate();
            s.clear();
            host = (HostDO) hostQuery.uniqueResult();
            assertNotNull("The record should have been refreshed", host);
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }

    /**
     * Builds an HQL query for a particular host name
     * 
     * @param s
     *            Hibernate session to use
     * @param hostName
     *            name of the host to retrieve
     * @return a Hibernate query object
     */
    private Query queryForHost(Session s, String hostName) throws HibernateException {
        Query q = s.createQuery("select host from HostDO host where host.name = :hostName");
        q.setString("hostName", hostName);
        return q;
    }

}
