/*
 * Created on Jan 18, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import java.util.BitSet;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.Dialect;

import org.junit.Before;
import org.junit.Test;

import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.nextlabs.destiny.container.dac.datasync.DataSyncManagerTest.DataSyncManagerMock;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTask.SyncType;
import com.nextlabs.destiny.container.shared.inquirymgr.ReportDataHolderManager;
import com.nextlabs.destiny.container.shared.inquirymgr.ReportDataHolderManagerMemory;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/nextlabs/destiny/container/dac/datasync/DataSyncManagerTaskTest.java#1 $
 */

public class DataSyncManagerTaskTest extends BaseDACComponentTestCase{
    ReportDataHolderManager reportDataHolderManager;
    
    public DataSyncManagerTaskTest() {
        super("DataSyncManagerTaskTest");
    }

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
    }
    
    private HashMapConfiguration generateDefaultConfig(){
        if (reportDataHolderManager == null) {
            IComponentManager manager = ComponentManagerFactory.getComponentManager();
            HashMapConfiguration reportDataHolderonfig = new HashMapConfiguration();
            reportDataHolderonfig.setProperty(ReportDataHolderManager.DATA_SOURCE_PARAM, 
                    super.getActivityDataSource());
            reportDataHolderManager = manager.getComponent(ReportDataHolderManager.class, reportDataHolderonfig);
        }
        
        Calendar sameTime = Calendar.getInstance();
        sameTime.add(Calendar.SECOND, 8);
        
        BitSet everyday = new BitSet(7);
        everyday.set(0, 7, true);

        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IDataSyncManager.DATASOURCE_CONFIG_PARAM, super.getActivityDataSource());

        config.setProperty(IDataSyncManager.SYNC_TIME_INTERVAL_PARAM, null);
        config.setProperty(IDataSyncManager.SYNC_TIME_OF_DAY_PARAM, sameTime);
        config.setProperty(IDataSyncManager.SYNC_TIMEOUT_PARAM, 20000L);

        config.setProperty(IDataSyncManager.INDEXES_REBUILD_TIME_OF_DAY, sameTime);
        config.setProperty(IDataSyncManager.INDEXES_REBUILD_DAY_OF_WEEK, everyday);
        config.setProperty(IDataSyncManager.INDEXES_REBUILD_DAY_OF_MONTH, null);
        config.setProperty(IDataSyncManager.INDEXES_REBUILD_AUTO_REBUILD_INDEXES, true);
        config.setProperty(IDataSyncManager.INDEXES_REBUILD_TIMEOUT_PARAM, 1000L);

        config.setProperty(IDataSyncManager.ARCHIVE_TIME_OF_DAY, sameTime);
        config.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_WEEK, everyday);
        config.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_MONTH, null);
        config.setProperty(IDataSyncManager.ARCHIVE_DAYS_OF_DATA_TO_KEEP, 27);
        config.setProperty(IDataSyncManager.ARCHIVE_AUTO_ARCHIVE, true);
        config.setProperty(IDataSyncManager.ARCHIVE_TIMEOUT_PARAM, 1000L);
        
        config.setProperty(IDataSyncManager.REPORT_DATA_HOLDER_MANAGER_PARAM, reportDataHolderManager);
        
        return config;
    }
    
    public void testMultiTask() throws HibernateException{
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        DataSyncManager dataSyncManager = manager.getComponent(
                DataSyncManagerMock.class, generateDefaultConfig());
        reportDataHolderManager.remove(DataSyncManager.ACTIVE_MANAGER_KEY);
        reportDataHolderManager.remove(DataSyncManager.SYNC_ACTION_KEY);
        reportDataHolderManager.remove(DataSyncManager.SYNC_PROGRESS_KEY);
        
        isRunning.set(false);
        
        dataSyncManager.register(new SyncTaskMock(SyncType.SYNC));
        dataSyncManager.register(new SyncTaskMock(SyncType.ARCHIVE));
        dataSyncManager.register(new SyncTaskMock(SyncType.INDEXES_REBUILD));
        
        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static AtomicBoolean isRunning = new AtomicBoolean();
    
    private class SyncTaskMock implements IDataSyncTask{
        SyncType type;
        
        SyncTaskMock(SyncType type){
            this.type = type;
        }
        
        public SyncType getType() {
            return type;
        }

        public void run(Session session, long timeout, IConfiguration config) {
            System.out.println("run " + type);
            assertFalse(isRunning.get());
            isRunning.set(true);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                fail("Interrupted" + e);
            }
            assertTrue(isRunning.get());
            isRunning.set(false);
            System.out.println("end " + type);
        }
    }
}
