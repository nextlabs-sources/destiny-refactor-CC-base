/*
 * Created on Jun 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.ReporterDataHolderDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/inquirymgr/ReportDataHolderManagerTest.java#1 $
 */

public class ReportDataHolderManagerTest extends BaseContainerSharedTestCase{

    private static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.ACTIVITY_REPOSITORY);
    }
    
    @Override
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }
    
    protected String getComponentName() {
        return "dac";
    }
    
    private static final String KEY = "ReportDataHolderManagerTest";
    
    protected IConfiguration getConfiguation(){
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(ReportDataHolderManager.DATA_SOURCE_PARAM, getDataSource());
        return config;
    }
    
    private ReportDataHolderManager createManager(){
        final ReportDataHolderManager mgr = new ReportDataHolderManager();
        mgr.setConfiguration(getConfiguation());
        mgr.setLog(LogFactory.getLog(ReportDataHolderManager.class));
        mgr.init();
        return mgr;
    }
    
    public void test1() throws HibernateException{
        final ReportDataHolderManager mgr = createManager();
        
        String value = "1";
        
        mgr.remove(KEY);
        assertFalse("entry should be removed", mgr.containsKey(KEY));
        
        assertFalse("can only update an existed entry", mgr.update(KEY, value));
        
        assertTrue(mgr.add(KEY, value));
        assertTrue(mgr.containsKey(KEY));
        assertFalse(mgr.add(KEY, value));
        assertEquals(value, mgr.get(KEY));
        
        value = "2";
        assertTrue(mgr.update(KEY, value));
        assertTrue(mgr.containsKey(KEY));
        assertEquals(value, mgr.get(KEY));
        
        value = "3";
        assertTrue(mgr.saveOrUpdate(KEY, value));
        assertEquals(value, mgr.get(KEY));
        assertNotNull(mgr.remove(KEY));
        
        value = "4";
        assertTrue(mgr.saveOrUpdate(KEY, value));
        assertEquals(value, mgr.get(KEY));
    }
    
    public void test2() throws HibernateException{
        final ReportDataHolderManager mgr1 = createManager();
        final ReportDataHolderManager mgr2 = createManager();
        
        
        String value = "1";
        
        mgr1.remove(KEY);
        assertFalse("entry should be removed", mgr1.containsKey(KEY));
        assertNull(mgr1.remove(KEY));
        assertFalse("entry should be removed", mgr2.containsKey(KEY));

        assertTrue(mgr1.add(KEY, value));
        assertTrue(mgr2.containsKey(KEY));
        assertFalse(mgr1.add(KEY, value));
        assertFalse(mgr2.add(KEY, value));
        assertEquals(value, mgr1.get(KEY));
        assertEquals(value, mgr2.get(KEY));
        
        value = "2";
        assertTrue(mgr1.update(KEY, value));
        assertEquals(value, mgr2.get(KEY));
        
        value = "3";
        assertTrue(mgr2.saveOrUpdate(KEY, value));
        assertEquals(value, mgr1.get(KEY));
        assertNotNull(mgr1.remove(KEY));
        assertNull(mgr2.remove(KEY));
    }
    
    
    public void testConcurrentAddRemoveDifferentKey() throws InterruptedException{
        final ReportDataHolderManager mgr1 = createManager();
        final ReportDataHolderManager mgr2 = createManager();

        final int loop = 100;
        
        Thread t1 = new Thread(createConcurrentAddRemoveRunnable(mgr1, KEY,       loop, "1a", "2a"));
        Thread t2 = new Thread(createConcurrentAddRemoveRunnable(mgr2, KEY + "2", loop, "1b", "2b"));
        Thread t3 = new Thread(createConcurrentAddRemoveRunnable(mgr1, KEY + "3", loop, "1c", "2c"));
        Thread t4 = new Thread(createConcurrentAddRemoveRunnable(mgr2, KEY + "4", loop, "1d", "2d"));
        
        t1.start();
        t2.start();
        t3.start();
        t4.run();
        
        t1.join();
        t2.join();
        t3.join();
    }
    
    private Runnable createConcurrentAddRemoveRunnable(
            final ReportDataHolderManager mgr, 
            final String key,
            final int loop,
            final String value1,
            final String value2) {
        return new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < loop; i++) {
                        if (mgr.add(key, value1)) {
                            assertEquals(value1, mgr.get(key));
                            assertTrue(mgr.containsKey(key));
                            assertTrue(mgr.update(key, value2));
                            assertEquals(value2, mgr.get(key));
                            assertEquals(value2, mgr.remove(key));
                        }
                    }
                } catch (HibernateException e) {
                    
                }
            }
        };
    }
    
    public void testHoldSession() throws HibernateException, InterruptedException{
        final ReportDataHolderManager mgr = createManager();
        
        final String value = "1";
        
        mgr.saveOrUpdate(KEY, value);
        
        GetKeyRunnable r= new GetKeyRunnable(mgr, KEY, null);
        Thread thread = new Thread(r);
        
        Session session = getDataSource().getCountedSession();
        Transaction t = session.beginTransaction();
        ReporterDataHolderDO result = (ReporterDataHolderDO)session.get(ReporterDataHolderDO.class, KEY);
        assertEquals(value, result.getValue());
        assertNull(r.value);
        thread.start();
        Thread.yield();
        Thread.sleep(1000);
        assertEquals(value, r.value);
        thread.join();
        t.commit();
        
    }
    
    private class GetKeyRunnable implements Runnable{
        final ReportDataHolderManager mgr; 
        final String key;
        final LockMode lockMode;
        
        private String value;
        
        public GetKeyRunnable(ReportDataHolderManager mgr, String key, LockMode lockMode) {
            super();
            this.mgr = mgr;
            this.key = key;
            this.lockMode = lockMode;
            value = null;
        }
        
        public void run() {
            try {
                value = mgr.get(key);
            } catch (HibernateException e) {
                fail(e.toString());
            }
        }
    }
    

    /**
     * Returns a data source object that can be used to create sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(
                DestinyRepository.ACTIVITY_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for ProfileManager.");
        }

        return dataSource;
    }
}
