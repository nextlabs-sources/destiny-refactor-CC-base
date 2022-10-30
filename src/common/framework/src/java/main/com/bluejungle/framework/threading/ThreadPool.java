/*
 * Created on Dec 1, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.threading;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class ThreadPool implements IConfigurable, IInitializable, IThreadPool, ILogEnabled {

    private static final String DEFAULT_NAME = "Default Thread Pool";

    private final BlockingQueue<ITask> workQueue = new LinkedBlockingQueue<ITask>();
    private IConfiguration config;
    private Thread[] threads;
    private Log log;
    private String threadPoolName;

    /**
     * Add task to workQueue and notify queue
     * 
     * @see com.bluejungle.framework.threading.IWorker#doWork(com.bluejungle.framework.threading.ITask)
     */
    public void doWork(ITask task) {
        if(log.isTraceEnabled()){
            log.trace("Current queue size of '" + threadPoolName + "' = " + workQueue.size()
                      + ", new task = " + task);
        }
        try {
            workQueue.put(task);
        } catch (InterruptedException ex) {
            log.trace("Unable to put task " + task + " on work queue.  Skipping");
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
     * Start worker threads
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.threadPoolName = config.get(THREADPOOL_NAME, DEFAULT_NAME);
        int threadPoolSize = config.get(THREADPOOL_SIZE);
        HashMapConfiguration workerConfig = new HashMapConfiguration();
        workerConfig.setProperty(THREADPOOL_CONFIG, config);
        workerConfig.setProperty(WORK_QUEUE, workQueue);
        ComponentInfo<WorkerThread> info = new ComponentInfo<WorkerThread>(
                WorkerThread.class.getName(), 
                WorkerThread.class, 
                WorkerThread.class, 
                LifestyleType.TRANSIENT_TYPE, 
                workerConfig);
        ComponentManagerFactory.getComponentManager().registerComponent(info, true);

        threads = new Thread[threadPoolSize];

        for (int i = 0; i < threadPoolSize; i++) {
            // Create a name for the thread:
            String workerName = this.threadPoolName + "-" + i;
            workerConfig.setProperty(WorkerThread.WORKER_NAME, workerName);
            WorkerThread thread = ComponentManagerFactory.getComponentManager().getComponent(info);
            threads[i] = thread;
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {
        this.log.debug("Stopping threads in thread pool - '" + threadPoolName + "'");
        if (threads != null) {
            for (Thread thread : threads) {
                thread.interrupt();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        this.log.debug("Stopped all threads in pool - " + threadPoolName + "'");
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {
        if (!this.config.get(IThreadPool.MANUAL_START, false)) {
            startThreads();
        }
    }

    /**
     * start all threads in the threadpool
     */
    public void startThreads() {
        log.debug("Starting threads in thread pool - '" + threadPoolName + "'");
        if (threads != null) {
            for (Thread thread : threads) {
                thread.start();
            }
        } else {
            log.error("Trying to start a thread pool without calling init().");
        }
        log.debug("Started all threads in pool - " + threadPoolName + "'");
    }

}
