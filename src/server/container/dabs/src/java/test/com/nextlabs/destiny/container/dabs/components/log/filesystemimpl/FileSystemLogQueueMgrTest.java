/*
 * Created on Feb 12, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log.filesystemimpl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyActivityLogEntryTestData;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.PolicyAssistantLogEntryTestData;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.domain.log.TrackingLogEntryTestData;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.destiny.container.dabs.components.log.ILogQueueMgr;
import com.nextlabs.destiny.container.dabs.components.log.IThreadPool;
import com.nextlabs.destiny.container.dabs.components.log.LogQueueException;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;


/**
 * Test case to test
 * com.nextlabs.destiny.container.dabs.components.log.filesystemimpl.FileSystemLogQueueMgr
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/nextlabs/destiny/container/dabs/components/log/filesystemimpl/FileSystemLogQueueMgrTest.java#1 $
 */

public class FileSystemLogQueueMgrTest extends BaseDabsComponentTest {

    private static FileSystemLogQueueMgr logQueueMgrToTest;
    private static final int nbPolicyEntries1 = 97;
    private static final int nbPolicyEntries2 = 89;
    private static final int nbPolicyEntries3 = 91;
    private static final int nbTrackingEntries1 = 103;
    private static final int nbTrackingEntries2 = 107;
    private static final int nbTrackingEntries3 = 111;
    private static final int nbTrackingEntries4 = 101;
    private static final int nbTrackingEntries5 = 113;
    private static final PolicyActivityLogEntry[] policyLogEntries1 = PolicyActivityLogEntryTestData.generateRandom(nbPolicyEntries1);
    private static final PolicyActivityLogEntry[] policyLogEntries2 = PolicyActivityLogEntryTestData.generateRandom(nbPolicyEntries2);
    private static final PolicyActivityLogEntry[] policyLogEntries3 = PolicyActivityLogEntryTestData.generateRandom(nbPolicyEntries3);
    private static final TrackingLogEntry[] trackingLogEntries1 = TrackingLogEntryTestData.generateRandom(nbTrackingEntries1);
    private static final TrackingLogEntry[] trackingLogEntries2 = TrackingLogEntryTestData.generateRandom(nbTrackingEntries2);
    private static final TrackingLogEntry[] trackingLogEntries3 = TrackingLogEntryTestData.generateRandom(nbTrackingEntries3);
    private static final TrackingLogEntry[] trackingLogEntries4 = TrackingLogEntryTestData.generateRandom(nbTrackingEntries4);
    private static final TrackingLogEntry[] trackingLogEntries5 = TrackingLogEntryTestData.generateRandom(nbTrackingEntries5);
    private static String LOG_DIR;
    private Map<Long, String> testLogTracker = new Hashtable<Long, String>(); // used to keep track of all the queued logs
    private List<Map<Long, BaseLogEntry[]>> retrievedLogsTracker = new Vector<Map<Long, BaseLogEntry[]>>(); // used to keep track of all the logs retrieved for writing
    private List<Long> completedLogsTracker = new Vector<Long>(); // used to keep track of all the log file IDs that can be dequeued

    /**
     * Constructor
     * @param arg0
     */
    public FileSystemLogQueueMgrTest() {
        super(FileSystemLogQueueMgrTest.class.getName());
    }
    
