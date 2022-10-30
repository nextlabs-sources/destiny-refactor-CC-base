/*
 * Created on May 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.ipc;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.ThreadPool;

/**
 * This is the base IPC stub class. This base class is extended by all IPC stubs
 * implementations.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/IPCStubBase.java#1 $
 */

public abstract class IPCStubBase implements IIPCStub, Runnable, IInitializable, IConfigurable, /*IHasComponentInfo, */ILogEnabled {

    /**
     * OS wrapper communicates with C++ through JNI
     */
    private static IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    private IThreadPool threadPool = null;
    private Log log;
    private IConfiguration config;
    private String requestHandlerClassName;
    private int threadPoolSize;

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Returns the thread pool object
     * 
     * @return the thread pool object
     */
    protected IThreadPool getThreadPool() {
        return this.threadPool;
    }

    /**
     * Returns the OS Wrapper object.
     * 
     * @return the OS Wrapper object.
     */
    protected IOSWrapper getOSWrapper() {
        return IPCStubBase.osWrapper;
    }

    /**
     * Returns the request handler class name that is used in the thread pool to
     * handle IPC requests.
     * 
     * @return the request handler class name
     */
    protected String getRequestHandlerClassName() {
        return this.requestHandlerClassName;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        //Create ThreadPool
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IThreadPool.THREADPOOL_SIZE, new Integer(this.threadPoolSize));
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, getRequestHandlerClassName());
        config.setProperty(IThreadPool.THREADPOOL_NAME, "IPC");
        config.setProperty(IIPCStub.STUB_CONFIG_PARAM, this);
        ComponentInfo<ThreadPool> info = new ComponentInfo<ThreadPool>(
        		getRequestHandlerClassName()+"_ThreadPool", 
        		ThreadPool.class, 
        		IThreadPool.class, 
        		LifestyleType.TRANSIENT_TYPE, config);
        this.threadPool = ComponentManagerFactory.getComponentManager().getComponent(info);
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
        this.requestHandlerClassName = (String) config.get(REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM);
        if (this.requestHandlerClassName == null) {
            throw new NullPointerException("Request handler class name must be specified");
        }
        Integer numThreads = (Integer) config.get(THREAD_POOL_SIZE_CONFIG_PARAM);
        if (numThreads == null) {
            numThreads = new Integer(10);
        }
        this.threadPoolSize = numThreads.intValue();
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IIPCStub#stop()
     */
    public void stop() {
        getThreadPool().stop();
    }
}
