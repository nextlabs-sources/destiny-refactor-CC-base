/*
 * Created on May 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.ipc;

/**
 * This is the interface for IPC Stubs
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/IIPCStub.java#1 $
 */

public interface IIPCStub {

    /**
     * Configuration parameters
     */
    public static final String BUCKET_NOTIFICATION_EVENT_CONFIG_PARAM = "BUCKET_NOTIFICATION_EVENT";
    public static final String REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM = "REQUEST_HANDLER_CLASS_NAME";
    public static final String STUB_CONFIG_PARAM = "STUB";
    public static final String THREAD_POOL_SIZE_CONFIG_PARAM = "THREAD_POOL_SIZE";
    public static final String STUB_TYPE = "STUB_TYPE";

    /**
     * Shuts down the IPC stub
     *  
     */
    public void stop();
}
