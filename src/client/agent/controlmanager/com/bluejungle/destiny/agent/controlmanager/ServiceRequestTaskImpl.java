/*
 * Created on Feb 24, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2009 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.framework.threading.ITask;
/**
 * This is the servfice request task implementation class.
 * 
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/ServiceRequestTaskImpl.java#1 $
 */

public class ServiceRequestTaskImpl implements ITask {

    private Object[] inputParams;
    private IPDPJni pdpJni;
    private long handle;
    private IServiceRequestResponseHandler responseHandler;

    /**
     * Constructor
     * 
     * @param requestNb
     *            request number
     * @param inParams
     *            input parameters for policy evaluation request
     */
    public ServiceRequestTaskImpl(IPDPJni wrapper, Object[] inParams, long handle, IServiceRequestResponseHandler responseHandler) {
        super();
        this.pdpJni = wrapper;
        this.inputParams = inParams;
        this.handle = handle;
        this.responseHandler = responseHandler;
    }

    public Object[] getInputParams() {
        return inputParams;
    }

    public IPDPJni getOSWrapper() {
        return pdpJni;
    }

    public long getHandle() {
        return handle;
    }

    public IServiceRequestResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public static interface IServiceRequestResponseHandler {
        void respond(ServiceRequestTaskImpl request, String reqId, Object[] args);
    }
}
