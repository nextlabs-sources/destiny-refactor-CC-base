/*
 * Created on Mar 29, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004-2007 by NextLabs, Inc., San Mateo CA, Ownership remains with NextLabs,
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.activityjournal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.bluejungle.destiny.agent.commandengine.CommandExecutor;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.communication.tests.MockCommunicationManager;
import com.bluejungle.destiny.agent.communication.tests.MockLogServiceImpl;
import com.bluejungle.destiny.agent.communication.tests.TestControlManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.Pair;
import com.nextlabs.domain.log.PolicyActivityInfoV5;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/src/java/test/com/bluejungle/destiny/agent/activityjournal/ActivityJournalTest.java#1 $:
 */

public class ActivityJournalTest extends BaseDestinyTestCase {

    private static final long INITIAL_LOG_SIZE = 0;
    private static final long FIRST_FILLER_SIZE = 2 * 1024 * 1024;
    private static final long SECOND_FILLER_SIZE = 38;
    private static final int NUM_LOGS = ActivityJournal.SINGLE_FILE_LIMIT / ActivityJournal.SINGLE_LOG_SIZE + 2;
    // number determined experimentally
    private static final int ACTUAL_LOG_SIZE = 250;
    private static final int ACCURACY_THRESHOLD = (int) (ActivityJournal.SINGLE_FILE_LIMIT * .1);

    IComponentManager compMgr;
    ActivityJournal activityJournal;

    /**
     * Constructor for ActivityJournalTest.
     * 
     * @param name
     *            name of the test to run
     */
    public ActivityJournalTest(String name) {
        super(name);
    }

    /**
     * Creates a file filled up with dummy characters. If the directory where
     * the file should be created does not exist, it is created at the same
     * time.
     * 
     * @param string
     *            file name.
     * @param size
     *            size of the file to create
     */
    private void createFiller(String string, long size) {
        try {
            OutputStream os = null;
            try {
                File f = new File(string);
                final File dir = f.getParentFile();
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        fail("Unable to create directories " + f.getAbsolutePath());
                    }
                }
                if (f.exists()) {
                    fail(f + " exists");
                }
                os = new BufferedOutputStream(new FileOutputStream(f));
                for (long i = size; i > 0; i--) {
                    os.write(37);
                }
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        } catch (IOException e) {
            fail("IOException thrown when creating filled file. " + e.getLocalizedMessage());
        }
    }

    /**
     * This function initializes the Activity journal component
     */
    private void initAJ() {
        compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo<TestControlManager> info = new ComponentInfo<TestControlManager>(
        		IControlManager.NAME, 
        		TestControlManager.class, 
        		IControlManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        IControlManager controlManager = compMgr.getComponent(info);

        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IControlManager.NAME, controlManager);
        compMgr.getComponent(MockCommunicationManager.class, config);

        compMgr.registerComponent(new ComponentInfo<ProfileManager>(
        		IProfileManager.class.getName(), 
        		ProfileManager.class, 
        		IProfileManager.class, 
        		LifestyleType.SINGLETON_TYPE), true);

        compMgr.registerComponent(new ComponentInfo<CommandExecutor>(
        		ICommandExecutor.NAME, 
        		CommandExecutor.class, 
        		ICommandExecutor.class, 
        		LifestyleType.SINGLETON_TYPE), true);

        this.activityJournal = ComponentManagerFactory.getComponentManager().getComponent(
        		new ComponentInfo<ActivityJournal>(
        				IActivityJournal.NAME, 
        				ActivityJournal.class, 
        				IActivityJournal.class, 
        				LifestyleType.SINGLETON_TYPE, 
        				config)
        );
        this.activityJournal.setEnabled();
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        //Wipes out all logs in the logs directory
        File logDir = new File(ActivityJournal.LOG_SUB_DIR);
        logDir.listFiles(new FileFilter() {

            public boolean accept(File fileName) {
                if (fileName.exists() && !fileName.delete()) {
                    fail("Could not delete " + fileName);
                }
                return false;
            }

        });
        if (logDir.exists() && !logDir.delete()) {
            fail("Could not delete " + logDir);
        }
    }

    /**
     * This test verifies that at startup, the initial log file size is correct.
     */
    public void testInitialActivityJournalLogSize() throws Exception {
        initAJ();
        assertEquals("initial log size should be " + INITIAL_LOG_SIZE, INITIAL_LOG_SIZE, activityJournal.getTotalLogSize());
    }

    /**
     * This test verifies that the policy activity journal log file is
     * calculated properly.
     */
    public void testPolicyActivityJournalLogSize() {
        createFiller(ActivityJournal.LOG_SUB_DIR + File.separatorChar + ActivityJournal.POLICY_ACTIVITY_FILENAME_PREFIX + ActivityJournal.AGENT_LOG_FILE + 1, FIRST_FILLER_SIZE);
        initAJ();
        assertEquals(FIRST_FILLER_SIZE, activityJournal.getTotalLogSize());
    }

    /**
     * This test verifies that the tracking activity journal log file is
     * calculated properly.
     */
    public void testTrackingActivityJournalLogSize() {
        createFiller(ActivityJournal.LOG_SUB_DIR + File.separatorChar + ActivityJournal.TRACKING_ACTIVITY_FILENAME_PREFIX + ActivityJournal.AGENT_LOG_FILE + 1, FIRST_FILLER_SIZE);
        initAJ();
        assertEquals(FIRST_FILLER_SIZE, activityJournal.getTotalLogSize());
    }

    /**
     * This test verifies that the policy assistant journal log file is
     * calculated properly.
     */
    public void testPolicyAssistantJournalLogSize() {
        createFiller(ActivityJournal.LOG_SUB_DIR + File.separatorChar + ActivityJournal.POLICY_ASSISTANT_FILENAME_PREFIX + ActivityJournal.AGENT_LOG_FILE + 1, FIRST_FILLER_SIZE);
        initAJ();
        assertEquals(FIRST_FILLER_SIZE, activityJournal.getTotalLogSize());
    }

    /**
     * This test verifies that if multiple activity journals of all kinds are in
     * the log directory, the activity journal component can calculate their
     * total size properly.
     */
    public void testMultipleAndMixedActivityJournalLogSize() {
        final int fileSize1 = 200;
        final int fileSize2 = 125;
        final int fileSize3 = 77;
        final int fileSize4 = 41;
        final int expectedTotalSize = fileSize1 + fileSize2 + fileSize3 + fileSize4;
        createFiller(ActivityJournal.LOG_SUB_DIR + File.separatorChar + ActivityJournal.TRACKING_ACTIVITY_FILENAME_PREFIX + ActivityJournal.AGENT_LOG_FILE + 1, fileSize1);
        createFiller(ActivityJournal.LOG_SUB_DIR + File.separatorChar + ActivityJournal.TRACKING_ACTIVITY_FILENAME_PREFIX + ActivityJournal.AGENT_LOG_FILE + 2, fileSize2);
        createFiller(ActivityJournal.LOG_SUB_DIR + File.separatorChar + ActivityJournal.POLICY_ACTIVITY_FILENAME_PREFIX + ActivityJournal.AGENT_LOG_FILE + 1, fileSize3);
        createFiller(ActivityJournal.LOG_SUB_DIR + File.separatorChar + ActivityJournal.POLICY_ACTIVITY_FILENAME_PREFIX + ActivityJournal.AGENT_LOG_FILE + 2, fileSize4);
        initAJ();
        assertEquals(expectedTotalSize, activityJournal.getTotalLogSize());
    }

    /**
     * This test verifies the file size accuracy
     * 
     * @throws InterruptedException
     *             if threads fail
     */
