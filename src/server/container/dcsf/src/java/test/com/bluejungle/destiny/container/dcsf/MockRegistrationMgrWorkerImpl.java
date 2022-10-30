/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.container.dcsf.DMSRegistrationWorkerImpl;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;

/**
 * This test class extends the real clas for the registration manager worker. It
 * overwrites the component locator so that the code can see whether the base
 * class behaves properly.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/MockRegistrationMgrWorkerImpl.java#1 $:
 */

public class MockRegistrationMgrWorkerImpl extends DMSRegistrationWorkerImpl {

    MockComponentServiceLocator mockLocator = new MockComponentServiceLocator();

    /**
     * Returns the component Service locator
     * 
     * @return the component service locator
     */
    protected ComponentServiceLocator getComponentServiceLocator() {
        return this.mockLocator;
    }
}