/*
 * Created on Dec 3, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.ThreadPool;

/**
 * This is the Remote Event Dispatch Manager implementation class. This class
 * sends event notification to remote DCSF instance over web service. The event
 * notification requests are stored and passed to a thread pool.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/RemoteEventDispatchMgrImpl.java#1 $:
 */

public class RemoteEventDispatchMgrImpl implements IRemoteEventDispatchMgr {

    /**
     * Log related constant
     */
    private static final MessageFormat INITIALIZATION_MSG = new MessageFormat("Initialized instance of '" + RemoteEventRegistrationMgrImpl.class.getName() + "' with workerClassName: ''{0}''");
    private static final MessageFormat ADDING_EVENT_TO_QUEUE_MSG = new MessageFormat("Adding event ''{0}'' to remove event dispatch worker queue.");

    /**
     * Default size for the thread pool
     */
    private static final Integer DEFAULT_THREADPOOL_SIZE = new Integer(3);
    private Log log;
    private IConfiguration config;
    private IThreadPool workerPool;
    private String workerClassName = RemoteEventDispatchWorkerImpl.class.getName();

    /**
     * Fires an event to a remote DCSF service instance
     * 
     * @param event
     *            event to be fired
     * @param remoteLocation
     *            location of the remote DCSF service
     */
    public void fireEvent(IDCCServerEvent event, URL remoteLocation) {
        //Prepare a request object and submit it to the worker thread pool
        if (getLog().isDebugEnabled()) {
            getLog().debug(ADDING_EVENT_TO_QUEUE_MSG.format(new Object[] { event.getName() }));
        }

        RemoteEventDispatchRequest request = new RemoteEventDispatchRequest(event, remoteLocation);
        this.workerPool.doWork(request);
    }

    /**
     * Initialization function
     */
    public void init() {

        //Initializes the thread pool
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IThreadPool.THREADPOOL_SIZE, DEFAULT_THREADPOOL_SIZE);
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, this.workerClassName);
        config.setProperty(IThreadPool.THREADPOOL_NAME, "RemoteEventDispatchWorkerPool");
        ComponentInfo threadPoolCompInfo = new ComponentInfo("DMSRemoteEventDispatchThreadPool", ThreadPool.class.getName(), IThreadPool.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        this.workerPool = (IThreadPool) compMgr.getComponent(threadPoolCompInfo);

        if (getLog().isDebugEnabled()) {
            getLog().debug(INITIALIZATION_MSG.format(new Object[] { this.workerClassName }));
        }
    }

    /**
     * Cleanup function
     */
    public void dispose() {
        this.workerPool = null;
        this.config = null;
        this.log = null;
    }

    /**
     * Sets the configuration
     * 
     * @param conf
     *            configuration object
     */
    public void setConfiguration(IConfiguration conf) {
        this.config = conf;
        String newWorkerClassName = (String) this.config.get(IThreadPool.WORKER_CLASS_NAME);
        if (newWorkerClassName != null) {
            this.workerClassName = newWorkerClassName;
        }

    }

    /**
     * Returns the configuration
     * 
     * @return the configuration
     */
    public IConfiguration getConfiguration() {
        return (this.config);
    }

    /**
     * Sets the log object
     * 
     * @param logObject
     *            log object
     */
    public void setLog(Log logObject) {
        this.log = logObject;
    }

    /**
     * @return the component log object
     */
    public Log getLog() {
        return (this.log);
    }

}