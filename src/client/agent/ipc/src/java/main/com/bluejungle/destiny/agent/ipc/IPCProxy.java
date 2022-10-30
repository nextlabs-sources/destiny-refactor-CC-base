/*
 * Created on Dec 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.ipc;

import java.util.ArrayList;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * 
 * IPCProxy implements the client side of the IPC Framework. Processes that need
 * to call methods on the IPC Stub (or server) need to instantiate IPCStub (or
 * subclass).
 * 
 * This class implements IConfigurable and needs to be provided with the request
 * handler class name as part of configuration. For an example of usage, see
 * com.bluejungle.destiny.agent.ipc.tests.IPCRequestHandlerTest
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 * @see com.bluejungle.destiny.agent.ipc.tests.IPCRequestHandlerTest
 */
public class IPCProxy implements ILogEnabled, IIPCProxy, IConfigurable {

    private static IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
    private Log log = null;
    private IConfiguration config = null;
    private boolean isInitialized = false;

    private long requestFileMapping = 0;
    private long requestSharedMem = 0;
    private long requestSendEvent = 0;
    private long requestReceiveEvent = 0;
    private long requestMutex = 0;

    // The buffers are 256 characters long, but for some reason we only allow
    // 254 characters per buffer.
    public static final int paramLen = 254;

    /**
     * 
     * Calls the other init and passes in the request handler class name
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        String requestHandlerClassName = (String) getConfiguration().get(REQUEST_HANDLER_CLASS_NAME);
        init(requestHandlerClassName);
    }

    /**
     * Sets up parser and sends handshake request to server to set up the
     * session.
     * 
     * @param requestHandlerClassName
     *            class name of IPC request handler.
     */
    public void init(String requestHandlerClassName) {
        boolean retVal = true;

        if (this.isInitialized) {
            return;
        }

        if (osWrapper.isStub()) {
            return;
        }
        
        String fileMappingName = requestHandlerClassName + IPCConstants.SHARED_MEMORY_SUFFIX;
        long fileMappingHandle = osWrapper.createFileMapping(fileMappingName, IPCConstants.CHANNEL_SIZE);
        if (fileMappingHandle == 0) {
            //handshake failed.
            getLog().info("Handshake failed: createfileMapping to " + fileMappingName + " did not return a handle");
            return;
        }

        getLog().debug("Handle for " + fileMappingName + " = " + fileMappingHandle);

        long sharedMem = 0;
        long sendEvent = 0;
        long receiveEvent = 0;
        long mutex = 0;
        if (retVal == true) {
            sharedMem = osWrapper.mapViewOfFile(fileMappingHandle);
            sendEvent = osWrapper.openEvent(requestHandlerClassName + IPCConstants.RECEIVE_EVENT_SUFFIX);
            receiveEvent = osWrapper.openEvent(requestHandlerClassName + IPCConstants.SEND_EVENT_SUFFIX);
            mutex = osWrapper.openMutex(requestHandlerClassName + IPCConstants.MUTEX_SUFFIX);

            if (sharedMem == 0 || sendEvent == 0 || receiveEvent == 0 || mutex == 0) {
                //handshake failed.
                getLog().error("Handshake failed: Could not create handshake OS objects");
                if (sharedMem != 0) {
                    osWrapper.unmapViewOfFile(sharedMem);
                }
                if (sendEvent != 0) {
                    osWrapper.closeHandle(sendEvent);
                }

                if (receiveEvent != 0) {
                    osWrapper.closeHandle(receiveEvent);
                }
                osWrapper.closeHandle(fileMappingHandle);
                return;
            }
        }

        String uniqueName = Integer.toString(osWrapper.getProcessId()) + System.nanoTime();

        String sharedMemName = IPCConstants.GLOBAL_PREFIX + uniqueName + IPCConstants.SHARED_MEMORY_SUFFIX;
        String sendEventName = IPCConstants.GLOBAL_PREFIX + uniqueName + IPCConstants.SEND_EVENT_SUFFIX;
        String receiveEventName = IPCConstants.GLOBAL_PREFIX + uniqueName + IPCConstants.RECEIVE_EVENT_SUFFIX;
        String mutexName = IPCConstants.GLOBAL_PREFIX + uniqueName + IPCConstants.MUTEX_SUFFIX;

        this.requestFileMapping = osWrapper.createFileMapping(sharedMemName, IPCConstants.CHANNEL_SIZE);
        if (this.requestFileMapping != 0) {
            this.requestSharedMem = osWrapper.mapViewOfFile(this.requestFileMapping);
            this.requestSendEvent = osWrapper.createEvent(sendEventName);
            this.requestReceiveEvent = osWrapper.createEvent(receiveEventName);
            this.requestMutex = osWrapper.createMutex(mutexName);
            if (sharedMem == 0 || sendEvent == 0 || receiveEvent == 0 || mutex == 0) {
                //handshake failed
                getLog().error("Handshake failed: Could not create request channel");
                if (this.requestSharedMem != 0) {
                    osWrapper.unmapViewOfFile(this.requestSharedMem);
                }
                if (this.requestSendEvent != 0) {
                    osWrapper.closeHandle(this.requestSendEvent);
                }

                if (this.requestReceiveEvent != 0) {
                    osWrapper.closeHandle(this.requestReceiveEvent);
                }
                osWrapper.closeHandle(this.requestFileMapping);
                return;
            }
        } else {
            //handshake failed
            getLog().error("Handshake failed: Could not create request channel");
            return;
        }

        // Start Handshake
        osWrapper.waitForSingleObject(mutex);

        String handshakeRequest = IPCConstants.CONNECT + "\n" + sharedMemName + "\n" + receiveEventName + "\n" + sendEventName;
        getLog().debug("Making handshake request " + handshakeRequest);

        osWrapper.writeString(sharedMem, handshakeRequest);

        osWrapper.setEvent(sendEvent);
        int waitResult = osWrapper.waitForSingleObject(receiveEvent);

        if (waitResult == IPCConstants.WAIT_TIMEOUT || waitResult == -1 && waitResult == IPCConstants.WAIT_STATUS_ABANDONED_0) {
            getLog().trace("Handshake failed for " + requestHandlerClassName + " " + waitResult);
        } else {
            this.isInitialized = true;
        }
        osWrapper.releaseMutex(mutex);
        // End Handshake

        osWrapper.unmapViewOfFile(sharedMem);
        osWrapper.closeHandle(fileMappingHandle);
        osWrapper.closeHandle(sendEvent);
        osWrapper.closeHandle(receiveEvent);
        osWrapper.closeHandle(mutex);
    }

