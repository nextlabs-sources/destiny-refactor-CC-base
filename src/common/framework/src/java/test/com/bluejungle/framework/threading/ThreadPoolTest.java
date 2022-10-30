/*
 * Created on Dec 1, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.threading;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.ThreadPool;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class ThreadPoolTest extends BaseDestinyTestCase {

    public ThreadPoolTest(String name) {
        super(name);
    }

    public void testThreadPool() throws InterruptedException {
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IThreadPool.THREADPOOL_SIZE, new Integer(10));
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, TestWorker.class.getName());
        config.setProperty(IThreadPool.THREADPOOL_NAME, "ThreadPoolTest");
        ComponentInfo info = new ComponentInfo(ThreadPool.class.getName(), ThreadPool.class.getName(), IThreadPool.class.getName(), LifestyleType.TRANSIENT_TYPE, config);

        IThreadPool threadPool = (IThreadPool) ComponentManagerFactory.getComponentManager().getComponent(info);

        for (int i = 0; i < 100; i++) {
            threadPool.doWork(new TestTask(Integer.toString(i)));
        }

        Thread.sleep(4000);
        threadPool.stop();

        assertEquals("Number of tasks executed is not the same as expected", 100, TestWorker.number.intValue());
    }

}