/*
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc., 
 * Redwood City CA, Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 *
 * Author : Dominic Lam
 * Date   : 5/23/2006
 * Note   : Linux Kernel interface is different as we are using socket
 * 
 */

/*
 * @author ctlam
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/IPCLinuxStub.java#1 $
 *
 */

package com.bluejungle.destiny.agent.ipc;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.comp.IConfiguration;

public class IPCLinuxStub extends IPCStubBase {

    public static final int BJ_KERNELPEP = 31;

    //private static final ComponentInfo COMP_INFO = new ComponentInfo(IPCLinuxStub.class.getName(), IPCLinuxStub.class.getName(), null, LifestyleType.TRANSIENT_TYPE);    


    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    //public ComponentInfo getComponentInfo() {
    //    return COMP_INFO;
    //}

    


    private long socketHandle = -1;

    // Making an array just to reuse the API from Window
    // This must has only one element  
    private final long[] dummySocketArray = new long[1];


    //[jzhang 092206] Distinguish between user mode socket and kernel socket by using configuration
    private IConfiguration config = null;
    
    public void setConfiguration(IConfiguration config){
    super.setConfiguration(config);
	this.config = config;
    }

    public IConfiguration getConfiguration(){
	return this.config;
    }

    ///////[jzhang]

    public void init() {
        super.init();
        final IOSWrapper osWrapper = getOSWrapper();
        // Open a socket type for kernel communication
	int type = ((Integer) getConfiguration().get(STUB_TYPE)).intValue();
	if(type==1)  //kernel stub
	    socketHandle = osWrapper.setupSocket(BJ_KERNELPEP);
	else //usermode stub
	    socketHandle = osWrapper.setupSocket(0);	 
        if (socketHandle == -1) {
            throw new UnsupportedOperationException ("Failed to setup the kernel socket"); 
        } else {
	    //use NEGATIVE handle value for user mode
	    if(type==0)
		socketHandle = -socketHandle;
	    dummySocketArray[0] = socketHandle;
        }
        
    }
    
    public void run() {

        final IThreadPool threadPool = getThreadPool();
        while (!Thread.interrupted()) {
            try {
                // Use one call to do wait AND converting the Policy Request to 
                // save the round-trip time between C++/Java
                Object [] policyRequest = getOSWrapper().getNextKernelPolicyRequest(dummySocketArray);

                if (policyRequest == null) {
                    getLog().error("Invalid kernel policy, ignoring");
                    continue;
                }
                if (policyRequest.length != 2) {
                    getLog().error("Invalid kernel policy length, ignoring");
                    continue;
                }

                final long index = Long.parseLong((String) policyRequest[0]);
                final Object[] objInputParams = (Object[]) policyRequest[1];
                if (objInputParams == null) {
                    getLog().error("Invalid kernel policy request length (null policy request input params), ignoring");
                    continue;
                }
                
                final String[] inputParams = new String[objInputParams.length];
                for (int i = 0; i < objInputParams.length; i++) {
                    inputParams[i] = (String) objInputParams[i];
                }

                threadPool.doWork (new KernelRequestTaskImpl(getOSWrapper(), index, inputParams, socketHandle));

            } catch (Exception e) {
                getLog().error ("Error while process IPC request.", e);
                continue;
            }
        }
    }

    public void stop() {
        getOSWrapper().shutdownSocket(socketHandle);
        super.stop();
    }

}
