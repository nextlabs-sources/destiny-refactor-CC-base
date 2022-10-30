/*
 * Created on Dec 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager.test;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.controlmanager.CMRequestHandler;
import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.IPCConstants;
import com.bluejungle.destiny.agent.ipc.IPCUserModeStub;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.pf.domain.destiny.action.IDAction;

/**
 * @author hfriedland
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class CMRequestHandlerTest extends TestCase {

    public static final String SHARED_MEMORY_NAME = "SERVER";
    public static final String SEND_EVENT_NAME = "SERVER_SEND";
    public static final String RECEIVE_EVENT_NAME = "SERVER_RECEIVE";
    public static final String REQUEST_SHARED_MEMORY_NAME = "REQUEST";
    public static final String REQUEST_SEND_EVENT_NAME = "REQUEST_SEND";
    public static final String REQUEST_RECEIVE_EVENT_NAME = "REQUEST_RECEIVE";
    public static final String REQUEST_HANDLER_CLASS = CMRequestHandler.class.getName();

    private static final String REQUEST_STR = "<request><method name=\"queryDecisionEngine\"><param value=\"param1\"/><param value=\"" + IDAction.EDIT_NAME
            + "\"/><param value=\"param3\"/><param value=\"param4\"/><param value=\"param5\"/></method></request>";

    public CMRequestHandlerTest(String name) {
        super(name);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ComponentManagerFactory.getComponentManager().getComponent(TestEvaluationEngine.class);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCMRequestHandler() {
        System.out.println("Start CM Request Handler Test...");
        //start stub
        System.out.println("Instatiate Stub...");
        IPCUserModeStub stub = (IPCUserModeStub) ComponentManagerFactory.getComponentManager().getComponent(IPCUserModeStub.class);

        //Iannis ?
        //stub.init(REQUEST_HANDLER_CLASS, 10);
        Thread stubThread = new Thread(stub);
        stubThread.start();

        String serverSharedMemoryFileName = REQUEST_HANDLER_CLASS + IPCConstants.SHARED_MEMORY_SUFFIX;
        String sendEventName = REQUEST_HANDLER_CLASS + IPCConstants.SEND_EVENT_SUFFIX;
        String receiveEventName = REQUEST_HANDLER_CLASS + IPCConstants.RECEIVE_EVENT_SUFFIX;
        String mutexName = REQUEST_HANDLER_CLASS + IPCConstants.MUTEX_SUFFIX;

        IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
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
        osWrapper.writeString(requestSharedMem, REQUEST_STR);
        osWrapper.setEvent(requestSendEvent);
        osWrapper.waitForSingleObject(requestReceiveEvent);

        stubThread.interrupt();
        stub.stop();
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

}
