// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.ipc.IIPCProxy;
import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.IPCConstants;
import com.bluejungle.destiny.agent.ipc.IPCProxy;
import com.bluejungle.destiny.agent.ipc.IPCUserModeStub;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.oil.OSType;

/**
 * 
 * Test for IPCProxy and IPCStub.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 *  
 */
public class IPCRequestHandlerTest extends TestCase {

    public static final String SHARED_MEMORY_NAME = "SERVER";

    public static final String SEND_EVENT_NAME = "SERVER_SEND";

    public static final String RECEIVE_EVENT_NAME = "SERVER_RECEIVE";

    public static final String REQUEST_SHARED_MEMORY_NAME = "REQUEST";

    public static final String REQUEST_SEND_EVENT_NAME = "REQUEST_SEND";

    public static final String REQUEST_RECEIVE_EVENT_NAME = "REQUEST_RECEIVE";

    public static final String REQUEST_HANDLER_CLASS = TestRequestHandler.class.getName();

    private static IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    private IPCUserModeStub stub = null;

    private OSType ot;
    /**
     * Constructor for IPCRequestHandlerTest.
     */
    public IPCRequestHandlerTest() {
    	this.ot = new OSType();
    }

    /**
     * Constructor for IPCRequestHandlerTest.
     * 
     * @param name
     *            name is used to specify the JUnit test to run
     */
    public IPCRequestHandlerTest(String name) {
        super(name);
    	this.ot = new OSType();
    }

    /**
     * @param args
     * 
     * The main method sets up the IPC request handler and waits for requests
     * from the client. This will be used to test the C++ proxy.
     * 
     * The method also waits for keyboard input. If the letter x is typed, the
     * program will close the request handler and exit
     *  
     */
    public static void main(String[] args) {
        IPCRequestHandlerTest test = new IPCRequestHandlerTest();
        test.setupStub();

        InputStreamReader unbuffered = new InputStreamReader(System.in);
        BufferedReader keyboard = new BufferedReader(unbuffered);

        while (true) {
            try {
                if (keyboard.readLine().equalsIgnoreCase("X")) {
                    test.stub.stop();
                    System.out.println("IPC Stub Stopped.");
                    System.exit(0);
                }
            } catch (IOException e) {
                IPCRequestHandlerTest.fail("IOException thrown when exiting test: " + e.getMessage());
            }
        }

    }

    /**
     * set up the IPC Stub to wait for request.
     *  
     */
    public void setupStub() {
        System.out.println("Start IPC Request Handler Test...");
        //start stub
        System.out.println("Instatiate Stub...");
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPCUserModeStub.REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM, REQUEST_HANDLER_CLASS);
        config.setProperty(IPCUserModeStub.THREAD_POOL_SIZE_CONFIG_PARAM, new Integer(10));
        this.stub = (IPCUserModeStub) ComponentManagerFactory.getComponentManager().getComponent(IPCUserModeStub.class, config);
        //        stub.init(REQUEST_HANDLER_CLASS, 10);
        Thread stubThread = new Thread(this.stub);
        stubThread.start();
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Starts the Stub with TestRequestHandler. Sends handshake request to
     * server to setup session Sends method request to server and check
     * response. Stops Stub.
     */
    public void testRequestHandler() {
        System.out.println("Start IPC Request Handler Test...");
        //start stub
        System.out.println("Instantiate Stub...");
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPCUserModeStub.REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM, REQUEST_HANDLER_CLASS);
        config.setProperty(IPCUserModeStub.THREAD_POOL_SIZE_CONFIG_PARAM, new Integer(10));
        this.stub = (IPCUserModeStub) ComponentManagerFactory.getComponentManager().getComponent(IPCUserModeStub.class, config);
        //        stub.init(REQUEST_HANDLER_CLASS, 10);
        Thread stubThread = new Thread(this.stub);
        stubThread.start();

        String serverSharedMemoryFileName = REQUEST_HANDLER_CLASS + IPCConstants.SHARED_MEMORY_SUFFIX;
        String sendEventName = REQUEST_HANDLER_CLASS + IPCConstants.SEND_EVENT_SUFFIX;
        String receiveEventName = REQUEST_HANDLER_CLASS + IPCConstants.RECEIVE_EVENT_SUFFIX;
        String mutexName = REQUEST_HANDLER_CLASS + IPCConstants.MUTEX_SUFFIX;

        long handshakeFile = osWrapper.openFileMapping(serverSharedMemoryFileName);
        long handshakeSharedMem = osWrapper.mapViewOfFile(handshakeFile);
        long handshakeSendEvent = osWrapper.openEvent(receiveEventName);
        long handshakeReceiveEvent = osWrapper.openEvent(sendEventName);
        long handshakeMutex = osWrapper.openMutex(mutexName);

