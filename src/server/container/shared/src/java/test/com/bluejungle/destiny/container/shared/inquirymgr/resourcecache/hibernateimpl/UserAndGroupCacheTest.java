package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

/*
 * Created on Mar 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.SampleDataMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserDO;
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
 * This is the test class for the user and user group resource cache.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/UserAndGroupCacheTest.java#1 $
 */

public class UserAndGroupCacheTest extends DACContainerSharedTestCase {

    /**
     * Returns an instance of the host cache
     * 
     * @return an instance of the host cache
     */
    private UserAndGroupsCacheImpl getUserCache() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IResourceCache.ACTIVITY_DATASOURCE_CONFIG_ATTR, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("userCache4Test", UserAndGroupsCacheImpl.class.getName(), IConfigurable.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        UserAndGroupsCacheImpl cache = (UserAndGroupsCacheImpl) compMgr.getComponent(compInfo);
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
        Session s = this.getActivityDateSource().getSession();
        SampleDataMgr dataMgr = new SampleDataMgr();
        dataMgr.deleteUsersAndGroups(s);
        HibernateUtils.closeSession(s, null);
    }

    public void testUserAndGroupCacheClassBasics() {
        IResourceCache cache = getUserCache();
        assertNotNull("The user and user group cache should be created", cache);
        assertTrue("The user and user group cache should be initializable", cache instanceof IInitializable);
        assertTrue("The user and user group cache should be logEnabled", cache instanceof ILogEnabled);
        assertTrue("The user and user group cache should be disposable", cache instanceof IDisposable);
        assertTrue("The user and user group cache should be configurable", cache instanceof IConfigurable);
        assertTrue("The user and user group cache should extend the basic resource cache class", cache instanceof BaseResourceCacheImpl);
    }

    /*
     * public void testHighVolume() throws SynchronizeArchiveException {
     * startLDAPAdapter(); UserAndGroupsCacheImpl cache = getUserCacheForPerf();
     * try { cache.refreshData(null); } catch (HibernateException e) {
     * e.printStackTrace(); } }
     */

