// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.threading.ITask;

/**
 * Each instance of this class represents one channel of communication between
 * client and server IPC processes. Whenever a client comes up, it performs a
 * handshake by providing a shared memory location and two events. The shared
 * memory is used communication and the two events are used to synchronize
 * requests. An instance of this class is created to represent the channel.
 * 
 * This class implements ITask so that it can be used as a task for Threadpool
 * 
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/IPCMessageChannel.java#1 $:
 */
public class IPCMessageChannel implements ITask {

    private static IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    private long sharedFileMapping;

    private long sharedMemoryHandle;

    private long sendEvent;

    private long receiveEvent;

    private boolean fileMappingCloseRequired = true;

    /**
     * @param sharedFileMapping
     *            handle of shared file mapping for this channel
     * @param sharedMemoryHandle
     *            handle for shared memory
     * @param sendEvent
     *            handle for send event for this channel. The events are named
     *            with respect to the stub. This means the send event is
     *            signalled by the stub when it is sending a message.
     * @param receiveEvent
     *            handle for receive event for this channel The events are named
     *            with respect to the stub. The stub waits on the recieve event
     *            to get messages from the client
     *  
     */
    IPCMessageChannel(long sharedFileMapping, long sharedMemoryHandle, long sendEvent, long receiveEvent) {
        this.sharedFileMapping = sharedFileMapping;
        this.sharedMemoryHandle = sharedMemoryHandle;
        this.sendEvent = sendEvent;
        this.receiveEvent = receiveEvent;
    }

    /**
     * @return Returns the receiveEvent.
     */
    public long getReceiveEvent() {
        return receiveEvent;
    }

    /**
     * @param receiveEvent
     *            The receiveEvent to set.
     */
    public void setReceiveEvent(int receiveEvent) {
        this.receiveEvent = receiveEvent;
    }

    /**
     * @return Returns the sendEvent.
     */
    public long getSendEvent() {
        return sendEvent;
    }

    /**
     * @param sendEvent
     *            The sendEvent to set.
     */
    public void setSendEvent(int sendEvent) {
        this.sendEvent = sendEvent;
    }

    /**
     * @return Returns the sharedMemoryHandle.
     */
    public long getSharedMemoryHandle() {
        return sharedMemoryHandle;
    }

    /**
     * @param sharedMemoryHandle
     *            The sharedMemoryHandle to set.
     */
    public void setSharedMemoryHandle(int sharedMemoryHandle) {
        this.sharedMemoryHandle = sharedMemoryHandle;
    }

    /**
     * @return string read from shared memory
     */
    public String readString() {
        return (osWrapper.readString(sharedMemoryHandle));
    }

    /**
     * Writes string to shared memory and sets the event to signal the caller.
     * 
     * @param message
     *            string to write to shared memory
     */
    public void writeString(String message) {
        osWrapper.writeString(this.sharedMemoryHandle, message);
        osWrapper.setEvent(this.sendEvent);
    }

    public String[] readIPCResponse() {
        return osWrapper.readIPCResponse(this.sharedMemoryHandle);
    }

    public String[] readIPCRequest() {
        return osWrapper.readIPCRequest(this.sharedMemoryHandle);
    }

    public void writeIPCRequest(Object[] inputParams) {
        osWrapper.writeIPCRequest(this.sharedMemoryHandle, inputParams);
    }

    public void writeIPCResponse(Object[] outputParams) {
        osWrapper.writeIPCResponse(this.sharedMemoryHandle, outputParams);
        osWrapper.setEvent(this.sendEvent);
    }

    /**
     * Closes all OS handles for the message channel
     */
    public synchronized void close() {
        if (fileMappingCloseRequired) {
            osWrapper.unmapViewOfFile(sharedMemoryHandle);
            osWrapper.closeHandle(sharedFileMapping);
        }
        osWrapper.closeHandle(sendEvent);
        osWrapper.closeHandle(receiveEvent);
        sharedMemoryHandle = 0;
        sharedFileMapping = 0;
        sendEvent = 0;
        receiveEvent = 0;
    }

    public synchronized boolean isValid() {
        return (sharedMemoryHandle != 0);
    }

    /**
     * Sets the fileMappingCloseRequired flag
     * 
     * @param fileMappingCloseRequired
     *            The fileMappingCloseRequired to set.
     */
    public void setFileMappingCloseRequired(boolean fileMappingCloseRequired) {
        this.fileMappingCloseRequired = fileMappingCloseRequired;
    }
}