    private class DummyLogWriter implements ILogWriter{
        private void doNothing() {
            try {
                while (true) {
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        public void log(PolicyActivityLogEntry[] logEntries) throws DataSourceException {
            doNothing();
        }

        public void log(PolicyActivityLogEntryV2[] logEntries) throws DataSourceException {
            doNothing();
        }

        public void log(PolicyActivityLogEntryV3[] logEntries) throws DataSourceException {
            doNothing();
        }

        public void log(PolicyActivityLogEntryV4[] logEntries) throws DataSourceException {
            doNothing();
        }

        public void log(PolicyActivityLogEntryV5[] logEntries) throws DataSourceException {
            doNothing();
        }
        
        public void log(PolicyAssistantLogEntry[] logEntries) throws DataSourceException {
            doNothing();
        }

        public void log(TrackingLogEntry[] logEntries) throws DataSourceException {
            doNothing();
        }

        public void log(TrackingLogEntryV2[] logEntries) throws DataSourceException {
            doNothing();
        }

        public void log(TrackingLogEntryV3[] logEntries) throws DataSourceException {
            doNothing();
        }
    }
    
    private static class DummaryLogInsertTaskFactory extends LogInsertTaskFactory{

		public DummaryLogInsertTaskFactory(ILogWriter persistenceLogWriter,
				ILogQueueMgr logQueueMgr, int logInsertTaskIdleTime) {
			super(persistenceLogWriter, logQueueMgr, logInsertTaskIdleTime);
		}

		@Override
		public Runnable getTask() {
			return new Runnable(){
				public void run() {
					//do nothing
					try {
						while (true) {
							Thread.sleep(2000);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}
    }
    
    public static class FileSystemLogQueueMgrMod extends FileSystemLogQueueMgr {
		@Override
		protected LogInsertTaskFactory createTaskFactory(ILogWriter persistenceLogWriter,
				ILogQueueMgr logQueueMgr, int logInsertTaskIdleTime) {
			return new DummaryLogInsertTaskFactory(persistenceLogWriter, logQueueMgr,
					logInsertTaskIdleTime);
		}
	}

    /**
     * @see com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        
        HashMapConfiguration fileSystemThreadPoolConfig = new HashMapConfiguration();
        fileSystemThreadPoolConfig.setProperty(IThreadPool.KEEP_ALIVE_TIME_PARAM, 120000);
        fileSystemThreadPoolConfig.setProperty(IThreadPool.MAXIMUM_POOL_SIZE_PARAM, 32);
        IThreadPool fileSystemThreadPool = cm.getComponent(
				FileSystemThreadPool.COMP_INFO, fileSystemThreadPoolConfig);
        
        HashMapConfiguration logQueueMgrConfig = new HashMapConfiguration();
//        LOG_DIR = "c:/builds/destiny-mainline/log/logqueue";
        LOG_DIR = System.getProperty("build.root.dir") + "\\log\\logqueue";
        new File(LOG_DIR).mkdir();
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.LOG_QUEUE_LOCATION, LOG_DIR);
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.TIMEOUT_CHECKER_FREQUENCY, 15000);
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.LOG_TIMEOUT, 30000);
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.MAX_BYTES_UPLOAD_SIZE_PARAM, 1024*1024L);
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.MAX_HANDLE_FILE_SIZE_PER_THREAD_PARAM, 1024*1024*100L);
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.FILE_SYSTEM_THREAD_POOL_PARAM, fileSystemThreadPool);
        logQueueMgrConfig.setProperty(LogInsertTaskFactory.LOG_INSERT_TASK_IDLE_TIME, 60);
        logQueueMgrConfig.setProperty(LogInsertTaskFactory.PERSISTENCE_LOG_WRITER_PARAM, new DummyLogWriter());
        
        ComponentInfo<FileSystemLogQueueMgr> logQueueMgrCompInfo = 
            new ComponentInfo<FileSystemLogQueueMgr>(
                ILogQueueMgr.COMP_NAME, 
                FileSystemLogQueueMgrMod.class, 
                ILogQueueMgr.class, 
                LifestyleType.SINGLETON_TYPE, 
                logQueueMgrConfig);
        logQueueMgrToTest = cm.getComponent(logQueueMgrCompInfo);
    }

    /**
     * @see com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        File logDir = new File(LOG_DIR);
        File[] fileList = logDir.listFiles();
        if (fileList != null){
            for (int i = 0; i < fileList.length; i++){
                fileList[i].delete();
            }
        }
        logDir.delete();
    }

    /**
     * Tests the queueing of the policy activity logs
     * 
     * @throws LogQueueException 
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void testQueueAndDequeuePolicyLogs() throws LogQueueException, IOException, ClassNotFoundException {
        new File(LOG_DIR).mkdir();
        assertEquals("queueing of policy activity log failed", new Long(1), logQueueMgrToTest.queueLogs(policyLogEntries1));
        testLogTracker.put(new Long(0), "policy1");
        File logDir = new File(LOG_DIR);
        File[] logFileList = logDir.listFiles();
        assertEquals("there should only be one log file written to the file system", 1, logFileList.length);
        File logFile = logFileList[0];
        FileInputStream fileInputStream = new FileInputStream(logFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ObjectInputStream logInputStream = new ObjectInputStream(bufferedInputStream);
        PolicyActivityLogEntry[] retrievedEntries = (PolicyActivityLogEntry[])logInputStream.readObject();
        for (int i = 0; i < policyLogEntries1.length; i++){
            assertEquals("the policy activity logs retrieved from the file system is not equal to the original one", 
                         policyLogEntries1[i], 
                         retrievedEntries[i]);
        }
        fileInputStream.close();
        bufferedInputStream.close();
        logInputStream.close();
        logQueueMgrToTest.lockLogFile(new Long(1));
        try {
            logQueueMgrToTest.dequeueLogs(new Long(2));
        } catch (Exception e){
            assertTrue("the LogQueueMgr should have thrown a LogQueueException", e instanceof LogQueueException);
        }
        assertTrue("dequeueing of policy activity failed", logQueueMgrToTest.dequeueLogs(new Long(1)));
        testLogTracker.remove(new Long(1));
        assertEquals("there should not be any log files on the file system", 0, logDir.listFiles().length);
        try {
            logQueueMgrToTest.dequeueLogs(new Long(1));
        } catch (Exception e){
            assertTrue("the LogQueueMgr should have thrown a LogQueueException", e instanceof LogQueueException);
        }
    }
    
    /**
     * Tests the queueing of the document activity logs
     * @throws LogQueueException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws ClassNotFoundException 
     */
    public void testQueueAndDequeueTrackingLogs() throws LogQueueException, FileNotFoundException, IOException, ClassNotFoundException {
        assertEquals("queueing of tracking activity log failed", new Long(1), logQueueMgrToTest.queueLogs(trackingLogEntries1)); 
        testLogTracker.put(new Long(1), "tracking1");
        File logDir = new File(LOG_DIR);
        File[] logFileList = logDir.listFiles();
        assertEquals("there should only be one log file written to the file system", 1, logFileList.length);
        File logFile = logFileList[0];
        FileInputStream fileInputStream = new FileInputStream(logFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ObjectInputStream logInputStream = new ObjectInputStream(bufferedInputStream);
        TrackingLogEntry[] retrievedEntries = (TrackingLogEntry[])logInputStream.readObject();
        for (int i = 0; i < trackingLogEntries1.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries1[i], 
                         retrievedEntries[i]);
        }
        fileInputStream.close();
        bufferedInputStream.close();
        logInputStream.close();
        
        logQueueMgrToTest.lockLogFile(new Long(1));
        try {
            logQueueMgrToTest.dequeueLogs(new Long(0));
        } catch (Exception e){
            assertTrue("the LogQueueMgr should have thrown a LogQueueException", e instanceof LogQueueException);
        }
        assertTrue("dequeueing of policy activity failed", logQueueMgrToTest.dequeueLogs(new Long(1)));
        testLogTracker.remove(new Long(1));
        assertEquals("there should not be any log files on the file system", 0, logDir.listFiles().length);
        try {
            logQueueMgrToTest.dequeueLogs(new Long(1));
        } catch (Exception e){
            assertTrue("the LogQueueMgr should have thrown a LogQueueException", e instanceof LogQueueException);
        }
    }
            
    /**
     * Tests the dequeueing of the Tracking logs
     * @throws LogQueueException 
     */
    public void testDequeueTrackingLogs() throws LogQueueException {

    }
    
    /**
     * Tests the retrieval of policy logs to write
     * @throws LogQueueException 
     * @throws IOException 
     */
    public void testGetPolicyLogsToWrite() throws LogQueueException, IOException {
        logQueueMgrToTest.queueLogs(policyLogEntries1);
        testLogTracker.put(new Long(3), "policy1");
        Map<Long, BaseLogEntry[]> logReceived = logQueueMgrToTest.getLogsToWrite();
        assertEquals("There should only be one set of policy logs to write", 1, logReceived.size());
        PolicyActivityLogEntry[] logEntries = (PolicyActivityLogEntry[])logReceived.get(new Long(1));
        for (int i = 0; i < policyLogEntries1.length; i++){
            assertEquals("the policy activity log " + i + " retrieved from the file system is not equal to the original one", 
                         policyLogEntries1[i], 
                         logEntries[i]);
        }
        File logDir = new File(LOG_DIR);
        File[] fileList = logDir.listFiles();
        for (int i = 0; i < fileList.length; i++){
            fileList[i].delete();
        }
        logDir.delete();
    }
    
    /**
     * Tests the retrieval of tracking logs to write
     * @throws LogQueueException  
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void testGetTrackingLogsToWrite() throws LogQueueException, FileNotFoundException, IOException {
        new File(LOG_DIR).mkdir();
        logQueueMgrToTest.queueLogs(trackingLogEntries1);
        testLogTracker.put(new Long(4), "tracking1");
        Map<Long, BaseLogEntry[]> logReceived = logQueueMgrToTest.getLogsToWrite();
        assertEquals("There should only be one set of policy logs to write", 1, logReceived.size());
        TrackingLogEntry[] logEntries = (TrackingLogEntry[])logReceived.get(new Long(1));
        for (int i = 0; i < trackingLogEntries1.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries1[i], 
                         logEntries[i]);
        }
    }
            
    /**
     * Tests the retrieval of mixed logs to write
     * @throws LogQueueException 
     */
    public void testGetMixedLogsToWrite() throws LogQueueException{
        new File(LOG_DIR).mkdir();
        testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries1), "policy1");
        testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries1), "tracking1");
        testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries2), "policy2");
        testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries2), "tracking2");
        testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries3), "tracking3");
        testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries4), "tracking4");
        testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries3), "policy3");
        testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries5), "tracking5");
        
        Map<Long, BaseLogEntry[]> policyLogReceived = logQueueMgrToTest.getLogsToWrite();
        assertEquals("There should be three sets of policy logs to write", 3, policyLogReceived.size());
        PolicyActivityLogEntry[] receivedPolicyLogEntries1 = (PolicyActivityLogEntry[])policyLogReceived.get(new Long(1));
        PolicyActivityLogEntry[] receivedPolicyLogEntries2 = (PolicyActivityLogEntry[])policyLogReceived.get(new Long(3));
        PolicyActivityLogEntry[] receivedPolicyLogEntries3 = (PolicyActivityLogEntry[])policyLogReceived.get(new Long(7));
        for (int i = 0; i < policyLogEntries1.length; i++){
            assertEquals("the policy activity log " + i + " retrieved from the file system is not equal to the original one", 
                         policyLogEntries1[i], 
                         receivedPolicyLogEntries1[i]);
        }
        for (int i = 0; i < policyLogEntries2.length; i++){
            assertEquals("the policy activity log " + i + " retrieved from the file system is not equal to the original one", 
                         policyLogEntries2[i], 
                         receivedPolicyLogEntries2[i]);
        }
        for (int i = 0; i < policyLogEntries3.length; i++){
            assertEquals("the policy activity log " + i + " retrieved from the file system is not equal to the original one", 
                         policyLogEntries3[i], 
                         receivedPolicyLogEntries3[i]);
        }
        
        
        Map<Long, BaseLogEntry[]> trackingLogReceived = logQueueMgrToTest.getLogsToWrite();
        assertEquals("There should be five sets of tracking logs to write", 5, trackingLogReceived.size());
        TrackingLogEntry[] receivedTrackingLogEntries1 = (TrackingLogEntry[])trackingLogReceived.get(new Long(2));
        TrackingLogEntry[] receivedTrackingLogEntries2 = (TrackingLogEntry[])trackingLogReceived.get(new Long(4));
        TrackingLogEntry[] receivedTrackingLogEntries3 = (TrackingLogEntry[])trackingLogReceived.get(new Long(5));
        TrackingLogEntry[] receivedTrackingLogEntries4 = (TrackingLogEntry[])trackingLogReceived.get(new Long(6));
        TrackingLogEntry[] receivedTrackingLogEntries5 = (TrackingLogEntry[])trackingLogReceived.get(new Long(8));
        for (int i = 0; i < trackingLogEntries1.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries1[i], 
                         receivedTrackingLogEntries1[i]);
        }
        for (int i = 0; i < trackingLogEntries2.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries2[i], 
                         receivedTrackingLogEntries2[i]);
        }
        for (int i = 0; i < trackingLogEntries3.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries3[i], 
                         receivedTrackingLogEntries3[i]);
        }
        for (int i = 0; i < trackingLogEntries4.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries4[i], 
                         receivedTrackingLogEntries4[i]);
        }
        for (int i = 0; i < trackingLogEntries5.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries5[i], 
                         receivedTrackingLogEntries5[i]);
        }
    }
    
    /**
     * Tests the retieval of logs to write with log size limit
     * @throws LogQueueException  
     *
     */
    public void testGetLogsToWriteWithLogSizeLimit() throws LogQueueException {
        new File(LOG_DIR).mkdir();
        testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries1), "policy1");
        testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries2), "policy2");
        testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries1), "tracking1");
        testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries2), "tracking2");
        testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries3), "policy3");
        testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries3), "tracking3");
        logQueueMgrToTest.setLogSizeLimit(75000);
        
        Map<Long, BaseLogEntry[]> policyLogReceived = logQueueMgrToTest.getLogsToWrite();
        assertEquals("There should be two sets of policy logs to write", 2, policyLogReceived.size());
        PolicyActivityLogEntry[] receivedPolicyLogEntries1 = (PolicyActivityLogEntry[])policyLogReceived.get(new Long(1));
        PolicyActivityLogEntry[] receivedPolicyLogEntries2 = (PolicyActivityLogEntry[])policyLogReceived.get(new Long(2));
        for (int i = 0; i < policyLogEntries1.length; i++){
            assertEquals("the policy activity log " + i + " retrieved from the file system is not equal to the original one", 
                         policyLogEntries1[i], 
                         receivedPolicyLogEntries1[i]);
        }
        for (int i = 0; i < policyLogEntries2.length; i++){
            assertEquals("the policy activity log " + i + " retrieved from the file system is not equal to the original one", 
                         policyLogEntries2[i], 
                         receivedPolicyLogEntries2[i]);
        }
        
        Map<Long, BaseLogEntry[]> trackingLogReceived = logQueueMgrToTest.getLogsToWrite();
        assertEquals("There should be two sets of tracking logs to write", 2, trackingLogReceived.size());
        TrackingLogEntry[] receivedTrackingLogEntries1 = (TrackingLogEntry[])trackingLogReceived.get(new Long(3));
        TrackingLogEntry[] receivedTrackingLogEntries2 = (TrackingLogEntry[])trackingLogReceived.get(new Long(4));
        for (int i = 0; i < trackingLogEntries1.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries1[i], 
                         receivedTrackingLogEntries1[i]);
        }
        for (int i = 0; i < trackingLogEntries2.length; i++){
            assertEquals("the tracking activity logs retrieved from the file system is not equal to the original one", 
                         trackingLogEntries2[i], 
                         receivedTrackingLogEntries2[i]);
        }
        logQueueMgrToTest.getLogsToWrite();
        logQueueMgrToTest.getLogsToWrite();
    }
        
    /**
     * Tests the retrieval of logs to write when there are no logs to retrieve
     * @throws LogQueueException 
     *
     */
    public void testGetLogsToWriteWithNoLogFilesOnDisk() throws LogQueueException {
        Map<Long, BaseLogEntry[]> logsReceived = logQueueMgrToTest.getLogsToWrite();
        if (logsReceived != null){
            fail("There should not be any more logs to write");
        }
    }
    
    /**
     * Tests the queueing of logs with multiple threads
     * @throws IOException 
     * @throws ClassNotFoundException
     *
     */
    public void testQueueLogsWithMultipleThreads() throws IOException, ClassNotFoundException {
        new File(LOG_DIR).mkdir();
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        File logDir = new File(LOG_DIR);
        File[] logFileList = logDir.listFiles();
        assertEquals("there should be 32 log files written to the file system", 32, logFileList.length);
        for (int i = 0; i < logFileList.length; i++){
            File logFile = logFileList[i];
            String logFileType = logFile.getName().substring(0, 2);
            String logFileNum = logFile.getName().substring(logFile.getName().indexOf("log")+3);
            Long logFileId = new Long(logFileNum);
            String sampleDataName = testLogTracker.get(logFileId);
                
            FileInputStream fileInputStream = new FileInputStream(logFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream logInputStream = new ObjectInputStream(bufferedInputStream);
            BaseLogEntry[] retrievedEntries;
            if (logFileType.equals("tr")){
                retrievedEntries = (TrackingLogEntry[])logInputStream.readObject();
            } else {
                retrievedEntries = (PolicyActivityLogEntry[])logInputStream.readObject();
            }
            BaseLogEntry[] originalEntries = null;
            if (sampleDataName.equals("policy1")){
                originalEntries = policyLogEntries1;
            } else if (sampleDataName.equals("policy2")){
                originalEntries = policyLogEntries2;
            } else if (sampleDataName.equals("policy3")){
                originalEntries = policyLogEntries3;
            } else if (sampleDataName.equals("tracking1")){
                originalEntries = trackingLogEntries1;
            } else if (sampleDataName.equals("tracking2")){
                originalEntries = trackingLogEntries2;
            } else if (sampleDataName.equals("tracking3")){
                originalEntries = trackingLogEntries3;
            } else if (sampleDataName.equals("tracking4")){
                originalEntries = trackingLogEntries4;
            } else if (sampleDataName.equals("tracking5")){
                originalEntries = trackingLogEntries5;
            } 
            
            for (int j = 0; j < originalEntries.length; j++){
                assertEquals("the activity logs retrieved from the file system is not equal to the original one", 
                             originalEntries[j], 
                             retrievedEntries[j]);
            }
            
            fileInputStream.close();
            bufferedInputStream.close();
            logInputStream.close();
        }        
    }
    
    /**
     * Tests the retrieval of logs to write with multiple threads
     * @throws IOException 
     * @throws ClassNotFoundException 
     *
     */
    public void testGetLogsToWriteWithMultipleThreads() throws IOException, ClassNotFoundException{
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new TestGetLogsToWriteThread().start();
        new TestGetLogsToWriteThread().start();
        new TestGetLogsToWriteThread().start();
        new TestGetLogsToWriteThread().start();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        for (int i = 0; i < retrievedLogsTracker.size(); i++){     
            Map<Long, BaseLogEntry[]> logReceived = retrievedLogsTracker.get(i);
            if (logReceived != null){
                for (Map.Entry<Long, BaseLogEntry[]> entry : logReceived.entrySet()) {	
                    Long logFileId = entry.getKey();
                    completedLogsTracker.add(logFileId);
                    BaseLogEntry[] retrievedEntries = entry.getValue();
                    BaseLogEntry[] originalEntries = new BaseLogEntry[0];
                    String sampleDataName = testLogTracker.get(logFileId);
                    if (sampleDataName.equals("policy1")){
                        originalEntries = policyLogEntries1;
                    } else if (sampleDataName.equals("policy2")){
                        originalEntries = policyLogEntries2;
                    } else if (sampleDataName.equals("policy3")){
                        originalEntries = policyLogEntries3;
                    } else if (sampleDataName.equals("tracking1")){
                        originalEntries = trackingLogEntries1;
                    } else if (sampleDataName.equals("tracking2")){
                        originalEntries = trackingLogEntries2;
                    } else if (sampleDataName.equals("tracking3")){
                        originalEntries = trackingLogEntries3;
                    } else if (sampleDataName.equals("tracking4")){
                        originalEntries = trackingLogEntries4;
                    } else if (sampleDataName.equals("tracking5")){
                        originalEntries = trackingLogEntries5;
                    } 

                    for (int j = 0; j < originalEntries.length; j++){
                        assertEquals("the activity logs retrieved from log file " + logFileId + " on the file system is not equal to the original one at log " + j, 
                                originalEntries[j], 
                                retrievedEntries[j]);
                    }       
                }
            }
        } 
    }
    
    /**
     * Tests the dequeueing of logs with multiple threads
     * @throws LogQueueException 
     *
     */
    public void testDequeueLogsWithMultipleThreads() throws LogQueueException {
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        new TestQueueLogThread().start();
        logQueueMgrToTest.setLogSizeLimit(75000);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new TestGetLogsToWriteThread().start();
        new TestGetLogsToWriteThread().start();
        new TestGetLogsToWriteThread().start();
        new TestGetLogsToWriteThread().start();
        completedLogsTracker.add(new Long(1));
        completedLogsTracker.add(new Long(2));
        completedLogsTracker.add(new Long(3));
        completedLogsTracker.add(new Long(4));
        completedLogsTracker.add(new Long(5));
        completedLogsTracker.add(new Long(6));
        completedLogsTracker.add(new Long(7));
        completedLogsTracker.add(new Long(8));
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new TestDequeueLogsThread().start();
        new TestDequeueLogsThread().start();
        new TestDequeueLogsThread().start();
        new TestDequeueLogsThread().start();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        File logDir = new File(LOG_DIR);
        File[] logFileList = logDir.listFiles();
        assertEquals("there should be 24 log files written to the file system", 24, logFileList.length);
        for (int i = 0; i < logFileList.length; i++){
            File logFile = logFileList[i];
            String logFileNum = logFile.getName().substring(logFile.getName().indexOf("log")+3);
            Long logFileId = new Long(logFileNum);
            if (!testLogTracker.containsKey(logFileId)){
                fail("The log file " + logFileId + " should have been dequeued");
            } 
        }
    }
    
    /**
     * Tests multi-threaded queue, dequeue, and retrieving the logs
     * @throws ClassNotFoundException  
     * @throws IOException 
     *
     */
    public void testMultiThreadedLogQueueActions() throws IOException, ClassNotFoundException {
        new TestQueueLogThread().start();
        new TestDequeueLogsThread().start();
        new TestGetLogsToWriteThread().start();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // do some checking
        File logDir1 = new File(LOG_DIR);
        File[] logFileList1 = logDir1.listFiles();
        assertEquals("there should be 8 log files written to the file system", 8, logFileList1.length);
        for (int i = 0; i < logFileList1.length; i++){
            File logFile = logFileList1[i];
            String logFileType = logFile.getName().substring(0, 2);
            String logFileNum = logFile.getName().substring(logFile.getName().indexOf("log")+3);
            Long logFileId = new Long(logFileNum);
            if (!testLogTracker.containsKey(logFileId)){
                fail("The log file " + logFileId + " should have been dequeued");
            } 
            String sampleDataName = testLogTracker.get(logFileId);
            
            FileInputStream fileInputStream = new FileInputStream(logFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream logInputStream = new ObjectInputStream(bufferedInputStream);
            BaseLogEntry[] retrievedEntries;
            if (logFileType.equals("tr")){
                retrievedEntries = (TrackingLogEntry[])logInputStream.readObject();
            } else {
                retrievedEntries = (PolicyActivityLogEntry[])logInputStream.readObject();
            }
            BaseLogEntry[] originalEntries = null;
            if (sampleDataName.equals("policy1")){
                originalEntries = policyLogEntries1;
            } else if (sampleDataName.equals("policy2")){
                originalEntries = policyLogEntries2;
            } else if (sampleDataName.equals("policy3")){
                originalEntries = policyLogEntries3;
            } else if (sampleDataName.equals("tracking1")){
                originalEntries = trackingLogEntries1;
            } else if (sampleDataName.equals("tracking2")){
                originalEntries = trackingLogEntries2;
            } else if (sampleDataName.equals("tracking3")){
                originalEntries = trackingLogEntries3;
            } else if (sampleDataName.equals("tracking4")){
                originalEntries = trackingLogEntries4;
            } else if (sampleDataName.equals("tracking5")){
                originalEntries = trackingLogEntries5;
            } 
            
            for (int j = 0; j < originalEntries.length; j++){
                assertEquals("the activity logs retrieved from the file system is not equal to the original one", 
                             originalEntries[j], 
                             retrievedEntries[j]);
            }
            
            fileInputStream.close();
            bufferedInputStream.close();
            logInputStream.close();
        }
        for (int i = 0; i < retrievedLogsTracker.size(); i++){     
            Map<Long, BaseLogEntry[]> logReceived = retrievedLogsTracker.get(i);
            if (logReceived != null){
            	for (Map.Entry<Long, BaseLogEntry[]> entry : logReceived.entrySet()) {
                    Long logFileId = entry.getKey();
                    BaseLogEntry[] retrievedEntries = entry.getValue();
                    BaseLogEntry[] originalEntries = new BaseLogEntry[0];
                    String sampleDataName = testLogTracker.get(logFileId);
                    if (sampleDataName != null){
                        completedLogsTracker.add(logFileId);
                        if (sampleDataName.equals("policy1")){
                            originalEntries = policyLogEntries1;
                        } else if (sampleDataName.equals("policy2")){
                            originalEntries = policyLogEntries2;
                        } else if (sampleDataName.equals("policy3")){
                            originalEntries = policyLogEntries3;
                        } else if (sampleDataName.equals("tracking1")){
                            originalEntries = trackingLogEntries1;
                        } else if (sampleDataName.equals("tracking2")){
                            originalEntries = trackingLogEntries2;
                        } else if (sampleDataName.equals("tracking3")){
                            originalEntries = trackingLogEntries3;
                        } else if (sampleDataName.equals("tracking4")){
                            originalEntries = trackingLogEntries4;
                        } else if (sampleDataName.equals("tracking5")){
                            originalEntries = trackingLogEntries5;
                        } 

                        for (int j = 0; j < originalEntries.length; j++){
                            assertEquals("the activity logs retrieved from log file " + logFileId + " on the file system is not equal to the original one at log " + j, 
                                    originalEntries[j], 
                                    retrievedEntries[j]);
                        }       
                    }
                }
            }
        } 
        
        
        new TestQueueLogThread().start();
        new TestDequeueLogsThread().start();
        new TestGetLogsToWriteThread().start();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // do some checking
        File logDir2 = new File(LOG_DIR);
        File[] logFileList2 = logDir2.listFiles();
        assertEquals("there should be 16 log files written to the file system", 16, logFileList2.length);
        for (int i = 0; i < logFileList2.length; i++){
            File logFile = logFileList2[i];
            String logFileType = logFile.getName().substring(0, 2);
            String logFileNum = logFile.getName().substring(logFile.getName().indexOf("log")+3);
            Long logFileId = new Long(logFileNum);
            if (!testLogTracker.containsKey(logFileId)){
                fail("The log file " + logFileId + " should have been dequeued");
            } 
            String sampleDataName = testLogTracker.get(logFileId);
            
            FileInputStream fileInputStream = new FileInputStream(logFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream logInputStream = new ObjectInputStream(bufferedInputStream);
            BaseLogEntry[] retrievedEntries;
            if (logFileType.equals("tr")){
                retrievedEntries = (TrackingLogEntry[])logInputStream.readObject();
            } else {
                retrievedEntries = (PolicyActivityLogEntry[])logInputStream.readObject();
            }
            BaseLogEntry[] originalEntries = null;
            if (sampleDataName.equals("policy1")){
                originalEntries = policyLogEntries1;
            } else if (sampleDataName.equals("policy2")){
                originalEntries = policyLogEntries2;
            } else if (sampleDataName.equals("policy3")){
                originalEntries = policyLogEntries3;
            } else if (sampleDataName.equals("tracking1")){
                originalEntries = trackingLogEntries1;
            } else if (sampleDataName.equals("tracking2")){
                originalEntries = trackingLogEntries2;
            } else if (sampleDataName.equals("tracking3")){
                originalEntries = trackingLogEntries3;
            } else if (sampleDataName.equals("tracking4")){
                originalEntries = trackingLogEntries4;
            } else if (sampleDataName.equals("tracking5")){
                originalEntries = trackingLogEntries5;
            } 
            
            for (int j = 0; j < originalEntries.length; j++){
                assertEquals("the activity logs retrieved from the file system is not equal to the original one", 
                             originalEntries[j], 
                             retrievedEntries[j]);
            }
            
            fileInputStream.close();
            bufferedInputStream.close();
            logInputStream.close();
        }
        for (int i = 0; i < retrievedLogsTracker.size(); i++){     
            Map<Long, BaseLogEntry[]> logReceived = retrievedLogsTracker.get(i);
            if (logReceived != null){
            	for (Map.Entry<Long, BaseLogEntry[]> entry : logReceived.entrySet()) {
                    Long logFileId = entry.getKey();
                    BaseLogEntry[] retrievedEntries = entry.getValue();
                    BaseLogEntry[] originalEntries = new BaseLogEntry[0];
                    String sampleDataName = testLogTracker.get(logFileId);
                    if (sampleDataName != null){
                        completedLogsTracker.add(logFileId);
                        if (sampleDataName.equals("policy1")){
                            originalEntries = policyLogEntries1;
                        } else if (sampleDataName.equals("policy2")){
                            originalEntries = policyLogEntries2;
                        } else if (sampleDataName.equals("policy3")){
                            originalEntries = policyLogEntries3;
                        } else if (sampleDataName.equals("tracking1")){
                            originalEntries = trackingLogEntries1;
                        } else if (sampleDataName.equals("tracking2")){
                            originalEntries = trackingLogEntries2;
                        } else if (sampleDataName.equals("tracking3")){
                            originalEntries = trackingLogEntries3;
                        } else if (sampleDataName.equals("tracking4")){
                            originalEntries = trackingLogEntries4;
                        } else if (sampleDataName.equals("tracking5")){
                            originalEntries = trackingLogEntries5;
                        } 

                        for (int j = 0; j < originalEntries.length; j++){
                            assertEquals("the activity logs retrieved from log file " + logFileId + " on the file system is not equal to the original one at log " + j, 
                                    originalEntries[j], 
                                    retrievedEntries[j]);
                        }       
                    }
                }
            }
        } 
    }    
    
    /**
     * Tests the timeout of the logs.  The FileSystemLogQueueMgr should unlock the logs if 
     * the logs have not been requested to be dequeued after the timeout, which means the 
     * logs will be avialable on the next call to getLogsToWrite() 
     * 
     * @throws InterruptedException   
     * @throws LogQueueException 
     *
     */
    public void testTimeOut() throws InterruptedException, LogQueueException {
        new TestQueueLogThread().start();
        logQueueMgrToTest.setLogSizeLimit(15000);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new TestGetLogsToWriteThread().start();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(70000);
        Map<Long, BaseLogEntry[]> logsReceived = logQueueMgrToTest.getLogsToWrite();
        for (Long logFileId : logsReceived.keySet()) {
            assertTrue("The given log file should have been processed before: " + logFileId, logFileId <= 1);
        }
    }
    
    private class TestQueueLogThread extends Thread {          
        public void run(){
            try {
                testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries1), "tracking1");
                testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries1), "policy1");
                testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries2), "tracking2");
                testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries2), "policy2");
                testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries3), "tracking3");
                testLogTracker.put(logQueueMgrToTest.queueLogs(policyLogEntries3), "policy3");
                testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries4), "tracking4");
                testLogTracker.put(logQueueMgrToTest.queueLogs(trackingLogEntries5), "tracking5");
            } catch (LogQueueException e){
                // do nothing
            }
        }
    }
    
    private class TestGetLogsToWriteThread extends Thread {    
        public void run(){
            try {
            	Map<Long, BaseLogEntry[]> entries1 = logQueueMgrToTest.getLogsToWrite();
            	Map<Long, BaseLogEntry[]> entries2 = logQueueMgrToTest.getLogsToWrite();
                Map<Long, BaseLogEntry[]> entries3 = logQueueMgrToTest.getLogsToWrite();
                retrievedLogsTracker.add(entries1);
                retrievedLogsTracker.add(entries2);
                retrievedLogsTracker.add(entries3);
            } catch (LogQueueException e){
                // do nothing
            } 
        }
    }
    
    private class TestDequeueLogsThread extends Thread {    
        public void run(){
            try {
                while (completedLogsTracker.size() > 0){
                    Long logFileId = completedLogsTracker.remove(0);
                    logQueueMgrToTest.dequeueLogs(logFileId);
                    testLogTracker.remove(logFileId);
                    for (int i = 0; i < retrievedLogsTracker.size(); i++){
                        retrievedLogsTracker.get(i);
                    }
                }
            } catch (LogQueueException e){
                // do nothing
            } 
        }
    }
}
