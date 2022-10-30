/*
 * Created on Jun 17, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import com.nextlabs.destiny.container.dac.datasync.sync.ObligationLogSyncTaskTest;
import com.nextlabs.destiny.container.dac.datasync.sync.PolicyActivityLogSyncTaskTest;
//import com.nextlabs.destiny.container.dac.datasync.sync.TrackingActivityLogSyncTaskTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/nextlabs/destiny/container/dac/datasync/DatasyncTestSuite.java#1 $
 */

public class DatasyncTestSuite extends TestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(ArchiverTest.class));
        
        suite.addTest(new TestSuite(PolicyActivityLogSyncTaskTest.class));

        suite.addTest(new TestSuite(ObligationLogSyncTaskTest.class));
     
        suite.addTest(new TestSuite(IndexesRebuildTest.class));
        
        return suite;
    }
    
}
