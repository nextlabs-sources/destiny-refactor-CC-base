/*
 * Created on Dec 1, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.framework.threading;

import com.bluejungle.framework.threading.ITask;
import com.bluejungle.framework.threading.IWorker;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/threading/TestWorker.java#1 $:
 */

public class TestWorker implements IWorker {
    
    public static Integer number = new Integer (0);

    /**
     * @see com.bluejungle.framework.threading.IWorker#doWork(com.bluejungle.framework.threading.ITask)
     */
    public void doWork(ITask task) {
        TestTask testTask = (TestTask) task;
        synchronized (number)
        {
            number = new Integer (number.intValue() + 1);
        }
        System.out.println (Thread.currentThread().getName() + ": " + testTask.getName());
    }

}
