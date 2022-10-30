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
 * This is the kernel request task interface. It exposes the kernel request task
 * parameters.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/IKernelRequestTask.java#1 $
 */

public interface IKernelRequestTask extends ITask {

    /**
     * Returns the input parameters for the policy request evaluation
     * 
     * @return the input parameters for the policy request evaluation
     */
    public String[] getInputParams();

    /**
     * Returns the OS Wrapper object to access JNI functions
     * 
     * @return the OS Wrapper object to access JNI functions
     */
    public IOSWrapper getOSWrapper();

    /**
     * Returns the unique request number pending in the kernel
     * 
     * @return the unique request number pending in the kernel
     */
    public long getRequestNumber();


    /**
     * Returns the handle of the communication
     * 
     * @return the handle of the communication 
     */
    public long getHandle();

    
}
