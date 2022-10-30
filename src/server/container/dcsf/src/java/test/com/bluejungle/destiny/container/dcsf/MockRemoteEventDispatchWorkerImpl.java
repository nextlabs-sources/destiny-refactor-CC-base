/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.container.dcsf.RemoteEventDispatchWorkerImpl;
import com.bluejungle.destiny.services.dcsf.DCSFServiceLocator;

/**
 * This dummy class uses a different web service locator to spy if the web
 * service is properly invoked.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/MockRemoteEventDispatchWorkerImpl.java#1 $:
 */

public class MockRemoteEventDispatchWorkerImpl extends RemoteEventDispatchWorkerImpl {

    private static Integer initCount = new Integer(0);

    /**
     * Returns a dummy DCSF Service Locator
     * 
     * @return a dummy DCSF Service Locator
     */
    protected DCSFServiceLocator getDCSFServiceLocator() {
        return new MockDCSFServiceLocator();
    }

    /**
     * This init function counts how many inits occured
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        synchronized (initCount) {
            int count = initCount.intValue();
            count++;
            initCount = new Integer(count);
        }
    }

    /**
     * Returns the init Count.
     * 
     * @return the init Count.
     */
    public static Integer getInitCount() {
        return initCount;
    }

    /**
     * Sets the initCount
     * 
     * @param initCount
     *            The initCount to set.
     */
    public static void setInitCount(Integer initCount) {
        MockRemoteEventDispatchWorkerImpl.initCount = initCount;
    }
}