//    public void testFileSizeAccuracy() throws InterruptedException {
 //       initAJ();
//        PolicyLoggerThread loggerThread = new PolicyLoggerThread();
//        loggerThread.start();
 //       loggerThread.join();

 //       final long expectedSize = ACTUAL_LOG_SIZE * (NUM_LOGS - 2);
//        assertTrue(Math.abs(expectedSize - activityJournal.getTotalLogSize()) < ACCURACY_THRESHOLD);
 //   }

    /**
     * This test verifies that if only policy activity data is currently stored
     * on disk (and no tracking so far), it can be uploaded properly.
     */
    public void testPolicyActivityOnlyUpload() throws InterruptedException {
        initAJ();
        PolicyLoggerThread loggerThread = new PolicyLoggerThread();
        UploaderThread uploaderThread = new UploaderThread();
        //Start logging and uploading
        loggerThread.start();
        uploaderThread.start();
        loggerThread.join();
        uploaderThread.join();
    }

    /**
     * This test verifies that if only tracking activity data is currently
     * stored on disk (and no policy activity so far), it can be uploaded
     * properly.
     */
    public void testTrackingActivityOnlyUpload() throws InterruptedException {
        initAJ();
        TrackingLoggerThread loggerThread = new TrackingLoggerThread();
        UploaderThread uploaderThread = new UploaderThread();
        //Start logging and uploading
        loggerThread.start();
        uploaderThread.start();
        loggerThread.join();
        uploaderThread.join();
    }

    /**
     * This test verifies that if only policy assistant data is currently
     * stored on disk, it can be uploaded properly.
     */
    public void testPolicyAssistantOnlyUpload() throws InterruptedException {
        initAJ();
        PolicyAssistantLoggerThread loggerThread = new PolicyAssistantLoggerThread();
        UploaderThread uploaderThread = new UploaderThread();
        //Start logging and uploading
        loggerThread.start();
        uploaderThread.start();
        loggerThread.join();
        uploaderThread.join();
    }


    /**
     * This test verifies that policy, policy assistant, and tracking activity can be
     * uploaded at the same time to the server.
     */
    public void testAllActivityUpload() throws InterruptedException {
        initAJ();
        TrackingLoggerThread trackingLogger = new TrackingLoggerThread();
        PolicyLoggerThread policyLogger = new PolicyLoggerThread();
        PolicyAssistantLoggerThread policyAssistantLogger = new PolicyAssistantLoggerThread();
        UploaderThread uploaderThread = new UploaderThread();
        policyLogger.start();
        policyAssistantLogger.start();
        trackingLogger.start();
        uploaderThread.start();
        policyLogger.join();
        policyAssistantLogger.join();
        trackingLogger.join();
        uploaderThread.join();
    }

    /**
     * This threads logs activity related data
     * 
     * @author ihanen
     */
    class TrackingLoggerThread extends Thread {

        IActivityJournal activityJournal = (IActivityJournal) ComponentManagerFactory.getComponentManager().getComponent(IActivityJournal.NAME);

        public void run() {
            for (int i = 0; i < NUM_LOGS; i++) {
                FromResourceInformation ri = new FromResourceInformation(MockLogServiceImpl.FROM_FILE_NAME, 234, 78624243, 212131, "SOME-SID-LIKE-THING");
                String key = "key1";
                String value = "value1";
                DynamicAttributes fromResAttrs = new DynamicAttributes();
                fromResAttrs.put(key, value);
                TrackingLogEntryV3 logEntry = new TrackingLogEntryV3();
                logEntry.setAction(ActionEnumType.ACTION_MOVE.getName());
                logEntry.setApplicationId(89);
                logEntry.setApplicationName("notepad.exe");
                logEntry.setUserId(1);
                logEntry.setUserName("bob");
                logEntry.setHostName("host.domain.com");
                logEntry.setHostIP("1.2.3.4");
                logEntry.setFromResourceInfo(ri);
                logEntry.setCustomAttr(fromResAttrs);
                activityJournal.logTrackingActivity(logEntry);
            }
            this.activityJournal.setEnabled();
        }
    }

    /**
     * This threads logs activity related data
     * 
     * @author ihanen
     */
    class PolicyLoggerThread extends Thread {

        IActivityJournal activityJournal = (IActivityJournal) ComponentManagerFactory.getComponentManager().getComponent(IActivityJournal.NAME);

        public void run() {
            for (int i = 0; i < NUM_LOGS; i++) {
                FromResourceInformation ri = new FromResourceInformation(MockLogServiceImpl.FROM_FILE_NAME, 234, 78624243, 212131, "SOME-SID-LIKE-THING");
                String key = "key1";
                String value = "value1";
                Map<String, DynamicAttributes> attributesMap = new HashMap<String, DynamicAttributes>();
                DynamicAttributes fromResAttrs = new DynamicAttributes();
                fromResAttrs.put(key, value);
                attributesMap.put("FromAttributes", fromResAttrs);
                List<IPair<String, String>> annotations = new ArrayList<IPair<String, String>>();
                annotations.add(new Pair<String, String>("key", "value"));
                PolicyActivityInfoV5 req = new PolicyActivityInfoV5(ri, null, "FargeyMcGunkle@bluejungle.com", 1, "destiny.bluejungle.com", "1.2.3.4", 54, "notepad.exe", 89, ActionEnumType.ACTION_MOVE.getName(), PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 456, 24423, 0, attributesMap, annotations);
                activityJournal.logPolicyActivity(new PolicyActivityLogEntryV5(req, 345));
            }
        }
    }

    /**
     * This threads logs assistant related data
     * 
     * @author amorgan
     */
    class PolicyAssistantLoggerThread extends Thread {
        IActivityJournal activityJournal = (IActivityJournal) ComponentManagerFactory.getComponentManager().getComponent(IActivityJournal.NAME);

        public void run() {
            for (int i = 0; i < NUM_LOGS; i++) {
                activityJournal.logPolicyAssistantActivity(new PolicyAssistantLogEntry("8675309",
                                                                                       "Defooblifying Assistant",
                                                                                       "-defoob -extra-foob",
                                                                                       "Performs Defooblification On Fooblified Thingys",
                                                                                       "User defoobled correctly",
                                                                                       24423,
                                                                                       345));
            }
        }
    }
}
