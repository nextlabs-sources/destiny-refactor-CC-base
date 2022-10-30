// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc.tests;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 *  
 */
public class TestHelperThread implements Runnable {

    IPCOSWrapperTest osWrapperTest = null;

    private static IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    /**
     * Opens and reads from shared memory and sets an event to signal the other
     * thread to start. The behavior of the other thread is described in
     * IPCOSWrapperTest
     * 
     * @see java.lang.Runnable#run()
     * @see com.bluejungle.destiny.agent.ipc.tests.IPCOSWrapperTest#testOSWrapper()
     */
    public void run() {
        long mutex = osWrapper.openMutex(IPCOSWrapperTest.MUTEXNAME);
        long fileMapping = osWrapper.openFileMapping(IPCOSWrapperTest.FILENAME);
        long memHandle = osWrapper.mapViewOfFile(fileMapping);
        String str = osWrapper.readString(memHandle);
        IPCOSWrapperTest.assertEquals("String read from memory is not the same as what was written", str, IPCOSWrapperTest.TEXT);
        long event = osWrapper.openEvent(IPCOSWrapperTest.EVENTNAME);
        osWrapper.setEvent(event);
        
        osWrapper.waitForSingleObject(mutex);
        str = osWrapper.readString(memHandle);
        IPCOSWrapperTest.assertEquals("String read from memory is not the same as what was written", str, IPCOSWrapperTest.TEXT2);
        osWrapper.releaseMutex(mutex);
        
        osWrapper.unmapViewOfFile(memHandle);
        osWrapper.closeHandle(fileMapping);
        osWrapper.closeHandle(event);
        osWrapper.closeHandle(mutex);
    }

    /**
     * @param osWrapperTest
     *            The osWrapperTest to set.
     */
    public void setOsWrapperTest(IPCOSWrapperTest osWrapperTest) {
        this.osWrapperTest = osWrapperTest;
    }
}
