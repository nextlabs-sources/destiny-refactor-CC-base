/*
 * Created on Dec 1, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.threading;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IWorker {

    /**
     * Perform the task specified.
     * 
     * If the function is catching InterruptedException, please call the Thread.interrupt() again
     * The status of the Thread must stay interrupted once it is interrupted. 
     * Otherwise the shutdown may not be completed.
     * 
     * 
     * @param task
     *            task to perform
     */
    public void doWork(ITask task);

}