// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc;

import java.util.ArrayList;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.ITask;
import com.bluejungle.framework.threading.IWorker;

/**
 * Abstract class to be used as a base class for IPC Request handlers.
 * 
 * For an example, see com.bluejungle.destiny.agent.ipc.tests.TestRequestHandler
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */
public abstract class RequestHandlerBase implements IRequestHandler,
		IHasComponentInfo<RequestHandlerBase>, ILogEnabled, IConfigurable, IInitializable, IWorker {

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<RequestHandlerBase> getComponentInfo() {
        return (new ComponentInfo<RequestHandlerBase>(
        		this.getClass().getName(), 
        		this.getClass(), 
        		IRequestHandler.class, 
        		LifestyleType.TRANSIENT_TYPE));
    }

    private IConfiguration config = null;
    protected IPCUserModeStub stub = null;
    protected Log log;

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * Set up parser, retreive IPCStub and request queue from config.
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.stub = (IPCUserModeStub) getConfiguration().get(IIPCStub.STUB_CONFIG_PARAM);
    }

    /**
     * Handles request specified by Task by calling Invoke. Invoke must be
     * implemented by a concrete subclass.
     * 
     * @see com.bluejungle.framework.threading.IWorker#doWork(com.bluejungle.framework.threading.ITask)
     */
    public void doWork(ITask task) {
        IPCMessageChannel messageChannel = (IPCMessageChannel) task;
        handleRequest(messageChannel);

    }

    /**
     * @param messageChannel
     *            instance of message channel that the request is sent on.
     */
    private void handleRequest(IPCMessageChannel messageChannel) {
        String[] inputParams = readIPCRequest(messageChannel);
        if (inputParams == null) {
            return;
        }
        String methodName = inputParams[0];
        if (methodName.equals(IPCConstants.DISCONNECT)) {
            synchronized (messageChannel) {
                if (messageChannel.isValid()) {
                    //signal event before closing event handles.
                    messageChannel.writeString("");
                } else {
                    getLog().warn("while notifying agent of acknowledgement of disconnection, invalid message channel");
                }
            }
            this.stub.removeChannel(messageChannel);
        } else {
            ArrayList paramArray = new ArrayList();
            ArrayList resultParams = new ArrayList();
            for (int i = 1; i < inputParams.length; i++) {
                 paramArray.add(inputParams[i]);
            }
            if (methodName != null) {
                invoke(methodName, paramArray, resultParams);

                if (getLog().isTraceEnabled()) {
                    getLog().trace(Thread.currentThread().getName() + ": IPC Response: ?");
                }
            }

            writeIPCResponse(messageChannel, resultParams);
        }
    }

    /**
     * @param messageChannel
     * @param resultParams
     */
    protected void writeIPCResponse(IPCMessageChannel messageChannel, ArrayList resultParams) {
        synchronized (messageChannel) {
            if (messageChannel.isValid()) {
                messageChannel.writeIPCResponse(resultParams.toArray());
            } else {
                this.getLog().error("Tried to write to invalid IPC slot. Caller may be dead. Ignoring to avoid crash.");
            }
        }
    }

    /**
     * @param messageChannel
     * @return
     */
    protected String[] readIPCRequest(IPCMessageChannel messageChannel) {
        synchronized (messageChannel) {
            if (messageChannel.isValid()) {
                String[] inputParams = messageChannel.readIPCRequest();
                return inputParams;
            } else {
                this.getLog().error("Tried to read from invalid IPC slot. Caller may be dead. Ignoring to avoid crash.");
            }
        }
        return null;
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

}
