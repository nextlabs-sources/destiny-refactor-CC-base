/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs;

import junit.framework.TestCase;

import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationRelay;
import com.bluejungle.destiny.container.shared.sharedfolder.MockSharedFolderInformationRelay;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatInfoImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * This class tests the DABS heartbeat mgr
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/TestDABSHeartbeatMgrImpl.java#2 $
 */

public class TestDABSHeartbeatMgrImpl extends TestCase {

    /*
     * Private variables:
     */
    private MockSharedFolderInformationRelay mockSharedFolderInformationRelayForTest;

    /**
     * Constructor for TestDABSHeartbeatMgrImpl.
     * 
     * @param arg0
     */
    public TestDABSHeartbeatMgrImpl(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        // Setup the Shared Folder Information Relay:
        // Initialize the SharedFolderInformation relay for DABS:
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo sharedFolderInfoRelayCompInfo = new ComponentInfo(ISharedFolderInformationRelay.COMP_NAME, MockSharedFolderInformationRelay.class.getName(), ISharedFolderInformationRelay.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.mockSharedFolderInformationRelayForTest = (MockSharedFolderInformationRelay) compMgr.getComponent(sharedFolderInfoRelayCompInfo);
    }

    /**
     * Tests the preparation of the next heartbeat
     *  
     */
    public void testPrepareNextHeartbeat() {
        DABSHeartBeatMgrImpl heartbeatMgrToTest = new DABSHeartBeatMgrImpl();

        // Prepare a heartbeat object:
        IComponentHeartbeatInfo heartbeatInfoForTest = new ComponentHeartbeatInfoImpl();
        heartbeatInfoForTest.setComponentName("TestCompName");
        heartbeatInfoForTest.setComponentType(ServerComponentType.DABS);
        heartbeatInfoForTest.setHeartbeatCookie(null);
        heartbeatInfoForTest.setSharedFolderCookie(null);

        heartbeatMgrToTest.prepareNextHeartbeat(heartbeatInfoForTest);

        // Check that the stuff that was set on the heartbeat was the right
        // stuff:
        ISharedFolderCookie correctCookie = this.mockSharedFolderInformationRelayForTest.getLastUpdateCookie();
        assertEquals("The heartbeat should be set with the right cookie", correctCookie, heartbeatInfoForTest.getSharedFolderCookie());
    }
}