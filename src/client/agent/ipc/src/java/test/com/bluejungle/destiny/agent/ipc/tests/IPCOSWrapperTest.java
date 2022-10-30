// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc.tests;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.IPCConstants;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.oil.OSType;
/**
 * 
 * JUnit test to test the JNI OS wrapper.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 *  
 */
public class IPCOSWrapperTest extends TestCase {

    public static final String TEXT = "This is the string that is written";

    public static final String TEXT2 = "This is the second string that is written";

    public static final String FILENAME = "FILENAME";

    public static final String EVENTNAME = "EVENTNAME";

    public static final String EVENTNAME2 = "EVENTNAME2";

    public static final String EVENTNAME3 = "EVENTNAME3";

    public static final String MUTEXNAME = "MUTEXNAME";

    private IOSWrapper osWrapper = null;

    private OSType ot;
    /**
     * @param name
     *            name of test method
     */
    public IPCOSWrapperTest(String name) {
        super(name);
        this.ot = new OSType();
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    }

    /**
     * Creates some events and shared memory. Writes a string to the shared
     * memory. Starts another thread with class TestHelperThread and waits on an
     * event. The other thread reads the shared memory and sets the event to
     * restart this thread. 
     * 
     * Checks that the right event was fired.
     * 
     * Closes handles to OS events.
     */
 //   public void testOSWrapper() {
 //       assertNotNull("osWrapper is null", this.osWrapper);
 //       long fileMapping = this.osWrapper.createFileMapping(FILENAME, IPCConstants.CHANNEL_SIZE);
 //       long memHandle = this.osWrapper.mapViewOfFile(fileMapping);
 //       long mutex = this.osWrapper.createMutex(MUTEXNAME);
 //       long event = this.osWrapper.createEvent(EVENTNAME);
//        long event2 = this.osWrapper.createEvent(EVENTNAME2);
//        long event3 = this.osWrapper.createEvent(EVENTNAME3);
//        this.osWrapper.waitForSingleObject(mutex);
        
//        //use thread for synchronization
//        this.osWrapper.writeString(memHandle, TEXT);
//        long[] eventArray = new long[3];
//        eventArray[0] = event;
//        eventArray[1] = event2;
//        eventArray[2] = event3;
//        TestHelperThread helper = new TestHelperThread();
//        helper.setOsWrapperTest(this);
//        Thread t = new Thread(helper);
//        t.start();
//        int retEvent = this.osWrapper.waitForMultipleObjects(1, eventArray, null, IPCConstants.WAIT_TIMEOUT);
//        assertEquals("Unexpected event was set" , eventArray[retEvent], event);

//        //use mutex for synchronization
//        this.osWrapper.writeString(memHandle, TEXT2);        
//        this.osWrapper.releaseMutex(mutex);
        
//        this.osWrapper.unmapViewOfFile(memHandle);
//        this.osWrapper.closeHandle(fileMapping);
//        this.osWrapper.closeHandle(event);
//        this.osWrapper.closeHandle(event2);
 //       this.osWrapper.closeHandle(event3);
        
  //      String fqdn = this.osWrapper.getFQDN();
  //      assertNotNull(fqdn);
  //      assertTrue("hostname should not be empty", 0 != fqdn.length());
  //      assertTrue("hostname should be fully-qualified", fqdn.endsWith("bluejungle.com"));

 //   }
    
    /**
     * Tests the getLoggedInUsers() method on OSWrapper.  The method should return the host
     * as well as the logged-in users	
     */
//    public void testGetLoggedInUsers() {
//    	//this method is not supported by Linux osWrapper
//    	if(this.ot.getOSType()==OSType.OS_TYPE_LINUX)
//    		return;
//    	assertNotNull("osWrapper is null", this.osWrapper);
//    	String[] loggedInUsers = this.osWrapper.getLoggedInUsers();
 //   	assertNotNull("loggedInUsers string array is null", loggedInUsers);
 //   	assertTrue("There should be at least 1 entries returned in getLoggedInUsers", loggedInUsers.length > 1);
 //   }
}
