/*
 * Created on Dec 10, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.container.dcsf.DCSFServiceImpl;

/**
 * This is a mock DCSF Service implementation extending the real DCSF service.
 * It is used mostly to access member variables within the real DCSF service
 * implementation.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockDCSFServiceImpl.java#1 $:
 */

final class MockDCSFServiceImpl extends DCSFServiceImpl {

    /**
     * Constructor. It replaces the init of the real class
     */
    public MockDCSFServiceImpl() {
        super();
        this.dcsfContainerName = "JUNIT";
        this.eventManager = new MockSharedEventManager();
    }

    /**
     * Returns the dummy event manager
     * 
     * @return the dummy event manager
     */
    public MockSharedEventManager getMockEventManager() {
        return (MockSharedEventManager) this.eventManager;
    }
}