    /**
     * This test verifies that the data object for user and user groups cache
     * are properly set.
     */
    public void testUserAndGroupCacheDataObjects() {
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            SampleDataMgr dataMgr = new SampleDataMgr();
            dataMgr.createUsersAndGroups(s);
            s.clear();
            UserDO iannis = (UserDO) s.createCriteria(UserDO.class).add(Expression.eq("displayName", "Iannis")).uniqueResult();
            assertEquals("UserDO name property should match", "Iannis", iannis.getDisplayName());
            //Make sure the groups are properly fetched - Iannis should be in
            // engineering and DCC groups
//            int groupSize = iannis.getGroups().size();
//            assertEquals("User groups should be fetched properly", 2, groupSize);
//            Iterator it = iannis.getGroups().iterator();
//            while (it.hasNext()) {
//                IUserGroup group = (IUserGroup) it.next();
//                assertTrue("User groups should be fetched properly", "Engineering".equals(group.getName()) || "DCC".equals(group.getName()));
//            }
//
//            //Jessica should be in techpubs and contractor group
//            UserDO jessica = (UserDO) s.createCriteria(UserDO.class).add(Expression.eq("displayName", "Jessica")).uniqueResult();
//            groupSize = jessica.getGroups().size();
//            assertEquals("User groups should be fetched properly", 2, groupSize);
//            it = jessica.getGroups().iterator();
//            while (it.hasNext()) {
//                IUserGroup group = (IUserGroup) it.next();
//                assertTrue("User groups should be fetched properly", "Contractors".equals(group.getName()) || "Tech Pub".equals(group.getName()));
//            }
            dataMgr.deleteUsersAndGroups(s);
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        }
    }

    /**
     * This test verifies that a default user is properly inserted in the cached
     * table.
     */
    public void testUserAndGroupCacheDefaultUsers() {
       /* startLDAPAdapter();
        IResourceCache userCache = getUserCache();
        userCache.invalidate();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            //Try querying for the unknown user
            assertNotNull("There should be an system user in the cache", s.get(UserDO.class, IDSubject.SYSTEM_USER_ID));
            assertNotNull("There should be an unknown user in the cache", s.get(UserDO.class, IHasId.UNKNOWN_ID));

            //Clean the unknown user, make sure it gets created again
            SampleDataMgr dataMgr = new SampleDataMgr();
            dataMgr.deleteUsersAndGroups(s);
            assertNull("The system user should be deleted from the the cache", s.get(UserDO.class, IDSubject.SYSTEM_USER_ID));
            assertNull("The unknown user should be deleted from the the cache", s.get(UserDO.class, IHasId.UNKNOWN_ID));
            userCache.invalidate();
            s.clear();
            assertNotNull("The system user should always be in the cache", s.get(UserDO.class, IDSubject.SYSTEM_USER_ID));
            assertNotNull("The unknown user should always be in the cache", s.get(UserDO.class, IHasId.UNKNOWN_ID));
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }

    /**
     * This test verifies that HQL statements work correctly against the many to
     * many relationship between users and user groups.
     */
    public void testUserAndGroupCacheHQLQuery() {
        /*  There's a bit of a question mark here.  Iannis took out the "groups" field from UserDO.hbm.xml explicitly.  Therefore, this test cannot work.  I'm not sure what affect this will have on the Inquiry API and Reporter.  Will have to find out */
        /*Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            SampleDataMgr dataMgr = new SampleDataMgr();
            dataMgr.createUsersAndGroups(s);
            s.clear();
            //Try querying for various groups
            Query marketingQuery = queryForGroupUsers(s, "Marketing");
            List result = marketingQuery.list();
            assertEquals("The query for marketing group should return expected number of records", 2, result.size());

            Query engrQuery = queryForGroupUsers(s, "Engineering");
            result = engrQuery.list();
            assertEquals("The query for engr group should return expected number of records", 9, result.size());
            dataMgr.deleteUsersAndGroups(s);
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }

    /**
     * This test verifies that the PF library can fetch records correctly
     */
    public void testUserAndGroupPFIntegration() {
      /*  startLDAPAdapter();
        IResourceCache userCache = getUserCache();
        UserAndGroupsCacheImpl implObj = (UserAndGroupsCacheImpl) userCache;
        assertNotNull("the user and user group cache object should be created", userCache);
        assertNotNull("the policy domain helper should be created", implObj.getDomainObjectHelper());
        userCache.invalidate();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();

            //Try querying for various groups
            Query engrQuery = queryForGroupUsers(s, "test.bluejungle.com:Groups:Immigration");
            List result = engrQuery.list();
            assertEquals("The query for test.bluejungle.com:Groups:Immigration host group should return the right number of records", 2, result.size());
            SampleDataMgr dataMgr = new SampleDataMgr();
            dataMgr.deleteHosts(s);
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
    public void testUserAndGroupPFIntegrationMultiDataRefresh() {
       /* startLDAPAdapter();
        IResourceCache userCache = getUserCache();
        UserAndGroupsCacheImpl implObj = (UserAndGroupsCacheImpl) userCache;
        userCache.invalidate();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();

            //Try querying for a given host
            Query userQuery = queryForUser(s, "ihanen@bluejungle.com");
            List result = userQuery.list();
            assertEquals("The query for user 'ihanen@bluejungle.com' should return one record", 1, result.size());
            UserDO user = (UserDO) result.get(0);
            assertNotNull("There should be one record returned", user);
            final Long matchId = user.getOriginalId();

            //Deletes data and fetches again
            SampleDataMgr dataMgr = new SampleDataMgr();
            dataMgr.deleteUsersAndGroups(s);
            userCache.invalidate();
            user = (UserDO) userQuery.uniqueResult();
            assertNotNull("There should be one record returned", user);
            assertEquals("Row Ids should match after refresh", matchId, user.getOriginalId());

            //Refreshes without deleting (real update scenario)
            userCache.invalidate();
            user = (UserDO) userQuery.uniqueResult();
            assertNotNull("There should be one record returned", user);
            assertEquals("Row Ids should match after refresh", matchId, user.getOriginalId());

            //Refreshes multiple times in a row
            userCache.invalidate();
            userCache.invalidate();
            userCache.invalidate();
            userCache.invalidate();
            userCache.invalidate();
            user = (UserDO) userQuery.uniqueResult();
            assertNotNull("There should be one record returned", user);
            assertEquals("Row Ids should match after refresh", matchId, user.getOriginalId());
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        } */
    }

    /**
     * This test verifies that host cache data gets updated properly, on top of
     * previous data that could be different.
     *  
     */
    public void testUserAndGroupPFIntegrationNewDataRefresh() {
      /*  startLDAPAdapter();
        IResourceCache userCache = getUserCache();
        UserAndGroupsCacheImpl implObj = (UserAndGroupsCacheImpl) userCache;
        userCache.invalidate();
        Transaction t = null;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            //Try querying for a given host
            Query kengUserQuery = queryForUser(s, "keng@bluejungle.com");
            UserDO user = (UserDO) kengUserQuery.uniqueResult();
            assertNotNull("Query for keng@bluejungle.com should return a record", user);
            //Take Keng Lim off the group and change its name to... Iannis
            t = s.beginTransaction();
//            Set groups = user.getGroups();
//            assertEquals("Keng Lim should belong to the right number of groups", 9, groups.size());
//            groups.clear();
//            user.setFirstName("Iannis");
//            user.setDisplayName("Iannis took over Keng Lim's seat!");
//            user.setLastName("Hanen");
//            s.update(user);
//            t.commit();
//            final Long matchId = user.getId();
//
//            //Invalidate - This will reload fresh data
//            userCache.invalidate();
//            s.clear();
//            user = (UserDO) kengUserQuery.uniqueResult();
//            assertNotNull("The record should have been refreshed", user);
//            assertEquals("The right number of groups should be restored", 9, user.getGroups().size());
//
//            //Partial group member ship removal and refresh
//            Iterator it = user.getGroups().iterator();
//            UserGroupDO firstGroup = (UserGroupDO) it.next();
//            t = s.beginTransaction();
//            user.getGroups().remove(firstGroup);
            t.commit();

            //Invalidate again - This should reload data and put the missing
            // group in place
            //Invalidate - This will reload fresh data
            userCache.invalidate();
            s.clear();
            user = (UserDO) kengUserQuery.uniqueResult();
            assertNotNull("The record should have been refreshed", user);
            //assertEquals("The right number of groups should be restored", 9, user.getGroups().size());
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }*/
    }

    /**
     * Builds an HQL query for a particular user group name
     * 
     * @param s
     *            Hibernate session to use
     * @param groupName
     *            name of the user group to retrieve
     * @return a Hibernate query object
     */
    private Query queryForGroupUsers(Session s, String groupName) throws HibernateException {
        Query q = s.createQuery("select user from UserDO user, UserGroupDO userGroup where LOWER(userGroup.name) = :groupName and userGroup = some elements (user.groups)");
        q.setString("groupName", groupName.toLowerCase());
        return q;
    }

    /**
     * Builds an HQL query for a particular user display name
     * 
     * @param s
     *            Hibernate session to use
     * @param userName
     *            name of the host to retrieve
     * @return a Hibernate query object
     */
    private Query queryForUser(Session s, String userName) throws HibernateException {
        Query q = s.createQuery("select user from UserDO user where user.displayName = :userName");
        q.setString("userName", userName);
        return q;
    }
}
