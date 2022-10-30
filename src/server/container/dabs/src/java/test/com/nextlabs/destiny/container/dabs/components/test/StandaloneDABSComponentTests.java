/*
 * Created on Feb 12, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.nextlabs.destiny.container.dabs.components.log.filesystemimpl.FileSystemBufferLogWriterTest;
import com.nextlabs.destiny.container.dabs.components.log.filesystemimpl.FileSystemLogQueueMgrTest;

/**
 * DABS component test which run outside of the servlet container
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/nextlabs/destiny/container/dabs/components/test/StandaloneDABSComponentTests.java#1 $
 */

public class StandaloneDABSComponentTests {
    
    /**
     * Returns the test suite
     * 
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("NextLabs DABS internal Components");
        suite.addTest(new TestSuite(FileSystemLogQueueMgrTest.class, "File System Buffer Log Queue Manager"));
        suite.addTest(new TestSuite(FileSystemBufferLogWriterTest.class, "File System Buffer Log Writer"));
        return suite;
    }
}
