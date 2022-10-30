/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.util.Set;

import com.bluejungle.destiny.container.dcsf.RemoteEventRegistrationWorkerImpl;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;

/**
 * This is a dummy class used to spy the real behavior of its super class This
 * class spies the behavir of the remote event registration manager worker
 * thread
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/MockRemoteEventRegistrationWorkerImpl.java#1 $:
 */

public class MockRemoteEventRegistrationWorkerImpl extends RemoteEventRegistrationWorkerImpl {

    private static Integer workerCount = new Integer(0);
    private static Set registerList;
    private static Set unregisterList;

    /**
     * Initialize the thread and count how many have been initialized
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        synchronized (workerCount) {
            int count = workerCount.intValue();
            count++;
            workerCount = new Integer(count);
        }
    }

    /**
     * Returns the worker Count.
     * 
     * @return the worker Count.
     */
    public static Integer getWorkerCount() {
        return workerCount;
    }

    /**
     * Reset the dummy class statistics
     */
    public static void reset() {
        synchronized (workerCount) {
            workerCount = new Integer(0);
        }

        if (unregisterList != null) {
            unregisterList.clear();
        }

        if (registerList != null) {
            registerList.clear();
        }
    }

    /**
     * @param registrationList
     * @param unRegistrationList
     * @see com.bluejungle.destiny.container.dcsf.IRemoteEventRegistrationWorker#processRequests(java.util.Set,
     *      java.util.Set)
     */
    public void processRequests(Set registrationList, Set unRegistrationList) {
        super.processRequests(registrationList, unRegistrationList);
        registerList = registrationList;
        unregisterList = unRegistrationList;

    }

    /**
     * Returns a mock service locator that can work without a real web service
     * 
     * @return a mock service locator
     */
    protected ComponentServiceLocator getComponentServiceLocator() {
        return new MockComponentServiceLocator();
    }

    /**
     * Returns the registerList.
     * 
     * @return the registerList.
     */
    public static Set getRegisterList() {
        return registerList;
    }

    /**
     * Returns the unregisterList.
     * 
     * @return the unregisterList.
     */
    public static Set getUnregisterList() {
        return unregisterList;
    }
}