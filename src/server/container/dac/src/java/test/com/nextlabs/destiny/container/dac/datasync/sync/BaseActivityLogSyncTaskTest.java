/*
 * Created on Jun 16, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.HibernateLogWriter;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.nextlabs.destiny.container.dac.datasync.BaseDatasyncTestCase;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTask;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTaskUpdate;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/nextlabs/destiny/container/dac/datasync/sync/BaseActivityLogSyncTaskTest.java#1 $
 */

public abstract class BaseActivityLogSyncTaskTest<T extends SyncTaskBase<?>> extends BaseDatasyncTestCase{
    private static final Log LOG = LogFactory.getLog(BaseActivityLogSyncTaskTest.class);
    
    private final String sourceTableName;
    
    public BaseActivityLogSyncTaskTest(String testName, String sourceTableName) {
        super(testName);
        this.sourceTableName = sourceTableName;
    }
    
    protected Session session;
    protected T syncTaskBase;
    
    @Override
    protected void setUp() throws Exception{
        super.setUp();
        session = getActivityDataSource().getSession();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (session != null) {
            session.close();
            session = null;
        }
    }
    
    protected int getPALSize() throws SQLException, HibernateException {
        Statement statement =session.connection().createStatement();
        
        int palSize;
        try {
            ResultSet rs = statement.executeQuery("select count(*) from " + sourceTableName);
            try {
                assertTrue(rs.next());
                palSize = rs.getInt(1);
            } finally {
                rs.close();
            }
        } finally {
            statement.close();
        }
        return palSize;
    }
    
    protected abstract T createSyncTaskBase();
    
    protected void sync() throws HibernateException {
        long startTime = System.currentTimeMillis();
        syncTaskBase = createSyncTaskBase();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IDataSyncTask.DIALECT_CONFIG_PARAMETER, getDialect());
        config.setProperty(IDataSyncTask.TASK_UPDATE_PARAMETER, new IDataSyncTaskUpdate(){

            public void addFail(int size) throws IllegalStateException {
            }

            public void addSuccess(int size) throws IllegalStateException {
            }

            public boolean alive() {
                return true;
            }

            public long getUpdateInterval() {
                return Long.MAX_VALUE;
            }

            public void setPrefix(String prefix) {
            }

            public void setTotalSize(int size) throws IllegalStateException {
            }

            public void reset() {
                
            }
            
        });

        SyncTask syncTask = new SyncTaskMock();
        Connection c = session.connection();
        boolean r = syncTaskBase.run(session, c, config, syncTask);
        try {
            c.close();
        } catch (SQLException e) {
            LOG.error("SQL exception: " + e);
        }
        
        LOG.info("sync, " + syncTaskBase.getParsedCount() + " items took "
                 + ConsoleDisplayHelper.formatTime(System.currentTimeMillis() - startTime));
        assertTrue(r);
    }
    
    protected HibernateLogWriter initHibernateLogWriter() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        ComponentInfo<HibernateLogWriter> persistenceLogWriterCompInfo =
                new ComponentInfo<HibernateLogWriter>(
                        HibernateLogWriter.COMP_NAME,
                        HibernateLogWriter.class, 
                        ILogWriter.class, 
                        LifestyleType.SINGLETON_TYPE);
        HibernateLogWriter writer = componentManager.getComponent(persistenceLogWriterCompInfo);
        assertNotNull(writer);
        return writer;

    }
    
    public void testClearAllReportLog() throws HibernateException, SQLException{
        clearAllReportLog();
        setSyncFlag(null);
    }
    
    protected abstract void clearAllReportLog() throws HibernateException, SQLException;
    
    
    
    public void testNormalSync() throws HibernateException, SQLException{
        int palSize = getPALSize();
        
        sync();
        assertEquals(palSize, syncTaskBase.getParsedCount());
        assertGoodAndBase(palSize, 0);
    }
    
    public void testAlreadySync() throws HibernateException {
        sync();
        
        int count = syncTaskBase.getParsedCount();
        assertEquals(0, count);
        assertGoodAndBase(0, 0);
    }
    
    public void testMessSyncFlag() throws HibernateException, SQLException{
        setSyncFlag(null);
    }

    protected void setSyncFlag(Boolean value) throws HibernateException, SQLException {
        LOG.info("start setting sync flag to " + value);
        Connection connection = session.connection();
        PreparedStatement statement =
                connection.prepareStatement("update " + sourceTableName + " set sync_done = ?");
        
        try {
            if(value == null){
                statement.setNull(1, java.sql.Types.BIT);
            }else{
                statement.setBoolean(1, value);
            }
            statement.execute();
            connection.commit();
        } finally {
            statement.close();
        }
        LOG.info("done setting sync flag to " + value);
    }
    
    public void testSyncWithDuplicatedData() throws HibernateException, SQLException{
        int palSize = getPALSize();
        sync();
        
        assertEquals(palSize, syncTaskBase.getParsedCount());
        assertGoodAndBase(0, palSize);
        
    }
    
    public void testEmptyReportTableButSyncIsTrue() throws HibernateException, SQLException{
        testClearAllReportLog();
        setSyncFlag(true);
        
        sync();
        
        assertEquals(0, syncTaskBase.getParsedCount());
        assertGoodAndBase(0, 0);
    }
    
    protected abstract void assertGoodAndBase(int goodCount, int badCount);
    
    protected class SyncTaskMock extends SyncTask{

        @Override
        protected int getRemainingTime() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void run(Session session, long timeout, IConfiguration config) {
        }
        
    }
}
