/*
 * Created on Mar 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.util.Iterator;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ApplicationDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IApplicationGroup;
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

public class ApplicationCacheTest extends DACContainerSharedTestCase {

    private SampleDataMgr dataMgr;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.dataMgr = new SampleDataMgr();
        Session s = this.getActivityDateSource().getSession();
        this.dataMgr.deleteApplications(s);
        this.dataMgr.createApplicationsAndGroups(s);
        s.close();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Session s = this.getActivityDateSource().getSession();
        this.dataMgr.deleteApplications(s);
        s.close();
        super.tearDown();
    }

    /**
     * Returns an instance of the application cache
     * 
     * @return an instance of the ApplicationCacheImpl cache
     */
    private ApplicationCacheImpl getApplicationCache() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IResourceCache.ACTIVITY_DATASOURCE_CONFIG_ATTR, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("applicationCache4Test", ApplicationCacheImpl.class.getName(), IConfigurable.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        ApplicationCacheImpl cache = (ApplicationCacheImpl) compMgr.getComponent(compInfo);
        return cache;
    }

    /**
     * This test verifies the basic about the application cache class.
     */
    public void testApplicationAndGroupCacheClassBasics() {
        ApplicationCacheImpl appCache = getApplicationCache();
        assertNotNull("Application cache should be created", appCache);
        assertTrue("Application cache should be initializable", appCache instanceof IInitializable);
        assertTrue("Application cache should be logEnabled", appCache instanceof ILogEnabled);
        assertTrue("Application cache should be disposable", appCache instanceof IDisposable);
        assertTrue("Application cache should be configurable", appCache instanceof IConfigurable);
        assertTrue("Application cache should extend the basic resource cache class", appCache instanceof BaseResourceCacheImpl);
    }

    /**
     * This test verifies the data deletion from the cache table
     */
   /* public void testApplicationAndGroupCacheDataDeletion() {
        IHibernateRepository ds = this.getActivityDateSource();
        Transaction t = null;
        Session s = null;
        try {
            s = ds.getSession();
            t = s.beginTransaction();
            ApplicationCacheImpl cache = getApplicationCache();
            cache.deleteData(s);
            t.commit();
            s.clear();
            List result = s.createCriteria(ApplicationDO.class).list();
            assertEquals("Application cache deletion should empty application cache tables", 0, result.size());
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("No Hibernate exception should be thrown");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }*/

    /**
     * This test verifies that the data objects for application and application
     * groups are working properly.
     */
    public void testApplicationAndGroupCacheDataObjects() {
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            ApplicationDO excel = (ApplicationDO) s.createCriteria(ApplicationDO.class).add(Expression.eq("name", "excel")).uniqueResult();
            assertEquals("ApplicationDO name property should match", "excel", excel.getName());

            //Make sure that groups for the excel application are properly
            // fetched
            int groupSize = excel.getGroups().size();
            assertEquals("Application groups should be fetched properly", 1, groupSize);

            //njstar should be in games and email
            ApplicationDO njstar = (ApplicationDO) s.createCriteria(ApplicationDO.class).add(Expression.eq("name", "njstar")).uniqueResult();
            groupSize = njstar.getGroups().size();
            assertEquals("Application groups should be fetched properly", 2, groupSize);
            Iterator it = njstar.getGroups().iterator();
            while (it.hasNext()) {
                IApplicationGroup group = (IApplicationGroup) it.next();
                assertTrue("Application groups should be fetched properly", "Games".equals(group.getName()) || "Email".equals(group.getName()));
            }
        } catch (HibernateException e) {
            fail("Error when performing hibernate operation : " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that a cached application object name is unique.
     */
    public void testApplicationAndGroupCacheDataObjectsUniqueName() {
        Session s = null;
        Transaction t = null;
        boolean exThrown = false;
        try {
            s = this.getActivityDateSource().getSession();
            t = s.beginTransaction();
            final String nonUniqueName = "nonUniqueName";
            ApplicationDO app1 = new ApplicationDO();
            app1.setId(new Long(50000));
            app1.setName(nonUniqueName);
            s.save(app1);
            ApplicationDO app2 = new ApplicationDO();
            app2.setId(new Long(50001));
            app2.setName(nonUniqueName);
            s.save(app2);
            t.commit();
        } catch (HibernateException e) {
            exThrown = true;
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
        assertTrue("Duplicate Application name in application cache should not be accepted", exThrown);
    }
}