    /**
     * 
     * Sends the method invocation request to the server, reads and parses the
     * response and adds the result parameters to resultParams
     * 
     * @see com.bluejungle.destiny.agent.ipc.IIPCProxy#invoke(java.lang.String,
     *      java.util.ArrayList, java.util.ArrayList)
     */
    public synchronized boolean invoke(String methodName, ArrayList inputParams, ArrayList resultParams) {

        if (!this.isInitialized) {
            return false;
        }

        osWrapper.waitForSingleObject(this.requestMutex);

        this.writeIPCRequest(methodName, inputParams);

        osWrapper.setEvent(this.requestSendEvent);
        String[] response;
        boolean ret;
        int waitResult = osWrapper.waitForSingleObject(this.requestReceiveEvent);
        if (waitResult != IPCConstants.WAIT_TIMEOUT && waitResult != -1 && waitResult != IPCConstants.WAIT_STATUS_ABANDONED_0) {
            response = readIPCResponse();
            ret = true;
        } else {
            ret = false;
            response = new String[0];
        }
        osWrapper.releaseMutex(this.requestMutex);

        if (ret && resultParams != null) {
            for (int i = 0; i < response.length; i++) {
                resultParams.add(response[i]);
            }
        }
        return (ret);
    }

    /**
     * @return
     */
    protected String[] readIPCResponse() {
        String[] response = osWrapper.readIPCResponse(this.requestSharedMem);
        return response;
    }

    /**
     * @param methodName
     * @param inputParams
     */
    protected void writeIPCRequest(String methodName, ArrayList inputParams) {
        String[] strArray = new String[inputParams.size() + 1];
        strArray[0] = methodName;
        for (int i = 0; i < inputParams.size(); i++) {
            strArray[i + 1] = (String) inputParams.get(i);
        }

        osWrapper.writeIPCRequest(this.requestSharedMem, strArray);
    }

    /**
     * 
     * Sends a Disconnect request to the server and closes all OS object
     * handles.
     * 
     * @see com.bluejungle.destiny.agent.ipc.IIPCProxy#uninit()
     */
    public synchronized void uninit() {
        if (!this.isInitialized) {
            return;
        }

        this.isInitialized = false;

        osWrapper.waitForSingleObject(this.requestMutex);
        ArrayList params = new ArrayList();
        params.add(IPCConstants.DISCONNECT);
        osWrapper.writeIPCRequest(this.requestSharedMem, params.toArray());
        osWrapper.setEvent(this.requestSendEvent);
        osWrapper.waitForSingleObject(this.requestReceiveEvent);
        osWrapper.releaseMutex(this.requestMutex);

        osWrapper.unmapViewOfFile(this.requestSharedMem);
        osWrapper.closeHandle(this.requestFileMapping);
        osWrapper.closeHandle(this.requestSendEvent);
        osWrapper.closeHandle(this.requestReceiveEvent);
        osWrapper.closeHandle(this.requestMutex);
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

}
