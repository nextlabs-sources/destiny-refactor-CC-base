/*
 * Created on Jun 10, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import static org.junit.Assert.*;

import java.util.BitSet;
import java.util.Calendar;
import java.util.Random;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.Dialect;

import org.junit.Before;
import org.junit.Test;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateRepositoryMock;
import com.nextlabs.destiny.container.shared.inquirymgr.ReportDataHolderManagerMemory;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/nextlabs/destiny/container/dac/datasync/DataSyncManagerTest.java#1 $
 */

public class DataSyncManagerTest {
    private static final int RANDOM_TEST_COUNT = 5000;
    
    public static class DataSyncManagerMock extends DataSyncManager{
        private static final ComponentInfo<DataSyncManager> COMP_INFO =
            new ComponentInfo<DataSyncManager>(
                DataSyncManagerMock.class.getName(),
                DataSyncManagerMock.class,
                IDataSyncManager.class,
                LifestyleType.SINGLETON_TYPE);
        
        @Override
        Dialect getDialect() throws IllegalArgumentException {
            return null;
        }

        @Override
        public void start() {
            //do nothing
        }

        @Override
        public ComponentInfo<DataSyncManager> getComponentInfo() {
            return COMP_INFO;
        }

        @Override
        protected boolean trafficLight(long taskTimeout) {
            return true;
        }
    }
    
    private class HibernateRepositoryMock2 extends HibernateRepositoryMock{

        @Override
        public Session getSession() throws HibernateException {
            return null;
        }

        @Override
        public void closeCurrentSession() throws HibernateException {
            //do nothing
        }
    }
    
    @Before
    public void setUp(){
    }
    
    private HashMapConfiguration generateDefaultConfig(){
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IDataSyncManager.DATASOURCE_CONFIG_PARAM, new HibernateRepositoryMock2());

        config.setProperty(IDataSyncManager.SYNC_TIME_INTERVAL_PARAM, 1000L);
        config.setProperty(IDataSyncManager.SYNC_TIME_OF_DAY_PARAM, null);
        config.setProperty(IDataSyncManager.SYNC_TIMEOUT_PARAM, 20000L);

        config.setProperty(IDataSyncManager.INDEXES_REBUILD_TIME_OF_DAY, Calendar.getInstance());
        BitSet bits = new BitSet(7);
        bits.set(Calendar.SUNDAY - 1);
        bits.set(Calendar.FRIDAY - 1);
        config.setProperty(IDataSyncManager.INDEXES_REBUILD_DAY_OF_WEEK, bits);
        config.setProperty(IDataSyncManager.INDEXES_REBUILD_DAY_OF_MONTH, null);
        config.setProperty(IDataSyncManager.INDEXES_REBUILD_AUTO_REBUILD_INDEXES, true);
        config.setProperty(IDataSyncManager.INDEXES_REBUILD_TIMEOUT_PARAM, 1000L);
        

