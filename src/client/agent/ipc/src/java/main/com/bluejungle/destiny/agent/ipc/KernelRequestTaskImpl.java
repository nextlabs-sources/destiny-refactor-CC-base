/*
 * Created on May 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.ipc;


/**
 * This is the kernel request task implementation class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/KernelRequestTaskImpl.java#1 $
 */

public class KernelRequestTaskImpl implements IKernelRequestTask {

    private String[] inputParams;
    private IOSWrapper osWrapper;
    private long requestNumber;
    private long handle;

    /**
     * Constructor
     * 
     * @param requestNb
     *            request number
     * @param inParams
     *            input parameters for policy evaluation request
     */
    public KernelRequestTaskImpl(IOSWrapper wrapper, long requestNb, String[] inParams, long handle) {
        super();
        this.osWrapper = wrapper;
        this.requestNumber = requestNb;
        this.inputParams = inParams;
        this.handle      = handle;
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IKernelRequestTask#getInputParams()
     */
    public String[] getInputParams() {
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
    public long getRequestNumber() {
        return requestNumber;
    }


    /**
     * @see com.bluejungle.destiny.agent.ipc.IKernelRequestTask#getHandle()
     */
    public long getHandle() {
        return handle;
    }

}
