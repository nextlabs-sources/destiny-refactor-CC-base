/*
 * Created on May 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.ipc;

import com.bluejungle.framework.threading.ITask;
/**
 * This is the kernel request task implementation class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/ServerStubRequestTaskImpl.java#1 $
 */

public class ServerStubRequestTaskImpl implements ITask{

    private Object[] inputParams;
    private IOSWrapper osWrapper;
    private String requestID;
    private long handle;

    /**
     * Constructor
     * 
     * @param requestNb
     *            request number
     * @param inParams
     *            input parameters for policy evaluation request
     */
    public ServerStubRequestTaskImpl(IOSWrapper wrapper, String requestNb, Object[] inParams, long handle) {
        super();
        this.osWrapper = wrapper;
        this.requestID = requestNb;
        this.inputParams = inParams;
        this.handle      = handle;
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IKernelRequestTask#getInputParams()
     */
    public Object[] getInputParams() {
        return inputParams;
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IKernelRequestTask#getOSWrapper()
     */
    public IOSWrapper getOSWrapper() {
        return osWrapper;
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IKernelRequestTask#getRequestNumber()
     */
    public String getRequestNumber() {
        return requestID;
    }


    /**
     * @see com.bluejungle.destiny.agent.ipc.IKernelRequestTask#getHandle()
     */
    public long getHandle() {
        return handle;
    }

}
