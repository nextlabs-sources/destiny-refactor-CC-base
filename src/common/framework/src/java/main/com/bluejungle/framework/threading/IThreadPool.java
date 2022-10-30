/*
 * Created on Dec 1, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.framework.threading;

import java.util.concurrent.BlockingQueue;

import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/threading/IThreadPool.java#1 $:
 */

public interface IThreadPool extends IWorker, IStartable {

    PropertyKey<String> THREADPOOL_NAME = new PropertyKey<String>("Purpose");
    
    PropertyKey<Integer> THREADPOOL_SIZE = new PropertyKey<Integer>("THREADPOOL_SIZE");

    PropertyKey<BlockingQueue<ITask>> WORK_QUEUE = new PropertyKey<BlockingQueue<ITask>>("WORK_QUEUE");

    PropertyKey<IConfiguration> THREADPOOL_CONFIG = new PropertyKey<IConfiguration>("THREADPOOL_CONFIG");

    PropertyKey<String> WORKER_CLASS_NAME = new PropertyKey<String>("WORKER_CLASS_NAME");

    PropertyKey<Boolean> MANUAL_START = new PropertyKey<Boolean>("MANUAL_START");

    /**
     * start all threads in the threadpool
     */
    public void startThreads();
}