        //perform handshake
        long requestFile = osWrapper.createFileMapping(REQUEST_SHARED_MEMORY_NAME, IPCConstants.CHANNEL_SIZE);
        long requestSharedMem = osWrapper.mapViewOfFile(requestFile);
        long requestSendEvent = osWrapper.createEvent(REQUEST_SEND_EVENT_NAME);
        long requestReceiveEvent = osWrapper.createEvent(REQUEST_RECEIVE_EVENT_NAME);

        StringBuffer buf = new StringBuffer(REQUEST_SHARED_MEMORY_NAME.length() + REQUEST_SEND_EVENT_NAME.length() + REQUEST_RECEIVE_EVENT_NAME.length() + 3);

        buf.append(IPCConstants.CONNECT);
        buf.append("\n");
        buf.append(REQUEST_SHARED_MEMORY_NAME);
        buf.append("\n");
        buf.append(REQUEST_RECEIVE_EVENT_NAME);
        buf.append("\n");
        buf.append(REQUEST_SEND_EVENT_NAME);
        osWrapper.waitForSingleObject(handshakeMutex);
        osWrapper.writeString(handshakeSharedMem, buf.toString());
        osWrapper.setEvent(handshakeSendEvent);
        osWrapper.waitForSingleObject(handshakeReceiveEvent);
        osWrapper.releaseMutex(handshakeMutex);
        System.out.println("Handshake complete");
        //handshake complete

        //send request
        System.out.println("Sending Request...");
        ArrayList params = new ArrayList();
        params.add("method1");
        params.add("param1");
        params.add("param2");
        osWrapper.writeIPCRequest(requestSharedMem, params.toArray());
        osWrapper.setEvent(requestSendEvent);
        osWrapper.waitForSingleObject(requestReceiveEvent);
        //Linux osWrapper does not support readIPCResponse
        if(this.ot.getOSType()==OSType.OS_TYPE_WINDOWS)
        {
        	String[] response = osWrapper.readIPCResponse(requestSharedMem);
        	assertEquals("Unexpected response", response[0], "retval1");
            assertEquals("Unexpected response", response[1], "retval2");
            System.out.println("Received Response...");
        }
        
        stubThread.interrupt();
        this.stub.stop();
        System.out.println("Stopped IPC Stub...");

        osWrapper.unmapViewOfFile(handshakeSharedMem);
        osWrapper.closeHandle(handshakeFile);
        osWrapper.closeHandle(handshakeSendEvent);
        osWrapper.closeHandle(handshakeReceiveEvent);
        osWrapper.closeHandle(handshakeMutex);
        osWrapper.unmapViewOfFile(requestSharedMem);
        osWrapper.closeHandle(requestFile);
        osWrapper.closeHandle(requestSendEvent);
        osWrapper.closeHandle(requestReceiveEvent);
        System.out.println("IPC Request Handler Test Complete.");
    }

    /**
     * Test the IPCProxy and IPCStub by setting up the stub and proxy, making a
     * method call and inspecting the response
     */
    public void testProxy() {
        System.out.println("Start IPC Proxy Test...");
        //start stub
        System.out.println("Instantiate Stub...");
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPCUserModeStub.REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM, REQUEST_HANDLER_CLASS);
        config.setProperty(IPCUserModeStub.THREAD_POOL_SIZE_CONFIG_PARAM, new Integer(10));
        this.stub = (IPCUserModeStub) ComponentManagerFactory.getComponentManager().getComponent(IPCUserModeStub.class, config);
        
        Thread stubThread = new Thread(this.stub);
        stubThread.start();

        config = new HashMapConfiguration();
        config.setProperty(IIPCProxy.REQUEST_HANDLER_CLASS_NAME, REQUEST_HANDLER_CLASS);
        ComponentInfo<IPCProxy> info = new ComponentInfo<IPCProxy>(
        		IPCProxy.class.getName(), 
        		IPCProxy.class, 
        		IIPCProxy.class, 
        		LifestyleType.TRANSIENT_TYPE, config);
        IIPCProxy proxy = ComponentManagerFactory.getComponentManager().getComponent(info);

        //Linux osWrapper does not support readIPCResponse
        if(this.ot.getOSType()==OSType.OS_TYPE_WINDOWS)
        {      
	        ArrayList inputArgs = new ArrayList();
	        inputArgs.add("param1");
	        inputArgs.add("param2");
	        ArrayList result = new ArrayList();
	        System.out.println("Sending Request...");
	        proxy.invoke("method1", inputArgs, result);
	        System.out.println("Received Response...");
	        assertEquals("Unexpected response", (String) result.get(0), "retval1");
	        assertEquals("Unexpected response", (String) result.get(1), "retval2");
        }
        proxy.uninit();
        System.out.println("IPC Proxy Test Complete.");
    }

}
