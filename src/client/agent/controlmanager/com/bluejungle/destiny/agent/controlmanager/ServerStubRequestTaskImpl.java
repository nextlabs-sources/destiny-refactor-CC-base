/*
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.framework.threading.ITask;
/**
 * This is the policy evaluation request task implementation class.
 * 
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/ServerStubRequestTaskImpl.java#1 $
 */

public class ServerStubRequestTaskImpl implements ITask {

    private Object[] inputParams;
    private IPDPJni pdpJni;
    private String requestID;
    private long handle;
    private IServerStubResponseHandler responseHandler;

    /**
     * Constructor
     * 
     * @param requestNb
     *            request number
     * @param inParams
     *            input parameters for policy evaluation request
     */
    public ServerStubRequestTaskImpl(IPDPJni wrapper, String requestNb, Object[] inParams, long handle, IServerStubResponseHandler responseHandler) {
        super();
        this.pdpJni = wrapper;
        this.requestID = requestNb;
        this.inputParams = inParams;
        this.handle      = handle;
        this.responseHandler = responseHandler;
    }

    public Object[] getInputParams() {
        return inputParams;
    }

    public IPDPJni getOSWrapper() {
        return pdpJni;
    }

    public String getRequestNumber() {
        return requestID;
    }

    public long getHandle() {
        return handle;
    }

    public IServerStubResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public static interface IServerStubResponseHandler {
        void respond(ServerStubRequestTaskImpl request, int resultCode, String[] obligations);
    }
}
