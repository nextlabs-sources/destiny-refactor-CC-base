/*
 * Created on Dec 1, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.threading;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class WorkerThread extends Thread implements IConfigurable, IInitializable, ILogEnabled {

    public static final PropertyKey<String> WORKER_NAME = new PropertyKey<String>("WorkerName");

    private IConfiguration config;

    private IWorker worker;

    private BlockingQueue<ITask> workQueue;

    Log log = null;

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return config;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration threadPoolConfig = config.get(IThreadPool.THREADPOOL_CONFIG);
        String workerClassName = threadPoolConfig.get(IThreadPool.WORKER_CLASS_NAME);
        ComponentInfo<IWorker> info = new ComponentInfo<IWorker>(
                workerClassName, 
                workerClassName, 
                IWorker.class.getName(), 
                LifestyleType.TRANSIENT_TYPE, 
                threadPoolConfig);

        worker = ComponentManagerFactory.getComponentManager().getComponent(info);

        workQueue = config.get(IThreadPool.WORK_QUEUE);

        this.setName(config.get(WORKER_NAME));
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (true) {
            try {
                worker.doWork(workQueue.take());
                if (log.isTraceEnabled()) {
                    log.trace(this.getName() + ": Queue size: " + workQueue.size());
                }
            } catch(InterruptedException ex) {
                log.info(this.getName() + " interrupted exception.  Exiting");
                // Give up.
                return;
            } catch (Throwable th) {
                log.error("Thread pool worker thread has thrown an exception", th);
            }
        }
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