        config.setProperty(IDataSyncManager.ARCHIVE_TIME_OF_DAY, Calendar.getInstance());
        config.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_WEEK, null);
        
        bits = new BitSet(31);
        bits.set(1 - 1);
        bits.set(10 - 1);
        bits.set(31 - 1);
        config.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_MONTH, bits);
        config.setProperty(IDataSyncManager.ARCHIVE_DAYS_OF_DATA_TO_KEEP, 27);
        config.setProperty(IDataSyncManager.ARCHIVE_AUTO_ARCHIVE, false);
        config.setProperty(IDataSyncManager.ARCHIVE_TIMEOUT_PARAM, 1000L);
        
        config.setProperty(IDataSyncManager.REPORT_DATA_HOLDER_MANAGER_PARAM, 
                new ReportDataHolderManagerMemory());
        
        return config;
    }
    
    
    @Test
    public void delayBefore(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();

        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());

        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        time.clear();
        now.clear();
        time.set(Calendar.HOUR, 3);
        time.set(Calendar.MINUTE, 4);
        time.set(Calendar.SECOND, 5);
        
        //one second before
        now.set(2000, Calendar.JANUARY, 2, 3, 4, 4);
        long result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals(1000L, result);

        //one minute before
        now.set(2000, Calendar.JANUARY, 2, 3, 3, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals((long)(60 * 1000), result);

        //one hour before
        now.set(2000, Calendar.JANUARY, 2, 2, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals((long)(60 * 60 * 1000), result);

        //one day before
        now.set(2000, Calendar.JANUARY, 1, 3, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals(0L, result);

        //almost one day before
        now.set(2000, Calendar.JANUARY, 1, 3, 4, 6);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals((long)(24 * 60 * 60 * 1000 - 1000), result);
    }
    
    @Test
    public void randomDelay(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());

        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        Random r = new Random();
        
        for (int i = 0; i < RANDOM_TEST_COUNT; i++) {
            time.setTimeInMillis(Math.abs(r.nextLong()));
            now.setTimeInMillis(Math.abs(r.nextLong()));
            long result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
            
            //25 hours but not 24 since the DST
            assertTrue(time.getTime().toString() + " + vs " + now.getTime().toString() + " : " + result,
                    result < 25 * 60 * 60 * 1000);
        }
    }
    
    @Test
    public void delayAfter(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());
        
        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        time.clear();
        now.clear();
        time.set(Calendar.HOUR, 3);
        time.set(Calendar.MINUTE, 4);
        time.set(Calendar.SECOND, 5);

        //one second after
        now.set(2008, Calendar.JANUARY, 2, 3, 4, 6);
        long result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals((long)(24 * 60 * 60 * 1000 - 1000), result);

        //one minute after
        now.set(2008, Calendar.JANUARY, 2, 3, 5, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals((long)(24 * 60 * 60 * 1000 - 60 * 1000), result);

        //one hour after
        now.set(2008, Calendar.JANUARY, 2, 4, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals((long)(24 * 60 * 60 * 1000 - 60 * 60 * 1000), result);

        //one day after
        now.set(2008, Calendar.JANUARY, 3, 3, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals(0L, result);

        //almost one day after
        now.set(2008, Calendar.JANUARY, 3, 3, 4, 4);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals(1000L, result);
    }
    
    @Test
    public void delayWeekBeforeEveryDay(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());
        
        BitSet bits = new BitSet(7);
        bits.set(0, 7, true);
        
        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        time.clear();
        now.clear();
        time.set(Calendar.HOUR, 3);
        time.set(Calendar.MINUTE, 4);
        time.set(Calendar.SECOND, 5);

        //one second before
        now.set(2008, Calendar.JANUARY, 2, 3, 4, 4);
        long result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals(1000L, result);

        //one minute after
        now.set(2008, Calendar.JANUARY, 2, 3, 5, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals((long)(24 * 60 * 60 * 1000 - 60 * 1000), result);

        //one hour after
        now.set(2008, Calendar.JANUARY, 2, 4, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals((long)(24 * 60 * 60 * 1000 - 60 * 60 * 1000), result);

        //one day after
        now.set(2008, Calendar.JANUARY, 3, 3, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
        assertEquals(0L, result);

        //almost one day after
        now.set(2008, Calendar.JANUARY, 3, 3, 4, 4);
        result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
    }
    
    @Test
    public void delayWeekAfterEveryDay(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());

        BitSet bits = new BitSet(7);
        bits.set(0, 7, true);
        
        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        time.clear();
        now.clear();
        time.set(Calendar.HOUR, 3);
        time.set(Calendar.MINUTE, 4);
        time.set(Calendar.SECOND, 5);
        
        //one second after
        now.set(2008, Calendar.JANUARY, 2, 3, 4, 6);
        long result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals((long)(24 * 60 * 60 * 1000 - 1000), result);

        //one minute after
        now.set(2008, Calendar.JANUARY, 2, 3, 5, 5);
        result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals((long)(24 * 60 * 60 * 1000 - 60 * 1000), result);

        //one hour after
        now.set(2008, Calendar.JANUARY, 2, 4, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals((long)(24 * 60 * 60 * 1000 - 60 * 60 * 1000), result);

        //one day after
        now.set(2008, Calendar.JANUARY, 3, 3, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals(0L, result);

        //almost one day after
        now.set(2008, Calendar.JANUARY, 3, 3, 4, 4);
        result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals(1000L, result);
    }
    
    @Test
    public void delayWeekOnceAWeek(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());

        BitSet bits = new BitSet(7);
        bits.set(Calendar.SATURDAY - 1);
        
        Calendar time = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        time.clear();
        now.clear();
        time.set(Calendar.HOUR, 3);
        time.set(Calendar.MINUTE, 4);
        time.set(Calendar.SECOND, 5);

        // 2008, Calendar.JANUARY, 2 is Wednesday
        now.set(2008, Calendar.JANUARY, 2, 3, 4, 5);
        long result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals((long)((24 * 3) * 60 * 60 * 1000), result);
        
        //same time
        now.set(2008, Calendar.JANUARY, 5, 3, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals(0L, result);

        // a second after
        now.set(2008, Calendar.JANUARY, 5, 3, 4, 6);
        result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
        assertEquals((long)(24 * 7) * 60 * 60 * 1000 - 1000), result);
    }
    
    @Test
    public void randomDelayWeekEveryday(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());

        BitSet bits = new BitSet(7);
        bits.set(0, 7, true);
        
        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        Random r = new Random();
        
        for (int i = 0; i < RANDOM_TEST_COUNT; i++) {
            time.setTimeInMillis(Math.abs(r.nextLong()));
            now.setTimeInMillis(Math.abs(r.nextLong()));
            long result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
            
            //25 hours but not 24 since the DST
            assertTrue(time.getTime().toString() + " + vs " + now.getTime().toString() + " : " + result,
                    result < (24 + 1) * 60 * 60 * 1000);
        }
    }
    
    @Test
    public void randomDelayWeek4timesPerWeek(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());

        BitSet bits = new BitSet(7);
        bits.set(Calendar.MONDAY - 1);
        bits.set(Calendar.WEDNESDAY - 1);
        bits.set(Calendar.FRIDAY - 1);
        bits.set(Calendar.SUNDAY - 1);
        
        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        Random r = new Random();
        
        for (int i = 0; i < RANDOM_TEST_COUNT; i++) {
            time.setTimeInMillis(Math.abs(r.nextLong()));
            now.setTimeInMillis(Math.abs(r.nextLong()));
            long result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
            
            //49 hours but not 48 since the DST
            assertTrue(time.getTime().toString() + " + vs " + now.getTime().toString() + " : " + result,
                    result < (2 * 24 + 1) * 60 * 60 * 1000);
        }
    }
    
    @Test
    public void randomDelayWeekOnceAWeek(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());

        BitSet bits = new BitSet(7);
        bits.set(Calendar.SATURDAY - 1);
        
        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        Random r = new Random();
        
        for (int i = 0; i < RANDOM_TEST_COUNT; i++) {
            time.setTimeInMillis(Math.abs(r.nextLong()));
            now.setTimeInMillis(Math.abs(r.nextLong()));
            long result = dataSyncManager.calcuateTimeOfDayDelayWeek(time, now, bits);
            
            //49 hours but not 48 since the DST
            assertTrue(time.getTime().toString() + " + vs " + now.getTime().toString() + " : " + result,
                    result < (7 * 24 + 1) * 60 * 60 * 1000);
        }
    }
    
    @Test
    public void delayWeekMonth(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());

        BitSet bits = new BitSet(7);
        bits.set(1 - 1);
        bits.set(15 - 1);
        bits.set(31 - 1);
        
        Calendar time = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        time.clear();
        now.clear();
        time.set(Calendar.HOUR, 3);
        time.set(Calendar.MINUTE, 4);
        time.set(Calendar.SECOND, 5);

        // 2008 February is a leap year
        now.set(2008, Calendar.FEBRUARY, 2, 3, 4, 5);
        long result = dataSyncManager.calcuateTimeOfDayDelayMonth(time, now, bits);
        assertEquals((long)((24 * (15 - 2)) * 60 * 60 * 1000), result);
        
        now.set(2008, Calendar.FEBRUARY, 28, 3, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelayMonth(time, now, bits);
        assertEquals((long)(24 * 60 * 60 * 1000), result);
        
        now.set(2008, Calendar.FEBRUARY, 29, 3, 4, 5);
        result = dataSyncManager.calcuateTimeOfDayDelayMonth(time, now, bits);
        assertEquals(0L, result);

        now.set(2008, Calendar.FEBRUARY, 29, 3, 4, 6);
        result = dataSyncManager.calcuateTimeOfDayDelayMonth(time, now, bits);
        assertEquals((long)(24 * 60 * 60 * 1000 - 1000), result);
    }
    
    @Test
    public void noArchiveDayIsDefined(){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        HashMapConfiguration config = generateDefaultConfig();
        config.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_MONTH, null);
        config.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_WEEK, null);
        
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, config);
        
        Calendar time= Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        time.clear();
        now.clear();
        time.set(Calendar.HOUR, 3);
        time.set(Calendar.MINUTE, 4);
        time.set(Calendar.SECOND, 5);
        
        Random r = new Random();
        
        for (int i = 0; i < RANDOM_TEST_COUNT; i++) {
            now.set(now.getMaximum(Calendar.DAY_OF_MONTH), r.nextInt(now.getMaximum(Calendar.MONTH)), 2, 3, 4, 5);
            long result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
            assertEquals(0L, result);
        }
        
        for (int i = 0; i < RANDOM_TEST_COUNT; i++) {
            now.set(now.getMaximum(Calendar.DAY_OF_MONTH), r.nextInt(now.getMaximum(Calendar.MONTH)), 2, 3, 4, 3);
            long result = dataSyncManager.calcuateTimeOfDayDelay(time, now);
            assertEquals(2000L, result);
        }
    }
    
    @Test
    public void test2() throws InterruptedException{
        IComponentManager manager = ComponentManagerFactory.getComponentManager();

        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());
        DataSynctaskMock task = new DataSynctaskMock();
        assertEquals(0, task.executed);
        dataSyncManager.register(task);
        
        Thread.sleep(20000);
        assertTrue(task.executed > 17);
        assertTrue(task.executed < 23);
        System.out.println(task.executed);
    }
    
    private class DataSynctaskMock implements IDataSyncTask{
        int executed;
        
        private DataSynctaskMock(){
            executed = 0;
        }
        
        public SyncType getType() {
            return SyncType.SYNC;
        }

        public void run(Session session, long timeout, IConfiguration config) {
            executed++;
        }
        
    }
}
