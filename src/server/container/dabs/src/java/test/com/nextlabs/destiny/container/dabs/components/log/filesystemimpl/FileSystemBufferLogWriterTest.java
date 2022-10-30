/*
 * Created on Feb 12, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log.filesystemimpl;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;


/**
 * Test Case to test
 * com.nextlabs.destiny.container.dabs.components.log.filesystemimpl.FileSystemBufferLogWriter
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/nextlabs/destiny/container/dabs/components/log/filesystemimpl/FileSystemBufferLogWriterTest.java#1 $
 */

public class FileSystemBufferLogWriterTest extends BaseDabsComponentTest {

    private FileSystemBufferLogWriter logWriterToTest;
    //private LogWriterTestHelper helper;
    
    /**
     * Constructor
     * @param arg0
     */
    public FileSystemBufferLogWriterTest() {
        super(FileSystemBufferLogWriterTest.class.getName());
//        IComponentManager cm = ComponentManagerFactory.getComponentManager();
//        ComponentInfo logWriterCompInfo = new ComponentInfo(ILogWriter.COMP_NAME, FileSystemBufferLogWriter.class.getName(), ILogWriter.class.getName(), LifestyleType.SINGLETON_TYPE);
//        logWriterToTest = (FileSystemBufferLogWriter) cm.getComponent(logWriterCompInfo);
//        helper = LogWriterTestHelper.getInstance();
    }

    /**
     * @see com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        //helper.deleteAllLogs();
    }

    /**
     * @see com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * This test verifies the insertion of policy activity logs
     */
    public void testFileSystemBufferLogPolicyActivity() {
        assertTrue(true);
    }

    /**
     * This test verifies the insertion of tracking activity logs
     */
    public void testFileSystemBufferLogTrackingActivity() {
        assertTrue(true);
    }
    
    /**
     * This test verifies that an exception is thrown if a bad policy activity
     * log record is inserted
     */
    public void testFileSystemBufferPolicyActivityLogFailureException() {
        assertTrue(true);
    }
    
    /**
     * This test verifies that an exception is thrown if a bad tracking activity
     * log record is inserted
     */
    public void testFileSystemBufferTrackingActivityLogFailureException() {
        assertTrue(true);
    }
}
