/*
 * Created on Feb 3, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.ipc;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.comp.IHasComponentInfo;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class IPCKernelStub extends IPCStubBase  implements IHasComponentInfo<IPCKernelStub>{

    /**
     * Size of the even array cannot exceed 32. Don't change it without changing
     * JNI.
     */
    private static final int EVENT_ARRAY_SIZE = 32;
    private long ipcSetupHandle = -1;

    private final long[] eventArray = new long[EVENT_ARRAY_SIZE];

    /**
     * Component information
     */
    private static final ComponentInfo<IPCKernelStub> COMP_INFO = new ComponentInfo<IPCKernelStub>(
    		IPCKernelStub.class,
    		LifestyleType.TRANSIENT_TYPE);

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<IPCKernelStub> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        final IOSWrapper osWrapper = getOSWrapper();
        //First, create 32 events
        for (int i = 0; i < EVENT_ARRAY_SIZE; i++) {
            eventArray[i] = osWrapper.createEvent("IPCKernelStub_Event_" + i);
        }
        //Pass these events to Kernel for init
        ipcSetupHandle = osWrapper.setupKernelIPC(EVENT_ARRAY_SIZE, eventArray);
        if (ipcSetupHandle == 0) {
            throw new NullPointerException ("Cannot setup the kernelIPC channel");
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        final IThreadPool threadPool = getThreadPool();
        while (!Thread.interrupted()) {
            try {
                final Object[] kernelPolicyRequest = getOSWrapper().getNextKernelPolicyRequest(this.eventArray);
                getLog().info("GOT EVENT!");
                if (kernelPolicyRequest != null) {
                    if (kernelPolicyRequest.length != 2) {
                        getLog().error("Invalid kernel policy request length, ignoring");
                        continue;
                    }

                    final long index = Long.parseLong((String) kernelPolicyRequest[0]);
                    final Object[] objInputParams = (Object[]) kernelPolicyRequest[1];
                    if (objInputParams == null) {
                        getLog().error("Invalid kernel policy request length (null policy request input params), ignoring");
                        continue;
                    }

                    final String[] inputParams = new String[objInputParams.length];
                    for (int i = 0; i < objInputParams.length; i++) {
                        inputParams[i] = (String) objInputParams[i];
                    }
                    threadPool.doWork(new KernelRequestTaskImpl(getOSWrapper(), index, inputParams, ipcSetupHandle));
                } else {
                    getLog().error("NULL kernel policy request, ignoring");
                    continue;
                }
            } catch (Exception e) {
                getLog().error("Error while process IPC kernel request. Request is skipped", e);
                continue;
            }
        }
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IIPCStub#stop()
     */
    public void stop() {
        getOSWrapper().uninitKernelIPC(ipcSetupHandle);
        super.stop();
    }

}
